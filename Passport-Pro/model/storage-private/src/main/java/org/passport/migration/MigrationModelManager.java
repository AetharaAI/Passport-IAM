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

package org.passport.migration;

import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.storage.DatastoreProvider;
import org.passport.storage.datastore.DefaultDatastoreProvider;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class MigrationModelManager {

    public static void migrate(PassportSession session) {
        ((DefaultDatastoreProvider) session.getProvider(DatastoreProvider.class)).getMigrationManager().migrate();
    }

    public static void migrateImport(PassportSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        ((DefaultDatastoreProvider) session.getProvider(DatastoreProvider.class)).getMigrationManager().migrate(realm, rep, skipUserDependent);
    }

}
