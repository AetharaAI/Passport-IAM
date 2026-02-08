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

package org.passport.tests.admin.event;

import org.passport.admin.client.Passport;
import org.passport.events.admin.OperationType;
import org.passport.events.admin.ResourceType;
import org.passport.models.AdminRoles;
import org.passport.models.Constants;
import org.passport.representations.idm.UserRepresentation;
import org.passport.testframework.annotations.InjectAdminClient;
import org.passport.testframework.annotations.InjectAdminEvents;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.events.AdminEventAssertion;
import org.passport.testframework.events.AdminEvents;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.RealmConfig;
import org.passport.testframework.realm.RealmConfigBuilder;
import org.passport.tests.utils.admin.AdminApiUtil;
import org.passport.tests.utils.admin.AdminEventPaths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

/**
 * Test authDetails in admin events
 * 
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@PassportIntegrationTest
public class AdminEventAuthDetailsTest {

    @InjectRealm(config = AdminEventsAuthDetailsRealmConfig.class)
    ManagedRealm managedRealm;

    @InjectAdminClient(ref = "admin", mode = InjectAdminClient.Mode.MANAGED_REALM, client = "client", user = "admin")
    Passport adminClient;

    @InjectAdminEvents
    AdminEvents adminEvents;

    private String clientUuid;
    private String adminId;
    private String appUserId;
    private String adminCliUuid;

    @BeforeEach
    public void initConfig() {
        clientUuid = adminClient.realm(managedRealm.getName()).clients().findByClientId("client").get(0).getId();
        adminId = adminClient.realm(managedRealm.getName()).users().search("admin", true).get(0).getId();
        appUserId = adminClient.realm(managedRealm.getName()).users().search("app-user", true).get(0).getId();
        adminCliUuid = AdminApiUtil.findClientByClientId(managedRealm.admin(), Constants.ADMIN_CLI_CLIENT_ID).toRepresentation().getId();
    }

    @Test
    public void testAuthSuccess() {
        testClient(adminClient, appUserId, managedRealm.getId(), clientUuid, adminId);
    }

    @Test
    public void testAuthFailure() {
        Assertions.assertThrows(AssertionFailedError.class, () -> testClient(adminClient, appUserId, managedRealm.getId(), adminCliUuid, adminId));
    }

    private void testClient(Passport admin, String userId, String expectedRealmId, String expectedClientUuid, String expectedUserId) {
        UserRepresentation rep = admin.realm(managedRealm.getName()).users().get(userId).toRepresentation();
        rep.setEmail("foo@email.org");
        admin.realm(managedRealm.getName()).users().get(userId).update(rep);

        AdminEventAssertion.assertEvent(adminEvents.poll(), OperationType.UPDATE, AdminEventPaths.userResourcePath(userId), rep, ResourceType.USER)
                .auth(expectedRealmId, expectedClientUuid, expectedUserId);
    }

    public static class AdminEventsAuthDetailsRealmConfig implements RealmConfig {

        @Override
        public RealmConfigBuilder configure(RealmConfigBuilder realm) {
            realm.addClient("client").name("client").publicClient(true).directAccessGrantsEnabled(true);
            realm.addUser("admin").password("password").name("My", "Admin").email("admin@localhost")
                    .emailVerified(true).clientRoles(Constants.REALM_MANAGEMENT_CLIENT_ID, AdminRoles.REALM_ADMIN);
            realm.addUser("app-user").password("password");

            return realm;
        }
    }
}
