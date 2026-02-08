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

package org.passport.testsuite.model.authz;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.passport.authorization.AuthorizationProvider;
import org.passport.authorization.model.Policy;
import org.passport.authorization.model.ResourceServer;
import org.passport.authorization.store.StoreFactory;
import org.passport.models.ClientModel;
import org.passport.models.ClientProvider;
import org.passport.models.Constants;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.RealmProvider;
import org.passport.models.UserModel;
import org.passport.models.cache.authorization.CachedStoreFactoryProvider;
import org.passport.models.utils.PassportModelUtils;
import org.passport.models.utils.ModelToRepresentation;
import org.passport.representations.idm.authorization.UmaPermissionRepresentation;
import org.passport.representations.idm.authorization.UserPolicyRepresentation;
import org.passport.testsuite.model.PassportModelTest;
import org.passport.testsuite.model.RequireProvider;

import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@RequireProvider(CachedStoreFactoryProvider.class)
@RequireProvider(RealmProvider.class)
@RequireProvider(ClientProvider.class)
public class ConcurrentAuthzTest extends PassportModelTest {

    private String realmId;
    private String resourceServerId;
    private String resourceId;
    private String adminId;

    @Override
    protected void createEnvironment(PassportSession s) {
        RealmModel realm = createRealm(s, "test");
        s.getContext().setRealm(realm);
        realm.setDefaultRole(s.roles().addRealmRole(realm, Constants.DEFAULT_ROLES_ROLE_PREFIX + "-" + realm.getName()));

        realmId = realm.getId();

        ClientModel client = s.clients().addClient(realm, "my-server");

        AuthorizationProvider authorization = s.getProvider(AuthorizationProvider.class);
        StoreFactory aStore = authorization.getStoreFactory();

        ResourceServer rs = aStore.getResourceServerStore().create(client);
        resourceServerId = rs.getId();
        resourceId =  aStore.getResourceStore().create(rs, "myResource", client.getClientId()).getId();
        aStore.getScopeStore().create(rs, "read");

        adminId = s.users().addUser(realm, "admin").getId();
    }

    @Override
    protected void cleanEnvironment(PassportSession s) {
        RealmModel realm = s.realms().getRealm(realmId);
        s.getContext().setRealm(realm);
        s.realms().removeRealm(realmId);
    }

    @Override
    protected boolean isUseSamePassportSessionFactoryForAllThreads() {
        return true;
    }

    @Test
    public void testPermissionRemoved() {
        IntStream.range(0, 500).parallel().forEach(index -> {
            String permissionId = withRealm(realmId, (session, realm) -> {
                AuthorizationProvider authorization = session.getProvider(AuthorizationProvider.class);
                StoreFactory aStore = authorization.getStoreFactory();
                ResourceServer rs = aStore.getResourceServerStore().findById(resourceServerId);

                UserModel u = session.users().addUser(realm, "user" + index);

                UmaPermissionRepresentation permission = new UmaPermissionRepresentation();
                permission.setName(PassportModelUtils.generateId());
                permission.addUser(u.getUsername());
                permission.addScope("read");

                permission.addResource(resourceId);
                permission.setOwner(adminId);
                return aStore.getPolicyStore().create(rs, permission).getId();
            });

            withRealm(realmId, (session, realm) -> {
                AuthorizationProvider authorization = session.getProvider(AuthorizationProvider.class);
                StoreFactory aStore = authorization.getStoreFactory();

                aStore.getPolicyStore().delete(permissionId);
                return null;
            });

            withRealm(realmId, (session, realm) -> {
                AuthorizationProvider authorization = session.getProvider(AuthorizationProvider.class);
                StoreFactory aStore = authorization.getStoreFactory();
                ResourceServer rs = aStore.getResourceServerStore().findById(resourceServerId);

                Map<Policy.FilterOption, String[]> searchMap = new HashMap<>();
                searchMap.put(Policy.FilterOption.TYPE, new String[]{"uma"});
                searchMap.put(Policy.FilterOption.OWNER, new String[]{adminId});
                searchMap.put(Policy.FilterOption.PERMISSION, new String[] {"true"});
                Set<String> s = aStore.getPolicyStore().find(rs, searchMap, 0, 500).stream().map(Policy::getId).collect(Collectors.toSet());
                assertThat(s, not(contains(permissionId)));
                return null;
            });
        });
    }

    @Test
    @Ignore // This is ignored due to intermittent failure, see https://github.com/passport/passport/issues/14917
    public void testStaleCacheConcurrent() {
        String permissionId = withRealm(realmId, (session, realm) -> {
            AuthorizationProvider authorization = session.getProvider(AuthorizationProvider.class);
            StoreFactory aStore = authorization.getStoreFactory();
            UserModel u = session.users().getUserById(realm, adminId);
            ResourceServer rs = aStore.getResourceServerStore().findById(resourceServerId);


            UmaPermissionRepresentation permission = new UmaPermissionRepresentation();
            permission.setName(PassportModelUtils.generateId());
            permission.addUser(u.getUsername());
            permission.addScope("read");

            permission.addResource(resourceId);
            permission.setOwner(adminId);
            return aStore.getPolicyStore().create(rs, permission).getId();
        });

        IntStream.range(0, 500).parallel().forEach(index -> {
            String createdPolicyId = withRealm(realmId, (session, realm) -> {
                AuthorizationProvider authorization = session.getProvider(AuthorizationProvider.class);
                StoreFactory aStore = authorization.getStoreFactory();
                ResourceServer rs = aStore.getResourceServerStore().findById(resourceServerId);
                Policy permission = aStore.getPolicyStore().findById(rs, permissionId);

                UserPolicyRepresentation userRep = new UserPolicyRepresentation();
                userRep.setName("isAdminUser" + index);
                userRep.addUser("admin");
                Policy associatedPolicy = aStore.getPolicyStore().create(rs, userRep);
                permission.addAssociatedPolicy(associatedPolicy);
                return associatedPolicy.getId();
            });

            withRealm(realmId, (session, realm) -> {
                AuthorizationProvider authorization = session.getProvider(AuthorizationProvider.class);
                StoreFactory aStore = authorization.getStoreFactory();
                ResourceServer rs = aStore.getResourceServerStore().findById(resourceServerId);
                Policy permission = aStore.getPolicyStore().findById(rs, permissionId);

                assertThat(permission.getAssociatedPolicies(), not(contains(nullValue())));
                ModelToRepresentation.toRepresentation(permission, authorization);

                return null;
            });

            withRealm(realmId, (session, realm) -> {
                AuthorizationProvider authorization = session.getProvider(AuthorizationProvider.class);
                StoreFactory aStore = authorization.getStoreFactory();
                aStore.getPolicyStore().delete(createdPolicyId);
                return null;
            });
        });
    }
}
