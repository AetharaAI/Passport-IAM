/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.passport.authorization.store;

import java.util.HashMap;
import java.util.Map;

import org.passport.authorization.store.syncronization.ClientApplicationSynchronizer;
import org.passport.authorization.store.syncronization.GroupSynchronizer;
import org.passport.authorization.store.syncronization.RealmSynchronizer;
import org.passport.authorization.store.syncronization.RoleSynchronizer;
import org.passport.authorization.store.syncronization.Synchronizer;
import org.passport.authorization.store.syncronization.UserSynchronizer;
import org.passport.models.ClientModel.ClientRemovedEvent;
import org.passport.models.GroupModel.GroupRemovedEvent;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RealmModel.RealmRemovedEvent;
import org.passport.models.RoleContainerModel.RoleRemovedEvent;
import org.passport.models.UserModel.UserRemovedEvent;
import org.passport.provider.ProviderEvent;
import org.passport.provider.ProviderFactory;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface AuthorizationStoreFactory extends ProviderFactory<StoreFactory> {

    @Override
    default void postInit(PassportSessionFactory factory) {
        registerSynchronizationListeners(factory);
    }

    default void registerSynchronizationListeners(PassportSessionFactory factory) {
        Map<Class<? extends ProviderEvent>, Synchronizer> synchronizers = new HashMap<>();

        synchronizers.put(ClientRemovedEvent.class, new ClientApplicationSynchronizer());
        synchronizers.put(RealmRemovedEvent.class, new RealmSynchronizer());
        synchronizers.put(UserRemovedEvent.class, new UserSynchronizer());
        synchronizers.put(GroupRemovedEvent.class, new GroupSynchronizer());
        synchronizers.put(RoleRemovedEvent.class, new RoleSynchronizer());

        factory.register(event -> {
            try {
                synchronizers.forEach((eventType, synchronizer) -> {
                    if (eventType.isInstance(event)) {
                        synchronizer.synchronize(event, factory);
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException("Error synchronizing authorization data.", e);
            }
        });
    }
}
