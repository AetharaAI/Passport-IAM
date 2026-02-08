/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.passport.migration.migrators;

import org.passport.migration.MigrationProvider;
import org.passport.migration.ModelVersion;
import org.passport.models.AuthenticationFlowModel;
import org.passport.models.Constants;
import org.passport.models.IdentityProviderModel;
import org.passport.models.IdentityProviderQuery;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.utils.DefaultAuthenticationFlows;
import org.passport.representations.idm.RealmRepresentation;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MigrateTo1_7_0 implements Migration {

    public static final ModelVersion VERSION = new ModelVersion("1.7.0");

    public ModelVersion getVersion() {
        return VERSION;
    }

    public void migrate(PassportSession session) {
        RealmModel sessionRealm = session.getContext().getRealm();
        session.realms().getRealmsStream().forEach(realm -> {
            session.getContext().setRealm(realm);
            migrateRealm(session, realm);
        });
        session.getContext().setRealm(sessionRealm);
    }

    @Override
    public void migrateImport(PassportSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        RealmModel sessionRealm = session.getContext().getRealm();
        session.getContext().setRealm(realm);
        migrateRealm(session, realm);
        session.getContext().setRealm(sessionRealm);
    }

    protected void migrateRealm(PassportSession session, RealmModel realm) {
        // Set default accessToken timeout for implicit flow
        realm.setAccessTokenLifespanForImplicitFlow(Constants.DEFAULT_ACCESS_TOKEN_LIFESPAN_FOR_IMPLICIT_FLOW_TIMEOUT);

        // Add 'admin-cli' builtin client
        MigrationProvider migrationProvider = session.getProvider(MigrationProvider.class);
        migrationProvider.setupAdminCli(realm);

        // add firstBrokerLogin flow and set it to all identityProviders
        DefaultAuthenticationFlows.migrateFlows(realm);
        AuthenticationFlowModel firstBrokerLoginFlow = realm.getFlowByAlias(DefaultAuthenticationFlows.FIRST_BROKER_LOGIN_FLOW);

        session.identityProviders().getAllStream(IdentityProviderQuery.userAuthentication().with(IdentityProviderModel.FIRST_BROKER_LOGIN_FLOW_ID, ""), null, null)
                    .forEach(provider -> {
                        provider.setFirstBrokerLoginFlowId(firstBrokerLoginFlow.getId());
                        session.identityProviders().update(provider);
                });
    }
}
