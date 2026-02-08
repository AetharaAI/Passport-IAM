package org.passport.tests.admin;

import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

import org.passport.admin.client.Passport;
import org.passport.admin.client.resource.RealmResource;
import org.passport.admin.client.resource.RoleMappingResource;
import org.passport.models.AdminRoles;
import org.passport.models.Constants;
import org.passport.representations.idm.GroupRepresentation;
import org.passport.representations.idm.RoleRepresentation;
import org.passport.representations.idm.UserRepresentation;
import org.passport.testframework.admin.AdminClientFactory;
import org.passport.testframework.annotations.InjectAdminClient;
import org.passport.testframework.annotations.InjectAdminClientFactory;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.GroupConfigBuilder;
import org.passport.testframework.realm.UserConfigBuilder;
import org.passport.testframework.util.ApiUtil;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@PassportIntegrationTest
public class AdminEndpointAccessibilityTest {

    @InjectAdminClient
    Passport adminClient;

    @InjectAdminClientFactory
    AdminClientFactory adminClientFactory;

    /**
     * Verifies that the user does not have access to Passport Admin endpoint when role is not
     * assigned to that user.
     *
     * @link https://issues.jboss.org/browse/PASSPORT-2964
     */
    @Test
    public void noAdminEndpointAccessWhenNoRoleAssigned() {
        String userName = "user-" + UUID.randomUUID();
        UserRepresentation user = UserConfigBuilder.create()
                .username(userName)
                .password("pwd")
                .build();
        final String realmName = "master";
        final String userUuid = ApiUtil.getCreatedId(adminClient.realm(realmName).users().create(user));

        Passport userClient = adminClientFactory.create().realm(realmName)
                .username(userName).password("pwd")
                .clientId(Constants.ADMIN_CLI_CLIENT_ID)
                .build();
        ClientErrorException e = Assertions.assertThrows(ClientErrorException.class,
                () -> userClient.realms().findAll()  // Any admin operation will do
        );
        assertThat(e.getMessage(), containsString(String.valueOf(Response.Status.FORBIDDEN.getStatusCode())));
        adminClient.realm(realmName).users().get(userUuid).remove();
    }

    /**
     * Verifies that the role assigned to a user is correctly handled by Passport Admin endpoint.
     *
     * @link https://issues.jboss.org/browse/PASSPORT-2964
     */
    @Test
    public void adminEndpointAccessibleWhenAdminRoleAssignedToUser() {
        String userName = "user-" + UUID.randomUUID();
        UserRepresentation user = UserConfigBuilder.create()
                .username(userName)
                .password("pwd")
                .build();

        final String realmName = "master";
        RealmResource realm = adminClient.realms().realm(realmName);
        RoleRepresentation adminRole = realm.roles().get(AdminRoles.ADMIN).toRepresentation();
        assertThat(adminRole, notNullValue());
        assertThat(adminRole.getId(), notNullValue());

        final String userUuid = ApiUtil.getCreatedId(adminClient.realm(realmName).users().create(user));
        assertThat(userUuid, notNullValue());

        RoleMappingResource mappings = realm.users().get(userUuid).roles();
        mappings.realmLevel().add(List.of(adminRole));

        Passport userClient = adminClientFactory.create().realm(realmName)
                .username(userName).password("pwd")
                .clientId(Constants.ADMIN_CLI_CLIENT_ID)
                .build();

        assertFalse(userClient.realms().findAll().isEmpty()); // Any admin operation will do
        adminClient.realm(realmName).users().get(userUuid).remove();
    }

    /**
     * Verifies that the role assigned to a user's group is correctly handled by Passport Admin endpoint.
     *
     * @link https://issues.jboss.org/browse/PASSPORT-2964
     */
    @Test
    public void adminEndpointAccessibleWhenAdminRoleAssignedToGroup() {
        String userName = "user-" + UUID.randomUUID();
        String groupName = "group-" + UUID.randomUUID();

        final String realmName = "master";
        RealmResource realm = adminClient.realms().realm(realmName);
        RoleRepresentation adminRole = realm.roles().get(AdminRoles.ADMIN).toRepresentation();
        assertThat(adminRole, notNullValue());
        assertThat(adminRole.getId(), notNullValue());

        UserRepresentation user = UserConfigBuilder.create()
                .username(userName)
                .password("pwd")
                .build();
        final String userUuid = ApiUtil.getCreatedId(adminClient.realm(realmName).users().create(user));
        assertThat(userUuid, notNullValue());

        GroupRepresentation group = GroupConfigBuilder.create().name(groupName).build();
        Response response = realm.groups().add(group);
        String groupId = ApiUtil.getCreatedId(response);

        RoleMappingResource mappings = realm.groups().group(groupId).roles();
        mappings.realmLevel().add(List.of(adminRole));

        realm.users().get(userUuid).joinGroup(groupId);

        Passport userClient = adminClientFactory.create().realm(realmName)
                .username(userName).password("pwd")
                .clientId(Constants.ADMIN_CLI_CLIENT_ID)
                .build();
        assertFalse(userClient.realms().findAll().isEmpty()); // Any admin operation will do

        adminClient.realm(realmName).groups().group(groupId).remove();
        adminClient.realm(realmName).users().get(userUuid).remove();
    }

    /**
     * Verifies that the role assigned to a user's group is correctly handled by Passport Admin endpoint.
     *
     * @link https://issues.jboss.org/browse/PASSPORT-2964
     */
    @Test
    public void adminEndpointAccessibleWhenAdminRoleAssignedToGroupAfterUserJoinedIt() {
        String userName = "user-" + UUID.randomUUID();
        String groupName = "group-" + UUID.randomUUID();
        final String realmName = "master";

        RealmResource realm = adminClient.realms().realm(realmName);
        RoleRepresentation adminRole = realm.roles().get(AdminRoles.ADMIN).toRepresentation();
        assertThat(adminRole, notNullValue());
        assertThat(adminRole.getId(), notNullValue());

        UserRepresentation user = UserConfigBuilder.create()
                .username(userName)
                .password("pwd")
                .build();
        final String userUuid = ApiUtil.getCreatedId(adminClient.realm(realmName).users().create(user));
        assertThat(userUuid, notNullValue());

        GroupRepresentation group = GroupConfigBuilder.create().name(groupName).build();
        Response response = realm.groups().add(group);
        String groupId = ApiUtil.getCreatedId(response);

        realm.users().get(userUuid).joinGroup(groupId);

        RoleMappingResource mappings = realm.groups().group(groupId).roles();

        mappings.realmLevel().add(List.of(adminRole));

        Passport userClient = adminClientFactory.create().realm(realmName)
                .username(userName).password("pwd")
                .clientId(Constants.ADMIN_CLI_CLIENT_ID)
                .build();
        assertFalse(userClient.realms().findAll().isEmpty()); // Any admin operation will do

        adminClient.realm(realmName).groups().group(groupId).remove();
        adminClient.realm(realmName).users().get(userUuid).remove();
    }

}
