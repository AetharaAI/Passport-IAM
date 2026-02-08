/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.passport.tests.admin.authz.fgap;

import java.util.List;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;

import org.passport.admin.client.Passport;
import org.passport.common.Profile.Feature;
import org.passport.models.AdminRoles;
import org.passport.models.Constants;
import org.passport.representations.idm.ClientRepresentation;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.representations.idm.RoleRepresentation;
import org.passport.representations.idm.UserRepresentation;
import org.passport.testframework.admin.AdminClientFactory;
import org.passport.testframework.annotations.InjectAdminClient;
import org.passport.testframework.annotations.InjectAdminClientFactory;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.UserConfigBuilder;
import org.passport.testframework.server.PassportServerConfig;
import org.passport.testframework.server.PassportServerConfigBuilder;
import org.passport.testframework.util.ApiUtil;
import org.passport.tests.admin.authz.fgap.RealmAdminAccessTest.ServerConfig;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.fail;

@PassportIntegrationTest(config = ServerConfig.class)
public class RealmAdminAccessTest extends AbstractPermissionTest {

    @InjectAdminClient(mode = InjectAdminClient.Mode.MANAGED_REALM, client = "myclient", user = "myadmin")
    Passport realmAdminClient;

    @InjectAdminClientFactory
    AdminClientFactory adminClientFactory;

    @Test
    public void testRealmAdminAccess() {
        UserRepresentation myadmin = realm.admin().users().search("myadmin").get(0);
        ClientRepresentation realmManagement = realm.admin().clients().findByClientId("realm-management").get(0);
        RoleRepresentation realmAdminRole = realm.admin().clients().get(realmManagement.getId()).roles().get(AdminRoles.REALM_ADMIN).toRepresentation();
        realm.admin().users().get(myadmin.getId()).roles().clientLevel(realmManagement.getId()).add(List.of(realmAdminRole));

        assertThat(realmAdminClient.realm(realm.getName()).users().search("myadmin"), is(not(empty())));
        assertThat(realmAdminClient.realm(realm.getName()).clients().findAll(), is(not(empty())));
        RealmRepresentation realmRep = realmAdminClient.realm(realm.getName()).toRepresentation();

        realmRep.setAdminPermissionsEnabled(!realmRep.isAdminPermissionsEnabled());
        realmAdminClient.realm(realmRep.getRealm()).update(realmRep);
        realmRep.setAdminPermissionsEnabled(!realmRep.isAdminPermissionsEnabled());
        realmAdminClient.realm(realmRep.getRealm()).update(realmRep);

        try {
            assertThat(realmAdminClient.realm("master").clients().findAll(), is(not(empty())));
            fail("Should not have access to other realm");
        } catch (ForbiddenException ignore) {
        }

        RealmRepresentation myrealm = new RealmRepresentation();
        myrealm.setRealm("myrealm");
        myrealm.setEnabled(true);

        try {
            realmAdminClient.realms().create(myrealm);
            fail("Should not have access to create realms");
        } catch (ForbiddenException ignore) {
        }

        try (Passport client = adminClientFactory.create().realm("master")
                .username("admin").password("admin").clientId(Constants.ADMIN_CLI_CLIENT_ID).build()) {
            try {
                Assertions.assertNotNull(client.serverInfo().getInfo());
                client.realms().create(myrealm);

                assertThat(realmAdminClient.realms().findAll(), hasSize(1));
                assertThat(realmAdminClient.realms().findAll().get(0).getRealm(), is(realm.getName()));

                try {
                    realmAdminClient.realm(myrealm.getRealm()).remove();
                    fail("Should not have access to other realm");
                } catch (ForbiddenException ignore) {
                }

                try {
                    assertThat(realmAdminClient.realm(myrealm.getRealm()).users().search(null), is(not(empty())));
                    fail("Should not have access to other realm");
                } catch (ForbiddenException ignore) {
                }

                try {
                    assertThat(realmAdminClient.realm(myrealm.getRealm()).clients().findAll(), is(not(empty())));
                    fail("Should not have access to other realm");
                } catch (ForbiddenException ignore) {
                }

                assertWorkflowAccess(client);
            } finally {
                client.realm(myrealm.getRealm()).remove();
            }
        }
    }

    private void assertWorkflowAccess(Passport serverAdminClient) {
        // server admin can access workflows
        serverAdminClient.realm(realm.getName()).workflows().list();

        UserRepresentation myadmin = realm.admin().users().search("myadmin").get(0);
        ClientRepresentation realmManagement = realm.admin().clients().findByClientId("realm-management").get(0);
        RoleRepresentation realmAdminRole = realm.admin().clients().get(realmManagement.getId()).roles().get(AdminRoles.REALM_ADMIN).toRepresentation();

        // can access workflows with realm-admin role
        realm.admin().users().get(myadmin.getId()).roles().clientLevel(realmManagement.getId()).add(List.of(realmAdminRole));
        realmAdminClient.realm(realm.getName()).workflows().list();

        // cannot access workflows without realm-admin role
        realm.admin().users().get(myadmin.getId()).roles().clientLevel(realmManagement.getId()).remove(List.of(realmAdminRole));

        try {
            realmAdminClient.realm(realm.getName()).workflows().list();
            fail("Should not have access to workflows");
        } catch (ForbiddenException ignore) {
        }

        UserRepresentation masterUserRealmAdmin = UserConfigBuilder.create()
                .username("mymasteradmin")
                .password("password")
                .firstName("f")
                .lastName("l")
                .email("mymasteradmin@passport-pro.ai")
                .build();
        try (Response response = serverAdminClient.realm("master").users().create(masterUserRealmAdmin)) {
            masterUserRealmAdmin.setId(ApiUtil.getCreatedId(response));
        }

        ClientRepresentation myRealmMasterClient = serverAdminClient.realm("master").clients().findByClientId(realm.getName() + "-realm").get(0);
        RoleRepresentation masterRealmAdminRole = serverAdminClient.realm("master").clients().get(myRealmMasterClient.getId())
                .roles().get(AdminRoles.MANAGE_REALM).toRepresentation();
        serverAdminClient.realm("master").users().get(masterUserRealmAdmin.getId())
                .roles().clientLevel(myRealmMasterClient.getId()).add(List.of(masterRealmAdminRole));
        try (Passport masterRealmAdminClient = adminClientFactory.create().realm("master")
                .username("mymasteradmin").password("password").clientId(Constants.ADMIN_CLI_CLIENT_ID).build()) {

            // can not access workflows with manage-realm role in master realm
            try {
                masterRealmAdminClient.realm(realm.getName()).workflows().list();
                fail("Should not have access to manage workflows if user is master realm admin with manage-realm role in a realm");
            } catch (ForbiddenException ignore) {}
        }
    }

    public static class ServerConfig implements PassportServerConfig {

        @Override
        public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
            return config.features(Feature.WORKFLOWS);
        }
    }
}
