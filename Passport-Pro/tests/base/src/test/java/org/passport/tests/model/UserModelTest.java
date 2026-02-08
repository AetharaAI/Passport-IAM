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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.passport.models.ClientModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.RoleModel;
import org.passport.models.UserModel;
import org.passport.models.UserModel.RequiredAction;
import org.passport.models.utils.PassportModelUtils;
import org.passport.services.managers.ClientManager;
import org.passport.services.managers.RealmManager;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.RealmConfig;
import org.passport.testframework.realm.RealmConfigBuilder;
import org.passport.testframework.remote.annotations.TestOnServer;

import org.junit.jupiter.api.Assertions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@PassportIntegrationTest
public class UserModelTest {

    @InjectRealm(lifecycle = LifeCycle.METHOD, config = UserModelRealm.class)
    ManagedRealm originalRealm;

    @TestOnServer
    public void persistUser(PassportSession session) {
        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesPersistUser) -> {
            PassportSession currentSession = sesPersistUser;
            RealmModel realm = currentSession.realms().getRealmByName("original");

            UserModel user = currentSession.users().addUser(realm, "user");
            user.setFirstName("first-name");
            user.setLastName("last-name");
            user.setEmail("email");
            assertNotNull(user.getCreatedTimestamp());
            // test that timestamp is current with 10s tollerance
            Assertions.assertTrue((System.currentTimeMillis() - user.getCreatedTimestamp()) < 10000);

            user.addRequiredAction(RequiredAction.CONFIGURE_TOTP);
            user.addRequiredAction(RequiredAction.UPDATE_PASSWORD);

            RealmModel searchRealm = currentSession.realms().getRealm(realm.getId());
            UserModel persisted = currentSession.users().getUserByUsername(searchRealm, "user");

            assertUserModel(user, persisted);

            searchRealm = currentSession.realms().getRealm(realm.getId());
            UserModel persisted2 = currentSession.users().getUserById(searchRealm, user.getId());
            assertUserModel(user, persisted2);

            Map<String, String> attributes = new HashMap<>();
            attributes.put(UserModel.LAST_NAME, "last-name");
            List<UserModel> search = currentSession.users().searchForUserStream(realm, attributes)
                    .collect(Collectors.toList());
            assertThat(search, hasSize(1));
            assertThat(search.get(0).getUsername(), equalTo("user"));

            attributes.clear();
            attributes.put(UserModel.EMAIL, "email");
            search = currentSession.users().searchForUserStream(realm, attributes)
                    .collect(Collectors.toList());
            assertThat(search, hasSize(1));
            assertThat(search.get(0).getUsername(), equalTo("user"));

            attributes.clear();
            attributes.put(UserModel.LAST_NAME, "last-name");
            attributes.put(UserModel.EMAIL, "email");
            search = currentSession.users().searchForUserStream(realm, attributes).collect(Collectors.toList());
            assertThat(search, hasSize(1));
            assertThat(search.get(0).getUsername(), equalTo("user"));
        });
    }

    @TestOnServer
    public void webOriginSetTest(PassportSession session) {
        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesWebOrigin) -> {
            PassportSession currentSession = sesWebOrigin;
            RealmModel realm = currentSession.realms().getRealmByName("original");

            ClientModel client = realm.addClient("user");

            assertThat(client.getWebOrigins(), empty());

            client.addWebOrigin("origin-1");
            assertThat(client.getWebOrigins(), hasSize(1));

            client.addWebOrigin("origin-2");
            assertThat(client.getWebOrigins(), hasSize(2));

            client.removeWebOrigin("origin-2");
            assertThat(client.getWebOrigins(), hasSize(1));

            client.removeWebOrigin("origin-1");
            assertThat(client.getWebOrigins(), empty());

            client = realm.addClient("oauthclient2");

            assertThat(client.getWebOrigins(), empty());

            client.addWebOrigin("origin-1");
            assertThat(client.getWebOrigins(), hasSize(1));

            client.addWebOrigin("origin-2");
            assertThat(client.getWebOrigins(), hasSize(2));

            client.removeWebOrigin("origin-2");
            assertThat(client.getWebOrigins(), hasSize(1));

            client.removeWebOrigin("origin-1");
            assertThat(client.getWebOrigins(), empty());
        });
    }

    @TestOnServer
    public void testUserRequiredActions(PassportSession session) {
        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesUserReqActions) -> {
            PassportSession currentSession = sesUserReqActions;
            RealmModel realm = currentSession.realms().getRealmByName("original");

            UserModel user = currentSession.users().addUser(realm, "user");
            List<String> requiredActions = user.getRequiredActionsStream().collect(Collectors.toList());
            assertThat(requiredActions, empty());

            user.addRequiredAction(RequiredAction.CONFIGURE_TOTP);
            String id = realm.getId();

            realm = currentSession.realms().getRealm(id);
            user = currentSession.users().getUserByUsername(realm, "user");

            requiredActions = user.getRequiredActionsStream().collect(Collectors.toList());
            assertThat(requiredActions, hasSize(1));
            assertThat(requiredActions, contains(RequiredAction.CONFIGURE_TOTP.name()));

            user.addRequiredAction(RequiredAction.CONFIGURE_TOTP);
            user = currentSession.users().getUserByUsername(realm, "user");

            requiredActions = user.getRequiredActionsStream().collect(Collectors.toList());
            assertThat(requiredActions, hasSize(1));
            assertThat(requiredActions, contains(RequiredAction.CONFIGURE_TOTP.name()));

            user.addRequiredAction(RequiredAction.VERIFY_EMAIL.name());
            user = currentSession.users().getUserByUsername(realm, "user");

            requiredActions = user.getRequiredActionsStream().collect(Collectors.toList());
            assertThat(requiredActions, hasSize(2));
            assertThat(requiredActions, containsInAnyOrder(
                    RequiredAction.CONFIGURE_TOTP.name(),
                    RequiredAction.VERIFY_EMAIL.name())
            );

            user.removeRequiredAction(RequiredAction.CONFIGURE_TOTP.name());
            user = currentSession.users().getUserByUsername(realm, "user");

            requiredActions = user.getRequiredActionsStream().collect(Collectors.toList());
            assertThat(requiredActions, hasSize(1));
            assertThat(requiredActions, contains(RequiredAction.VERIFY_EMAIL.name()));

            user.removeRequiredAction(RequiredAction.VERIFY_EMAIL.name());
            user = currentSession.users().getUserByUsername(realm, "user");

            requiredActions = user.getRequiredActionsStream().collect(Collectors.toList());
            assertThat(requiredActions, empty());
        });
    }

    @TestOnServer
    public void testUserMultipleAttributes(PassportSession session) throws Exception {
        AtomicReference<List<String>> attrValsAtomic = new AtomicReference<>();

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesMultipleAtr1) -> {
            PassportSession currentSession = sesMultipleAtr1;
            RealmModel realm = currentSession.realms().getRealmByName("original");

            UserModel user = currentSession.users().addUser(realm, "user");
            currentSession.users().addUser(realm, "user-noattrs");

            user.setSingleAttribute("key1", "value1");

            List<String> attrVals = new ArrayList<>(Arrays.asList("val21", "val22"));
            attrValsAtomic.set(attrVals);

            user.setAttribute("key2", attrVals);
        });

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesMultipleAtr2) -> {
            PassportSession currentSession = sesMultipleAtr2;
            RealmModel realm = currentSession.realms().getRealmByName("original");

            // Test read attributes
            UserModel user = currentSession.users().getUserByUsername(realm, "user");

            List<String> attrVals = user.getAttributeStream("key1").collect(Collectors.toList());
            assertThat(attrVals, hasSize(1));
            assertThat(attrVals, contains("value1"));
            assertThat(user.getFirstAttribute("key1"), equalTo("value1"));

            attrVals = user.getAttributeStream("key2").collect(Collectors.toList());
            assertThat(attrVals, hasSize(2));
            assertThat(attrVals, containsInAnyOrder("val21", "val22"));

            attrVals = user.getAttributeStream("key3").collect(Collectors.toList());
            assertThat(attrVals, empty());
            assertThat(user.getFirstAttribute("key3"), nullValue());

            Map<String, List<String>> allAttrVals = user.getAttributes();
            assertThat(allAttrVals.keySet(), hasSize(6));
            assertThat(allAttrVals.keySet(), containsInAnyOrder(UserModel.USERNAME, UserModel.FIRST_NAME, UserModel.LAST_NAME, UserModel.EMAIL, "key1", "key2"));
            assertThat(allAttrVals.get("key1"), equalTo(user.getAttributeStream("key1").collect(Collectors.toList())));
            assertThat(allAttrVals.get("key2"), equalTo(user.getAttributeStream("key2").collect(Collectors.toList())));

            // Test remove and rewrite attribute
            user.removeAttribute("key1");
            user.setSingleAttribute("key2", "val23");
        });

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesMultipleAtr3) -> {
            PassportSession currentSession = sesMultipleAtr3;
            RealmModel realm = currentSession.realms().getRealmByName("original");

            UserModel user = currentSession.users().getUserByUsername(realm, "user");
            assertThat(user.getFirstAttribute("key1"), nullValue());

            List<String> attrVals = user.getAttributeStream("key2").collect(Collectors.toList());

            assertThat(attrVals, hasSize(1));
            assertThat(attrVals.get(0), equalTo("val23"));
        });
    }

    // PASSPORT-3494
    @TestOnServer
    public void testUpdateUserAttribute(PassportSession session) throws Exception {

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesUpdateAtr1) -> {
            PassportSession currentSession = sesUpdateAtr1;
            RealmModel realm = currentSession.realms().getRealmByName("original");

            UserModel user = currentSession.users().addUser(realm, "user");

            user.setSingleAttribute("key1", "value1");
        });

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesUpdateAtr2) -> {
            PassportSession currentSession = sesUpdateAtr2;
            RealmModel realm = currentSession.realms().getRealmByName("original");

            UserModel user = currentSession.users().getUserByUsername(realm, "user");

            // Update attribute
            List<String> attrVals = new ArrayList<>(Arrays.asList("val2"));
            user.setAttribute("key1", attrVals);
            Map<String, List<String>> allAttrVals = user.getAttributes();

            // Ensure same transaction is able to see updated value
            assertThat(allAttrVals.keySet(), hasSize(5));
            assertThat(allAttrVals.keySet(), containsInAnyOrder("key1", UserModel.FIRST_NAME, UserModel.LAST_NAME, UserModel.EMAIL, UserModel.USERNAME));
            assertThat(allAttrVals.get("key1"), contains("val2"));
        });
    }

    // PASSPORT-3608
    @TestOnServer
    public void testUpdateUserSingleAttribute(PassportSession session) {

        AtomicReference<Map<String, List<String>>> expectedAtomic = new AtomicReference<>();

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesUpdateUserSingleAtr) -> {
            PassportSession currentSession = sesUpdateUserSingleAtr;
            RealmModel realm = currentSession.realms().getRealmByName("original");

            Map<String, List<String>> expected = new HashMap<>();
            expected.put("key1", Collections.singletonList("value3"));
            expected.put("key2", Collections.singletonList("value2"));
            expected.put(UserModel.FIRST_NAME, Collections.singletonList(null));
            expected.put(UserModel.LAST_NAME, Collections.singletonList(null));
            expected.put(UserModel.EMAIL, Collections.singletonList(null));
            expected.put(UserModel.USERNAME, Collections.singletonList("user"));

            UserModel user = currentSession.users().addUser(realm, "user");

            user.setSingleAttribute("key1", "value1");
            user.setSingleAttribute("key2", "value2");
            user.setSingleAttribute("key3", null); //PASSPORT-7014

            // Overwrite the first attribute
            user.setSingleAttribute("key1", "value3");

            assertThat(user.getAttributes(), equalTo(expected));

            expectedAtomic.set(expected);
        });

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesUpdateUserSingleAtr2) -> {
            PassportSession currentSession = sesUpdateUserSingleAtr2;
            RealmModel realm = currentSession.realms().getRealmByName("original");

            Map<String, List<String>> expected = expectedAtomic.get();
            assertThat(currentSession.users().getUserByUsername(realm, "user").getAttributes(), equalTo(expected));
        });
    }

    @TestOnServer
    public void testSearchByString(PassportSession session) {

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesSearchString1) -> {
            PassportSession currentSession = sesSearchString1;
            RealmModel realm = currentSession.realms().getRealmByName("original");

            currentSession.users().addUser(realm, "user1");
        });

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesSearchString1) -> {
            PassportSession currentSession = sesSearchString1;
            RealmModel realm = currentSession.realms().getRealmByName("original");

            UserModel user1 = currentSession.users().getUserByUsername(realm, "user1");

            List<UserModel> users = currentSession.users().searchForUserStream(realm, Map.of(UserModel.SEARCH, "user"), 0, 7)
                    .collect(Collectors.toList());
            assertThat(users, hasSize(1));
            assertThat(users, contains(user1));
        });
    }

    @TestOnServer
    public void testSearchByUserAttribute(PassportSession session) {
        try {
            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesSearchAtr1) -> {
                PassportSession currentSession = sesSearchAtr1;
                RealmModel realm = currentSession.realms().getRealmByName("original");

                UserModel user1 = currentSession.users().addUser(realm, "user1");
                UserModel user2 = currentSession.users().addUser(realm, "user2");
                UserModel user3 = currentSession.users().addUser(realm, "user3");

                user1.setSingleAttribute("key1", "value1");
                user1.setSingleAttribute("key2", "value21");

                user2.setSingleAttribute("key1", "value1");
                user2.setSingleAttribute("key2", "value22");

                user3.setSingleAttribute("key2", "value21");

                RealmModel otherRealm = new RealmManager(session).createRealm("other", "other");
                UserModel otherRealmUser = currentSession.users().addUser(otherRealm, "user1");
                otherRealmUser.setSingleAttribute("key2", "value21");
            });

            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesSearchAtr2) -> {
                PassportSession currentSession = sesSearchAtr2;
                RealmModel realm = currentSession.realms().getRealmByName("original");

                UserModel user1 = currentSession.users().getUserByUsername(realm, "user1");
                UserModel user2 = currentSession.users().getUserByUsername(realm, "user2");
                UserModel user3 = currentSession.users().getUserByUsername(realm, "user3");

                List<UserModel> users = currentSession.users().searchForUserByUserAttributeStream(realm, "key1", "value1")
                        .collect(Collectors.toList());
                assertThat(users, hasSize(2));
                assertThat(users, containsInAnyOrder(user1, user2));

                users = currentSession.users().searchForUserByUserAttributeStream(realm, "key2", "value21")
                        .collect(Collectors.toList());
                assertThat(users, hasSize(2));
                assertThat(users, containsInAnyOrder(user1, user3));

                users = currentSession.users().searchForUserByUserAttributeStream(realm, "key2", "value22")
                        .collect(Collectors.toList());
                assertThat(users, hasSize(1));
                assertThat(users, contains(user2));

                users = currentSession.users().searchForUserByUserAttributeStream(realm, "key3", "value3")
                        .collect(Collectors.toList());
                assertThat(users, empty());
            });
        } finally {
            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession cleanupSession) -> {
                cleanupSession.realms().removeRealm("other");
            });
        }
    }

    @TestOnServer
    public void testServiceAccountLink(PassportSession session) {
        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesServiceLink1) -> {
            PassportSession currentSession = sesServiceLink1;
            RealmModel realm = currentSession.realms().getRealmByName("original");

            ClientModel client = realm.addClient("foo");

            UserModel user1 = currentSession.users().addUser(realm, "user1");
            user1.setFirstName("John");
            user1.setLastName("Doe");

            UserModel user2 = currentSession.users().addUser(realm, "user2");
            user2.setFirstName("John");
            user2.setLastName("Doe");

            // Search
            assertThat(currentSession.users().getServiceAccount(client), nullValue());
            List<UserModel> users = currentSession.users().searchForUserStream(realm, Map.of(UserModel.SEARCH, "John Doe", UserModel.INCLUDE_SERVICE_ACCOUNT, "true"))
                    .collect(Collectors.toList());
            assertThat(users, hasSize(2));
            assertThat(users, containsInAnyOrder(user1, user2));

            // Link service account
            user1.setServiceAccountClientLink(client.getId());
        });

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesServiceLink2) -> {
            PassportSession currentSession = sesServiceLink2;
            RealmModel realm = currentSession.realms().getRealmByName("original");

            UserModel user1 = currentSession.users().getUserByUsername(realm, "user1");
            UserModel user2 = currentSession.users().getUserByUsername(realm, "user2");

            // Search and assert service account user not found
            ClientModel client = realm.getClientByClientId("foo");
            UserModel searched = currentSession.users().getServiceAccount(client);
            assertThat(searched, equalTo(user1));
            List<UserModel> users = currentSession.users().searchForUserStream(realm, Map.of(UserModel.SEARCH, "John Doe", UserModel.INCLUDE_SERVICE_ACCOUNT, "false"))
                    .collect(Collectors.toList());
            assertThat(users, hasSize(1));
            assertThat(users, contains(user2));

            users = currentSession.users().searchForUserStream(realm, Collections.singletonMap(UserModel.INCLUDE_SERVICE_ACCOUNT, Boolean.FALSE.toString())).collect(Collectors.toList());
            assertThat(users, hasSize(1));
            assertThat(users, contains(user2));

            users = currentSession.users().searchForUserStream(realm, Collections.emptyMap()).collect(Collectors.toList());
            assertThat(users, hasSize(2));
            assertThat(users, containsInAnyOrder(user1, user2));

            assertThat(currentSession.users().getUsersCount(realm, true), equalTo(2));
            assertThat(currentSession.users().getUsersCount(realm, false), equalTo(1));

            // Remove client
            RealmManager realmMgr = new RealmManager(currentSession);
            ClientManager clientMgr = new ClientManager(realmMgr);

            clientMgr.removeClient(realm, client);
        });

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesServiceLink3) -> {
            PassportSession currentSession = sesServiceLink3;
            RealmModel realm = currentSession.realms().getRealmByName("original");
            // Assert service account removed as well
            assertThat(currentSession.users().getUserByUsername(realm, "user1"), nullValue());
        });
    }

    @TestOnServer
    public void testGrantToAll(PassportSession session) {
        try {
            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), (PassportSession sesGrantToAll1) -> {
                PassportSession currentSession = sesGrantToAll1;

                RealmManager realmManager = new RealmManager(currentSession);

                RealmModel realm1 = realmManager.createRealm("realm1", "realm1");
                currentSession.getContext().setRealm(realm1);

                realm1.addRole("role1");
                currentSession.users().addUser(realm1, "user1");
                currentSession.users().addUser(realm1, "user2");

                RealmModel realm2 = realmManager.createRealm("realm2", "realm2");
                currentSession.users().addUser(realm2, "user1");
            });

            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), (PassportSession sesGrantToAll2) -> {
                PassportSession currentSession = sesGrantToAll2;
                RealmModel realm1 = currentSession.realms().getRealmByName("realm1");
                currentSession.getContext().setRealm(realm1);

                RoleModel role1 = realm1.getRole("role1");
                currentSession.users().grantToAllUsers(realm1, role1);
            });

            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), (PassportSession sesGrantToAll2) -> {
                PassportSession currentSession = sesGrantToAll2;
                RealmModel realm1 = currentSession.realms().getRealmByName("realm1");
                currentSession.getContext().setRealm(realm1);

                RoleModel role1 = realm1.getRole("role1");
                UserModel user1 = currentSession.users().getUserByUsername(realm1, "user1");
                UserModel user2 = currentSession.users().getUserByUsername(realm1, "user2");
                Assertions.assertTrue(user1.hasRole(role1));
                Assertions.assertTrue(user2.hasRole(role1));

                RealmModel realm2 = currentSession.realms().getRealmByName("realm2");
                currentSession.getContext().setRealm(realm2);
                UserModel realm2User1 = currentSession.users().getUserByUsername(realm2, "user1");
                Assertions.assertFalse(realm2User1.hasRole(role1));

                currentSession.realms().removeRealm(realm2.getId());
                currentSession.getContext().setRealm(realm1);
                currentSession.realms().removeRealm(realm1.getId());
            });
        } finally {
            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession cleanupSession) -> {
                cleanupSession.realms().removeRealm("realm1");
                cleanupSession.realms().removeRealm("realm2");
            });
        }
    }

    @TestOnServer
    public void testUserNotBefore(PassportSession session) {
        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesUserNotBefore1) -> {
            PassportSession currentSession = sesUserNotBefore1;
            RealmModel realm = currentSession.realms().getRealmByName("original");

            UserModel user1 = currentSession.users().addUser(realm, "user1");
            currentSession.users().setNotBeforeForUser(realm, user1, 10);
        });

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesUserNotBefore2) -> {
            PassportSession currentSession = sesUserNotBefore2;
            RealmModel realm = currentSession.realms().getRealmByName("original");

            UserModel user1 = currentSession.users().getUserByUsername(realm, "user1");
            int notBefore = currentSession.users().getNotBeforeOfUser(realm, user1);
            assertThat(notBefore, equalTo(10));

            // Try to update
            currentSession.users().setNotBeforeForUser(realm, user1, 20);
        });

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), (PassportSession sesUserNotBefore3) -> {
            PassportSession currentSession = sesUserNotBefore3;
            RealmModel realm = currentSession.realms().getRealmByName("original");

            UserModel user1 = currentSession.users().getUserByUsername(realm, "user1");
            int notBefore = currentSession.users().getNotBeforeOfUser(realm, user1);
            assertThat(notBefore, equalTo(20));
        });
    }

    private static void assertUserModel(UserModel expected, UserModel actual) {
        assertThat(actual.getUsername(), equalTo(expected.getUsername()));
        assertThat(actual.getCreatedTimestamp(), equalTo(expected.getCreatedTimestamp()));
        assertThat(actual.getFirstName(), equalTo(expected.getFirstName()));
        assertThat(actual.getLastName(), equalTo(expected.getLastName()));
        assertThat(actual.getRequiredActionsStream().collect(Collectors.toSet()),
                containsInAnyOrder(expected.getRequiredActionsStream().toArray()));
    }

    private static final class UserModelRealm implements RealmConfig {

        @Override
        public RealmConfigBuilder configure(RealmConfigBuilder realm) {
            return realm.name("original");
        }
    }

}
