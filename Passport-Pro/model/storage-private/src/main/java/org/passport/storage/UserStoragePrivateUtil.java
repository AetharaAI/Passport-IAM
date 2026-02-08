/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.passport.storage;

import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RealmModel;
import org.passport.models.UserProvider;
import org.passport.models.utils.PassportModelUtils;
import org.passport.storage.UserStorageProviderModel.SyncMode;
import org.passport.storage.datastore.DefaultDatastoreProvider;
import org.passport.storage.user.SynchronizationResult;

/**
 * @author Alexander Schwartz
 */
public class UserStoragePrivateUtil {

    public static UserProvider userLocalStorage(PassportSession session) {
        return ((DefaultDatastoreProvider) session.getProvider(DatastoreProvider.class)).userLocalStorage();
    }

    public static SynchronizationResult runFullSync(PassportSessionFactory sessionFactory, UserStorageProviderModel provider) {
        return PassportModelUtils.runJobInTransactionWithResult(sessionFactory, session -> {
            RealmModel realm = session.realms().getRealm(provider.getParentId());
            session.getContext().setRealm(realm);
            return new UserStorageSyncTask(provider, SyncMode.FULL).runWithResult(session);
        });
    }

    public static SynchronizationResult runPeriodicSync(PassportSessionFactory sessionFactory, UserStorageProviderModel provider) {
        return PassportModelUtils.runJobInTransactionWithResult(sessionFactory, session -> {
            RealmModel realm = session.realms().getRealm(provider.getParentId());
            session.getContext().setRealm(realm);
            return new UserStorageSyncTask(provider, SyncMode.CHANGED).runWithResult(session);
        });
    }
}
