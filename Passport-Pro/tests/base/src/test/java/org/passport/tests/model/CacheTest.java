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

package org.passport.tests.model;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.passport.models.ClientModel;
import org.passport.models.RealmModel;
import org.passport.models.RoleModel;
import org.passport.models.UserModel;
import org.passport.models.UserSessionModel;
import org.passport.models.cache.infinispan.ClientAdapter;
import org.passport.models.cache.infinispan.RealmAdapter;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.RealmConfig;
import org.passport.testframework.realm.RealmConfigBuilder;
import org.passport.testframework.remote.runonserver.InjectRunOnServer;
import org.passport.testframework.remote.runonserver.RunOnServerClient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@PassportIntegrationTest
public class CacheTest {

    @InjectRealm(config = CacheRealmConfig.class)
    ManagedRealm managedRealm;

    @InjectRunOnServer
    RunOnServerClient runOnServer;

    @Test
    public void testStaleCache() throws Exception {
        runOnServer.run(session -> {
            // load up cache
            RealmModel realm = session.realms().getRealmByName("test");
            assertTrue(realm instanceof RealmAdapter);
            ClientModel testApp = realm.getClientByClientId("test-app");
            assertTrue(testApp instanceof ClientAdapter);
            assertNotNull(testApp);
            String appId = testApp.getId();
            assertTrue(testApp.isEnabled());

            // update realm, then get an AppModel and change it.  The AppModel would not be a cache adapter
            realm = session.realms().getRealmsStream().filter(r -> {
                assertTrue(r instanceof RealmAdapter);
                return "test".equals(r.getName());
            }).findFirst().orElse(null);

            assertNotNull(realm);

            realm.setAccessCodeLifespanLogin(200);
            testApp = realm.getClientByClientId("test-app");

            assertNotNull(testApp);
            testApp.setEnabled(false);

            // make sure that app cache was flushed and enabled changed
            realm = session.realms().getRealmByName("test");
            Assertions.assertEquals(200, realm.getAccessCodeLifespanLogin());
            testApp = session.clients().getClientById(realm, appId);
            Assertions.assertFalse(testApp.isEnabled());
        });
    }

    @Test
    public void testAddUserNotAddedToCache() {

    	runOnServer.run(session -> {
            RealmModel realm = session.realms().getRealmByName("test");

            UserModel user = session.users().addUser(realm, "testAddUserNotAddedToCache");
            user.setFirstName("firstName");
            user.addRequiredAction(UserModel.RequiredAction.CONFIGURE_TOTP);

            UserSessionModel userSession = session.sessions().createUserSession(UUID.randomUUID().toString(), realm, user, "testAddUserNotAddedToCache",
					"127.0.0.1", "auth", false, null, null, UserSessionModel.SessionPersistenceState.PERSISTENT);
            user = userSession.getUser();

            user.setLastName("lastName");

            assertNotNull(user.getLastName());
       });

    }

    // PASSPORT-1842
    @Test
    public void testRoleMappingsInvalidatedWhenClientRemoved() {
      	runOnServer.run(session -> {
            RealmModel realm = session.realms().getRealmByName("test");
            
            UserModel user = session.users().addUser(realm, "joel");
            ClientModel client = realm.addClient("foo");
            RoleModel fooRole = client.addRole("foo-role");
            user.grantRole(fooRole);
       });

        runOnServer.run(session -> {
        	RealmModel realm = session.realms().getRealmByName("test");
            UserModel user = session.users().getUserByUsername(realm, "joel");
            long grantedRolesCount = user.getRoleMappingsStream().count();

            ClientModel client = realm.getClientByClientId("foo");
            realm.removeClient(client.getId());

            realm = session.realms().getRealmByName("test");
            user = session.users().getUserByUsername(realm, "joel");
        
            Set<RoleModel> roles = user.getRoleMappingsStream().collect(Collectors.toSet());
            for (RoleModel role : roles) {
                Assertions.assertNotNull(role.getContainer());
            }
        
            Assertions.assertEquals(roles.size(), grantedRolesCount - 1);
        });

    }

    public static final class CacheRealmConfig implements RealmConfig {

        @Override
        public RealmConfigBuilder configure(RealmConfigBuilder realm) {
            realm.name("test");
            realm.addClient("test-app");
            return realm;
        }
    }

}
