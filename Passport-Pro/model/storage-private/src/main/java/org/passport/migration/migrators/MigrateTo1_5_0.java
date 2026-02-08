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

import org.passport.migration.ModelVersion;
import org.passport.models.AuthenticationFlowModel;
import org.passport.models.PassportSession;
import org.passport.models.OTPPolicy;
import org.passport.models.RealmModel;
import org.passport.models.utils.DefaultAuthenticationFlows;
import org.passport.representations.idm.RealmRepresentation;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class MigrateTo1_5_0 implements Migration {
    public static final ModelVersion VERSION = new ModelVersion("1.5.0");

    public ModelVersion getVersion() {
        return VERSION;
    }

    public void migrate(PassportSession session) {
        session.realms().getRealmsStream().forEach(realm -> migrateRealm(session, realm));
    }

    @Override
    public void migrateImport(PassportSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        migrateRealm(session, realm);
    }

    protected void migrateRealm(PassportSession session, RealmModel realm) {
        DefaultAuthenticationFlows.migrateFlows(realm); // add reset credentials flo
        realm.setOTPPolicy(OTPPolicy.DEFAULT_POLICY);
        realm.setBrowserFlow(realm.getFlowByAlias(DefaultAuthenticationFlows.BROWSER_FLOW));
        realm.setRegistrationFlow(realm.getFlowByAlias(DefaultAuthenticationFlows.REGISTRATION_FLOW));
        realm.setDirectGrantFlow(realm.getFlowByAlias(DefaultAuthenticationFlows.DIRECT_GRANT_FLOW));

        AuthenticationFlowModel resetFlow = realm.getFlowByAlias(DefaultAuthenticationFlows.RESET_CREDENTIALS_FLOW);
        if (resetFlow == null) {
            DefaultAuthenticationFlows.resetCredentialsFlow(realm);
        } else {
            realm.setResetCredentialsFlow(resetFlow);
        }

        AuthenticationFlowModel clientAuthFlow = realm.getFlowByAlias(DefaultAuthenticationFlows.CLIENT_AUTHENTICATION_FLOW);
        if (clientAuthFlow == null) {
            DefaultAuthenticationFlows.clientAuthFlow(realm);
        } else {
            realm.setClientAuthenticationFlow(clientAuthFlow);
        }

        realm.getClientsStream().forEach(MigrationUtils::setDefaultClientAuthenticatorType);
    }
}
