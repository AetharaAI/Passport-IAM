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

package org.passport.authorization.store.syncronization;

import java.util.EnumMap;
import java.util.Map;

import org.passport.authorization.AuthorizationProvider;
import org.passport.authorization.fgap.AdminPermissionsSchema;
import org.passport.authorization.model.PermissionTicket;
import org.passport.authorization.store.PermissionTicketStore;
import org.passport.authorization.store.PolicyStore;
import org.passport.authorization.store.ResourceStore;
import org.passport.authorization.store.StoreFactory;
import org.passport.models.PassportSessionFactory;
import org.passport.models.UserModel;
import org.passport.models.UserModel.UserRemovedEvent;
import org.passport.provider.ProviderFactory;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class UserSynchronizer implements Synchronizer<UserRemovedEvent> {

    @Override
    public void synchronize(UserRemovedEvent event, PassportSessionFactory factory) {
        ProviderFactory<AuthorizationProvider> providerFactory = factory.getProviderFactory(AuthorizationProvider.class);
        AuthorizationProvider authorizationProvider = providerFactory.create(event.getPassportSession());

        AdminPermissionsSchema.SCHEMA.removeResourceObject(authorizationProvider, event);

        removeFromUserPermissionTickets(event, authorizationProvider);
        removeUserResources(event, authorizationProvider);
    }

    private void removeUserResources(UserRemovedEvent event, AuthorizationProvider authorizationProvider) {
        StoreFactory storeFactory = authorizationProvider.getStoreFactory();
        PolicyStore policyStore = storeFactory.getPolicyStore();
        ResourceStore resourceStore = storeFactory.getResourceStore();
        UserModel userModel = event.getUser();

        resourceStore.findByOwner(null, userModel.getId(), resource -> {
            String resourceId = resource.getId();
            policyStore.findByResource(resource.getResourceServer(), resource).forEach(policy -> {
                if (policy.getResources().size() == 1) {
                    policyStore.delete(policy.getId());
                } else {
                    policy.removeResource(resource);
                }
            });
            resourceStore.delete(resourceId);
        });
    }

    private void removeFromUserPermissionTickets(UserRemovedEvent event, AuthorizationProvider authorizationProvider) {
        StoreFactory storeFactory = authorizationProvider.getStoreFactory();
        PermissionTicketStore ticketStore = storeFactory.getPermissionTicketStore();
        UserModel userModel = event.getUser();
        Map<PermissionTicket.FilterOption, String> attributes = new EnumMap<>(PermissionTicket.FilterOption.class);

        attributes.put(PermissionTicket.FilterOption.OWNER, userModel.getId());

        for (PermissionTicket ticket : ticketStore.find(null, attributes, null, null)) {
            ticketStore.delete(ticket.getId());
        }

        attributes.clear();

        attributes.put(PermissionTicket.FilterOption.REQUESTER, userModel.getId());

        for (PermissionTicket ticket : ticketStore.find(null, attributes, null, null)) {
            ticketStore.delete(ticket.getId());
        }
    }
}
