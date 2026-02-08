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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.passport.common.util.Time;
import org.passport.models.AuthenticatedClientSessionModel;
import org.passport.models.ClientModel;
import org.passport.models.Constants;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserManager;
import org.passport.models.UserModel;
import org.passport.models.UserSessionModel;
import org.passport.models.utils.PassportModelUtils;
import org.passport.protocol.oidc.OIDCLoginProtocol;
import org.passport.services.managers.ClientManager;
import org.passport.services.managers.RealmManager;
import org.passport.services.managers.UserSessionManager;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.RealmConfig;
import org.passport.testframework.realm.RealmConfigBuilder;
import org.passport.testframework.remote.annotations.TestOnServer;

import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@PassportIntegrationTest
public class UserSessionProviderOfflineTest {

    @InjectRealm(config = UserSessionProviderOfflineRealm.class, lifecycle = LifeCycle.METHOD)
    ManagedRealm managedRealm;

    @TestOnServer
    public void testOfflineSessionsCrud(PassportSession session) {
        Map<String, Set<String>> offlineSessions = new HashMap<>();

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), currentSession -> {
            // Create some online sessions in infinispan
            createSessions(currentSession);
        });

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), currentSession -> {
            RealmModel realm = currentSession.realms().getRealmByName("test");

            // Key is userSession ID, values are client UUIDS
            // Persist 3 created userSessions and clientSessions as offline
            ClientModel testApp = realm.getClientByClientId("test-app");
            currentSession.sessions().getUserSessionsStream(realm, testApp).toList()
                    .forEach(userSession -> offlineSessions.put(userSession.getId(), createOfflineSessionIncludeClientSessions(currentSession, userSession)));
        });

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), currentSession -> {
            RealmModel realm = currentSession.realms().getRealmByName("test");
            UserSessionManager sessionManager = new UserSessionManager(currentSession);

            // Assert all previously saved offline sessions found
            for (Map.Entry<String, Set<String>> entry : offlineSessions.entrySet()) {
                UserSessionModel offlineSession = sessionManager.findOfflineUserSession(realm, entry.getKey());
                Assertions.assertNotNull(offlineSession);
                Assertions.assertEquals(offlineSession.getAuthenticatedClientSessions().keySet(), entry.getValue());
            }

            // Find clients with offline token
            UserModel user1 = currentSession.users().getUserByUsername(realm, "user1");

            Set<ClientModel> clients = sessionManager.findClientsWithOfflineToken(realm, user1);
            Assertions.assertEquals(2, clients.size());
            for (ClientModel client : clients) {
                Assertions.assertTrue(client.getClientId().equals("test-app") || client.getClientId().equals("third-party"));
            }

            UserModel user2 = currentSession.users().getUserByUsername(realm, "user2");

            clients = sessionManager.findClientsWithOfflineToken(realm, user2);
            Assertions.assertEquals(1, clients.size());
            Assertions.assertEquals("test-app", clients.iterator().next().getClientId());

            // Test count
            ClientModel testApp = realm.getClientByClientId("test-app");
            ClientModel thirdparty = realm.getClientByClientId("third-party");
            Assertions.assertEquals(currentSession.sessions().getOfflineSessionsCount(realm, testApp), 3);
            Assertions.assertEquals(currentSession.sessions().getOfflineSessionsCount(realm, thirdparty), 1);
            // Revoke "test-app" for user1
            sessionManager.revokeOfflineToken(user1, testApp);
        });

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), currentSession -> {
            RealmModel realm = currentSession.realms().getRealmByName("test");
            UserSessionManager sessionManager = new UserSessionManager(currentSession);

            // Assert userSession revoked
            ClientModel thirdparty = realm.getClientByClientId("third-party");

            List<UserSessionModel> thirdpartySessions = currentSession.sessions().getOfflineUserSessionsStream(realm, thirdparty, 0, 10)
                    .toList();
            Assertions.assertEquals(1, thirdpartySessions.size());
            Assertions.assertEquals("127.0.0.1", thirdpartySessions.get(0).getIpAddress());
            Assertions.assertEquals("user1", thirdpartySessions.get(0).getUser().getUsername());

            UserModel user1 = currentSession.users().getUserByUsername(realm, "user1");
            UserModel user2 = currentSession.users().getUserByUsername(realm, "user2");

            Set<ClientModel> clients = sessionManager.findClientsWithOfflineToken(realm, user1);
            Assertions.assertEquals(1, clients.size());
            Assertions.assertEquals("third-party", clients.iterator().next().getClientId());
            clients = sessionManager.findClientsWithOfflineToken(realm, user2);
            Assertions.assertEquals(1, clients.size());
            Assertions.assertEquals("test-app", clients.iterator().next().getClientId());

            // Revoke the second currentSession for user1 too.
            sessionManager.revokeOfflineToken(user1, thirdparty);

        });

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), session.getContext(), currentSession -> {
            RealmModel realm = currentSession.realms().getRealmByName("test");
            UserSessionManager sessionManager = new UserSessionManager(currentSession);

            ClientModel testApp = realm.getClientByClientId("test-app");
            ClientModel thirdparty = realm.getClientByClientId("third-party");

            // Accurate count now. All sessions of user1 cleared
            Assertions.assertEquals(currentSession.sessions().getOfflineSessionsCount(realm, testApp), 1);
            Assertions.assertEquals(currentSession.sessions().getOfflineSessionsCount(realm, thirdparty), 0);

            List<UserSessionModel> testAppSessions = currentSession.sessions().getOfflineUserSessionsStream(realm, testApp, 0, 10)
                    .toList();

            Assertions.assertEquals(1, testAppSessions.size());
            Assertions.assertEquals("127.0.0.3", testAppSessions.get(0).getIpAddress());
            Assertions.assertEquals("user2", testAppSessions.get(0).getUser().getUsername());

            UserModel user1 = currentSession.users().getUserByUsername(realm, "user1");

            Set<ClientModel> clients = sessionManager.findClientsWithOfflineToken(realm, user1);
            Assertions.assertEquals(0, clients.size());
        });
    }

    @TestOnServer
    public void testOnRealmRemoved(PassportSession session) {
        AtomicReference<String> userSessionID = new AtomicReference<>();

        String realmId = PassportModelUtils.runJobInTransactionWithResult(session.getPassportSessionFactory(), currentSession -> {
            RealmModel fooRealm = currentSession.realms().createRealm("foo");
            currentSession.getContext().setRealm(fooRealm);
            fooRealm.setDefaultRole(currentSession.roles().addRealmRole(fooRealm, Constants.DEFAULT_ROLES_ROLE_PREFIX  + "-" + fooRealm.getName()));
            fooRealm.setSsoSessionIdleTimeout(1800);
            fooRealm.setSsoSessionMaxLifespan(36000);
            fooRealm.setOfflineSessionIdleTimeout(2592000);
            fooRealm.setOfflineSessionMaxLifespan(5184000);
            fooRealm.addClient("foo-app");
            currentSession.users().addUser(fooRealm, "user3");

            UserSessionModel userSession = currentSession.sessions().createUserSession(null, fooRealm, currentSession.users().getUserByUsername(fooRealm, "user3"), "user3", "127.0.0.1", "form", true, null, null, UserSessionModel.SessionPersistenceState.PERSISTENT);
            userSessionID.set(userSession.getId());

            createClientSession(currentSession, fooRealm.getClientByClientId("foo-app"), userSession, "http://redirect", "state");

            return fooRealm.getId();
        });

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), currentSession -> {
            UserSessionManager sessionManager = new UserSessionManager(currentSession);

            // Persist offline session
            RealmModel fooRealm = currentSession.realms().getRealm(realmId);
            currentSession.getContext().setRealm(fooRealm);
            UserSessionModel userSession = currentSession.sessions().getUserSession(fooRealm, userSessionID.get());
            createOfflineSessionIncludeClientSessions(currentSession, userSession);

            UserSessionModel offlineUserSession = sessionManager.findOfflineUserSession(fooRealm, userSession.getId());
            Assertions.assertEquals(1, offlineUserSession.getAuthenticatedClientSessions().size());
            AuthenticatedClientSessionModel offlineClientSession = offlineUserSession.getAuthenticatedClientSessions().values().iterator().next();
            Assertions.assertEquals("foo-app", offlineClientSession.getClient().getClientId());
            Assertions.assertEquals("user3", offlineClientSession.getUserSession().getUser().getUsername());
        });

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), currentSession -> {
            RealmModel fooRealm = currentSession.realms().getRealm(realmId);
            currentSession.getContext().setRealm(fooRealm);
            RealmManager realmMgr = new RealmManager(currentSession);
            realmMgr.removeRealm(realmMgr.getRealm(realmId));
        });
        
        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), currentSession -> {
            RealmModel fooRealm = currentSession.realms().createRealm(realmId, "foo");
            currentSession.getContext().setRealm(fooRealm);
            fooRealm.setDefaultRole(currentSession.roles().addRealmRole(fooRealm, Constants.DEFAULT_ROLES_ROLE_PREFIX + "-" + fooRealm.getName()));

            fooRealm.addClient("foo-app");
            currentSession.users().addUser(fooRealm, "user3");
        });

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), currentSession -> {
            RealmModel fooRealm = currentSession.realms().getRealm(realmId);
            currentSession.getContext().setRealm(fooRealm);
            Assertions.assertEquals(currentSession.sessions().getOfflineSessionsCount(fooRealm, fooRealm.getClientByClientId("foo-app")), 0);

            // Cleanup
            RealmManager realmMgr = new RealmManager(currentSession);
            realmMgr.removeRealm(realmMgr.getRealm(realmId));
        });
    }

    @TestOnServer
    public void testOnClientRemoved(PassportSession session) {
        AtomicReference<String> userSessionID = new AtomicReference<>();
        String realmId = PassportModelUtils.runJobInTransactionWithResult(session.getPassportSessionFactory(), currentSession -> {
            RealmModel fooRealm = currentSession.realms().createRealm("foo");
            currentSession.getContext().setRealm(fooRealm);
            fooRealm.setDefaultRole(currentSession.roles().addRealmRole(fooRealm, Constants.DEFAULT_ROLES_ROLE_PREFIX + "-" + fooRealm.getName()));
            fooRealm.setSsoSessionIdleTimeout(1800);
            fooRealm.setSsoSessionMaxLifespan(36000);
            fooRealm.setOfflineSessionIdleTimeout(2592000);
            fooRealm.setOfflineSessionMaxLifespan(5184000);

            fooRealm.addClient("foo-app");
            fooRealm.addClient("bar-app");
            currentSession.users().addUser(fooRealm, "user3");

            UserSessionModel userSession = currentSession.sessions().createUserSession(null, fooRealm, currentSession.users().getUserByUsername(fooRealm, "user3"), "user3", "127.0.0.1", "form", true, null, null, UserSessionModel.SessionPersistenceState.PERSISTENT);
            userSessionID.set(userSession.getId());

            createClientSession(currentSession, fooRealm.getClientByClientId("foo-app"), userSession, "http://redirect", "state");
            createClientSession(currentSession, fooRealm.getClientByClientId("bar-app"), userSession, "http://redirect", "state");

            return fooRealm.getId();
        });

        try {
            int started = Time.currentTime();

            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), currentSession -> {
                // Create offline currentSession
                RealmModel fooRealm = currentSession.realms().getRealm(realmId);
                currentSession.getContext().setRealm(fooRealm);
                UserSessionModel userSession = currentSession.sessions().getUserSession(fooRealm, userSessionID.get());
                createOfflineSessionIncludeClientSessions(currentSession, userSession);
            });

            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), currentSession -> {
                RealmManager realmMgr = new RealmManager(currentSession);
                ClientManager clientMgr = new ClientManager(realmMgr);
                RealmModel fooRealm = realmMgr.getRealm(realmId);
                currentSession.getContext().setRealm(fooRealm);

                // Assert currentSession was persisted with both clientSessions
                UserSessionModel offlineSession = currentSession.sessions().getOfflineUserSession(fooRealm, userSessionID.get());
                assertSession(offlineSession, currentSession.users().getUserByUsername(fooRealm, "user3"), "127.0.0.1", started, started);

                // Remove foo-app client
                ClientModel client = fooRealm.getClientByClientId("foo-app");
                clientMgr.removeClient(fooRealm, client);
            });

            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), currentSession -> {
                RealmManager realmMgr = new RealmManager(currentSession);
                ClientManager clientMgr = new ClientManager(realmMgr);
                RealmModel fooRealm = realmMgr.getRealm(realmId);
                currentSession.getContext().setRealm(fooRealm);

                // Assert just one bar-app clientSession persisted now
                UserSessionModel offlineSession = currentSession.sessions().getOfflineUserSession(fooRealm, userSessionID.get());
                Assertions.assertEquals(1, offlineSession.getAuthenticatedClientSessions().size());
                Assertions.assertEquals("bar-app", offlineSession.getAuthenticatedClientSessions().values().iterator().next().getClient().getClientId());

                // Remove bar-app client
                ClientModel client = fooRealm.getClientByClientId("bar-app");
                clientMgr.removeClient(fooRealm, client);
            });

            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), currentSession -> {
                // Assert nothing loaded - userSession was removed as well because it was last userSession
                RealmManager realmMgr = new RealmManager(currentSession);
                RealmModel fooRealm = realmMgr.getRealm(realmId);
                currentSession.getContext().setRealm(fooRealm);
                UserSessionModel offlineSession = currentSession.sessions().getOfflineUserSession(fooRealm, userSessionID.get());
                Assertions.assertEquals(0, offlineSession.getAuthenticatedClientSessions().size());
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), currentSession -> {
                RealmManager realmMgr = new RealmManager(currentSession);
                RealmModel fooRealm = realmMgr.getRealm(realmId);
                currentSession.getContext().setRealm(fooRealm);
                UserModel user3 = currentSession.users().getUserByUsername(fooRealm, "user3");

                // Remove user3
                new UserManager(currentSession).removeUser(fooRealm, user3);

                // Cleanup
                realmMgr = new RealmManager(currentSession);
                realmMgr.removeRealm(realmMgr.getRealm(realmId));
            });
        }
    }

    @TestOnServer
    public void testOnUserRemoved(PassportSession session) {
        AtomicReference<String> userSessionID = new AtomicReference<>();
        String realmId = PassportModelUtils.runJobInTransactionWithResult(session.getPassportSessionFactory(), currentSession -> {
            RealmModel fooRealm = currentSession.realms().createRealm("foo");
            currentSession.getContext().setRealm(fooRealm);
            fooRealm.setDefaultRole(currentSession.roles().addRealmRole(fooRealm, Constants.DEFAULT_ROLES_ROLE_PREFIX + "-" + fooRealm.getName()));
            fooRealm.setSsoSessionIdleTimeout(1800);
            fooRealm.setSsoSessionMaxLifespan(36000);
            fooRealm.setOfflineSessionIdleTimeout(2592000);
            fooRealm.setOfflineSessionMaxLifespan(5184000);
            fooRealm.addClient("foo-app");
            currentSession.users().addUser(fooRealm, "user3");

            UserSessionModel userSession = currentSession.sessions().createUserSession(null, fooRealm, currentSession.users().getUserByUsername(fooRealm, "user3"), "user3", "127.0.0.1", "form", true, null, null, UserSessionModel.SessionPersistenceState.PERSISTENT);
            userSessionID.set(userSession.getId());

            createClientSession(currentSession, fooRealm.getClientByClientId("foo-app"), userSession, "http://redirect", "state");

            return fooRealm.getId();
        });
        try {
            int started = Time.currentTime();


            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), currentSession -> {
                // Create offline session
                RealmModel fooRealm = currentSession.realms().getRealm(realmId);
                currentSession.getContext().setRealm(fooRealm);
                UserSessionModel userSession = currentSession.sessions().getUserSession(fooRealm, userSessionID.get());
                createOfflineSessionIncludeClientSessions(currentSession, userSession);
            });

            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), currentSession -> {
                RealmManager realmMgr = new RealmManager(currentSession);
                RealmModel fooRealm = realmMgr.getRealm(realmId);
                currentSession.getContext().setRealm(fooRealm);
                UserModel user3 = currentSession.users().getUserByUsername(fooRealm, "user3");

                // Assert session was persisted with both clientSessions
                UserSessionModel offlineSession = currentSession.sessions().getOfflineUserSession(fooRealm, userSessionID.get());
                assertSession(offlineSession, user3, "127.0.0.1", started, started);
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), currentSession -> {
                RealmManager realmMgr = new RealmManager(currentSession);
                RealmModel fooRealm = realmMgr.getRealm(realmId);
                currentSession.getContext().setRealm(fooRealm);
                UserModel user3 = currentSession.users().getUserByUsername(fooRealm, "user3");

                // Remove user3
                new UserManager(currentSession).removeUser(fooRealm, user3);

                // Cleanup
                realmMgr = new RealmManager(currentSession);
                realmMgr.removeRealm(fooRealm);
            });
        }
    }

    private static Set<String> createOfflineSessionIncludeClientSessions(PassportSession session, UserSessionModel
            userSession) {
        Set<String> offlineSessions = new HashSet<>();
        UserSessionManager localManager = new UserSessionManager(session);
        for (AuthenticatedClientSessionModel clientSession : userSession.getAuthenticatedClientSessions().values()) {
            localManager.createOrUpdateOfflineSession(clientSession, userSession);
            offlineSessions.add(clientSession.getClient().getId());
        }

        return offlineSessions;
    }

    public static void assertSession(UserSessionModel session, UserModel user, String ipAddress, int started,
                                     int lastRefresh) {
        assertEquals(user.getId(), session.getUser().getId());
        assertEquals(ipAddress, session.getIpAddress());
        assertEquals(user.getUsername(), session.getLoginUsername());
        assertEquals("form", session.getAuthMethod());
        assertTrue(session.isRememberMe());
        assertTrue((session.getStarted() >= started - 1) && (session.getStarted() <= started + 1));
        assertTrue((session.getLastSessionRefresh() >= lastRefresh - 1) && (session.getLastSessionRefresh() <= lastRefresh + 1));

        for (Map.Entry<String, AuthenticatedClientSessionModel> entry : session.getAuthenticatedClientSessions().entrySet()) {
            String clientUUID = entry.getKey();
            AuthenticatedClientSessionModel clientSession = entry.getValue();
            Assertions.assertEquals(clientUUID, clientSession.getClient().getId());
        }
    }

    private static AuthenticatedClientSessionModel createClientSession(PassportSession sessionParam, ClientModel
            client, UserSessionModel userSession, String redirect, String state) {
        AuthenticatedClientSessionModel clientSession = sessionParam.sessions().createClientSession(client.getRealm(), client, userSession);
        clientSession.setRedirectUri(redirect);
        if (state != null) clientSession.setNote(OIDCLoginProtocol.STATE_PARAM, state);
        return clientSession;
    }

    private static UserSessionModel[] createSessions(PassportSession currentSession) {
        UserSessionModel[] sessions = new UserSessionModel[3];
        RealmModel realm = currentSession.getContext().getRealm();
        sessions[0] = currentSession.sessions().createUserSession(null, realm, currentSession.users().getUserByUsername(realm, "user1"), "user1", "127.0.0.1", "form", true, null, null, UserSessionModel.SessionPersistenceState.PERSISTENT);

        createClientSession(currentSession, realm.getClientByClientId("test-app"), sessions[0], "http://redirect", "state");
        createClientSession(currentSession, realm.getClientByClientId("third-party"), sessions[0], "http://redirect", "state");

        sessions[1] = currentSession.sessions().createUserSession(null, realm, currentSession.users().getUserByUsername(realm, "user1"), "user1", "127.0.0.2", "form", true, null, null, UserSessionModel.SessionPersistenceState.PERSISTENT);
        createClientSession(currentSession, realm.getClientByClientId("test-app"), sessions[1], "http://redirect", "state");

        sessions[2] = currentSession.sessions().createUserSession(null, realm, currentSession.users().getUserByUsername(realm, "user2"), "user2", "127.0.0.3", "form", true, null, null, UserSessionModel.SessionPersistenceState.PERSISTENT);
        createClientSession(currentSession, realm.getClientByClientId("test-app"), sessions[2], "http://redirect", "state");

        return sessions;
    }

    public static class UserSessionProviderOfflineRealm implements RealmConfig {

        @Override
        public RealmConfigBuilder configure(RealmConfigBuilder realm) {
            realm.name("test");
            realm.addClient("test-app");
            realm.addClient("third-party");
            realm.addUser("user1").email("user1@localhost");
            realm.addUser("user2").email("user2@localhost");
            return realm;
        }

    }

}
