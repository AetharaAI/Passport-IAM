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
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.representations.idm.RealmRepresentation;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MigrateTo2_3_0 implements Migration {

    public static final ModelVersion VERSION = new ModelVersion("2.3.0");

    @Override
    public void migrate(PassportSession session) {
        session.realms().getRealmsStream().forEach(this::migrateRealm);
    }

    protected void migrateRealm(RealmModel realm) {
        realm.getClientsStream().forEach(MigrationUtils::updateProtocolMappers);

        realm.getClientScopesStream().forEach(MigrationUtils::updateProtocolMappers);
    }

    @Override
    public void migrateImport(PassportSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        migrateRealm(realm);
    }

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }
}
