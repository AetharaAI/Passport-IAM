/*
 * Copyright 2018 Bosch Software Innovations GmbH
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
package org.passport.tests.admin;

import jakarta.ws.rs.core.Response;

import org.passport.admin.client.resource.ClientResource;
import org.passport.admin.client.resource.GroupResource;
import org.passport.admin.client.resource.RealmResource;
import org.passport.admin.client.resource.RoleResource;
import org.passport.common.Profile;
import org.passport.representations.idm.ClientRepresentation;
import org.passport.representations.idm.GroupRepresentation;
import org.passport.representations.idm.ManagementPermissionReference;
import org.passport.representations.idm.ManagementPermissionRepresentation;
import org.passport.representations.idm.RoleRepresentation;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.server.PassportServerConfig;
import org.passport.testframework.server.PassportServerConfigBuilder;
import org.passport.testframework.util.ApiUtil;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:leon.graser@bosch-si.com">Leon Graser</a>
 */
@PassportIntegrationTest(config = ManagementPermissionsTest.ServerConfig.class)
public class ManagementPermissionsTest {

    @InjectRealm
    private ManagedRealm realm;

    @Test
    public void updateGroupPermissions() {
        RealmResource realmResource = realm.admin();
        GroupRepresentation group = new GroupRepresentation();
        group.setName("perm-group-test");
        Response response = realmResource.groups().add(group);
        String id = ApiUtil.getCreatedId(response);
        GroupResource groupResource = realmResource.groups().group(id);

        ManagementPermissionReference result = groupResource.setPermissions(new ManagementPermissionRepresentation(true));
        assertNotNull(result);
        assertTrue(result.isEnabled());
        result = groupResource.getPermissions();
        assertNotNull(result);
        assertTrue(result.isEnabled());

        result = groupResource.setPermissions(new ManagementPermissionRepresentation(false));
        assertNotNull(result);
        assertFalse(result.isEnabled());
        result = groupResource.getPermissions();
        assertNotNull(result);
        assertFalse(result.isEnabled());

        result = groupResource.setPermissions(new ManagementPermissionRepresentation(true));
        assertNotNull(result);
        assertTrue(result.isEnabled());
        result = groupResource.getPermissions();
        assertNotNull(result);
        assertTrue(result.isEnabled());

        result = groupResource.setPermissions(new ManagementPermissionRepresentation(true));
        assertNotNull(result);
        assertTrue(result.isEnabled());
        result = groupResource.getPermissions();
        assertNotNull(result);
        assertTrue(result.isEnabled());

        result = groupResource.setPermissions(new ManagementPermissionRepresentation(false));
        assertNotNull(result);
        assertFalse(result.isEnabled());
        result = groupResource.getPermissions();
        assertNotNull(result);
        assertFalse(result.isEnabled());

        result = groupResource.setPermissions(new ManagementPermissionRepresentation(false));
        assertNotNull(result);
        assertFalse(result.isEnabled());
        result = groupResource.getPermissions();
        assertNotNull(result);
        assertFalse(result.isEnabled());
    }

    @Test
    public void updateClientPermissions() {
        RealmResource realmResource = realm.admin();
        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setName("perm-client-test");
        Response response = realmResource.clients().create(clientRepresentation);
        String id = ApiUtil.getCreatedId(response);
        ClientResource clientResource = realmResource.clients().get(id);

        ManagementPermissionReference result = clientResource.setPermissions(new ManagementPermissionRepresentation(true));
        assertNotNull(result);
        assertTrue(result.isEnabled());
        result = clientResource.getPermissions();
        assertNotNull(result);
        assertTrue(result.isEnabled());

        result = clientResource.setPermissions(new ManagementPermissionRepresentation(false));
        assertNotNull(result);
        assertFalse(result.isEnabled());
        result = clientResource.getPermissions();
        assertNotNull(result);
        assertFalse(result.isEnabled());

        result = clientResource.setPermissions(new ManagementPermissionRepresentation(true));
        assertNotNull(result);
        assertTrue(result.isEnabled());
        result = clientResource.getPermissions();
        assertNotNull(result);
        assertTrue(result.isEnabled());

        result = clientResource.setPermissions(new ManagementPermissionRepresentation(true));
        assertNotNull(result);
        assertTrue(result.isEnabled());
        result = clientResource.getPermissions();
        assertNotNull(result);
        assertTrue(result.isEnabled());

        result = clientResource.setPermissions(new ManagementPermissionRepresentation(false));
        assertNotNull(result);
        assertFalse(result.isEnabled());
        result = clientResource.getPermissions();
        assertNotNull(result);
        assertFalse(result.isEnabled());

        result = clientResource.setPermissions(new ManagementPermissionRepresentation(false));
        assertNotNull(result);
        assertFalse(result.isEnabled());
        result = clientResource.getPermissions();
        assertNotNull(result);
        assertFalse(result.isEnabled());
    }

    @Test
    public void updateRealmRolePermissions() {
        RealmResource realmResource = realm.admin();
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName("perm-role-test");
        realmResource.roles().create(roleRepresentation);
        RoleResource roleResource = realmResource.roles().get("perm-role-test");

        ManagementPermissionReference result = roleResource.setPermissions(new ManagementPermissionRepresentation(true));
        assertNotNull(result);
        assertTrue(result.isEnabled());
        result = roleResource.getPermissions();
        assertNotNull(result);
        assertTrue(result.isEnabled());

        result = roleResource.setPermissions(new ManagementPermissionRepresentation(false));
        assertNotNull(result);
        assertFalse(result.isEnabled());
        result = roleResource.getPermissions();
        assertNotNull(result);
        assertFalse(result.isEnabled());

        result = roleResource.setPermissions(new ManagementPermissionRepresentation(true));
        assertNotNull(result);
        assertTrue(result.isEnabled());
        result = roleResource.getPermissions();
        assertNotNull(result);
        assertTrue(result.isEnabled());

        result = roleResource.setPermissions(new ManagementPermissionRepresentation(true));
        assertNotNull(result);
        assertTrue(result.isEnabled());
        result = roleResource.getPermissions();
        assertNotNull(result);
        assertTrue(result.isEnabled());

        result = roleResource.setPermissions(new ManagementPermissionRepresentation(false));
        assertNotNull(result);
        assertFalse(result.isEnabled());
        result = roleResource.getPermissions();
        assertNotNull(result);
        assertFalse(result.isEnabled());

        result = roleResource.setPermissions(new ManagementPermissionRepresentation(false));
        assertNotNull(result);
        assertFalse(result.isEnabled());
        result = roleResource.getPermissions();
        assertNotNull(result);
        assertFalse(result.isEnabled());
    }

    @Test
    public void updateClientRolePermissions() {
        RealmResource realmResource = realm.admin();
        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setName("perm-client-test");
        Response response = realmResource.clients().create(clientRepresentation);
        String id = ApiUtil.getCreatedId(response);
        ClientResource clientResource = realmResource.clients().get(id);
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName("perm-client-role-test");
        clientResource.roles().create(roleRepresentation);
        RoleResource roleResource = clientResource.roles().get("perm-client-role-test");

        ManagementPermissionReference result = roleResource.setPermissions(new ManagementPermissionRepresentation(true));
        assertNotNull(result);
        assertTrue(result.isEnabled());
        result = roleResource.getPermissions();
        assertNotNull(result);
        assertTrue(result.isEnabled());

        result = roleResource.setPermissions(new ManagementPermissionRepresentation(false));
        assertNotNull(result);
        assertFalse(result.isEnabled());
        result = roleResource.getPermissions();
        assertNotNull(result);
        assertFalse(result.isEnabled());

        result = roleResource.setPermissions(new ManagementPermissionRepresentation(true));
        assertNotNull(result);
        assertTrue(result.isEnabled());
        result = roleResource.getPermissions();
        assertNotNull(result);
        assertTrue(result.isEnabled());

        result = roleResource.setPermissions(new ManagementPermissionRepresentation(true));
        assertNotNull(result);
        assertTrue(result.isEnabled());
        result = roleResource.getPermissions();
        assertNotNull(result);
        assertTrue(result.isEnabled());

        result = roleResource.setPermissions(new ManagementPermissionRepresentation(false));
        assertNotNull(result);
        assertFalse(result.isEnabled());
        result = roleResource.getPermissions();
        assertNotNull(result);
        assertFalse(result.isEnabled());

        result = roleResource.setPermissions(new ManagementPermissionRepresentation(false));
        assertNotNull(result);
        assertFalse(result.isEnabled());
        result = roleResource.getPermissions();
        assertNotNull(result);
        assertFalse(result.isEnabled());
    }

    public static class ServerConfig implements PassportServerConfig {

        @Override
        public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
            return config.features(Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ);
        }

    }

}
