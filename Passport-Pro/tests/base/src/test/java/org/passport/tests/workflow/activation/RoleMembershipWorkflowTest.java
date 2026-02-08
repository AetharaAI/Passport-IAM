package org.passport.tests.workflow.activation;

import jakarta.ws.rs.core.Response;

import org.passport.admin.client.resource.UserResource;
import org.passport.admin.client.resource.WorkflowsResource;
import org.passport.models.workflow.SetUserAttributeStepProviderFactory;
import org.passport.models.workflow.events.UserRoleGrantedWorkflowEventFactory;
import org.passport.models.workflow.events.UserRoleRevokedWorkflowEventFactory;
import org.passport.representations.idm.RoleRepresentation;
import org.passport.representations.idm.UserRepresentation;
import org.passport.representations.userprofile.config.UPConfig;
import org.passport.representations.workflows.WorkflowRepresentation;
import org.passport.representations.workflows.WorkflowStepRepresentation;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.RoleConfigBuilder;
import org.passport.testframework.realm.UserConfigBuilder;
import org.passport.testframework.util.ApiUtil;
import org.passport.tests.workflow.AbstractWorkflowTest;
import org.passport.tests.workflow.config.WorkflowsBlockingServerConfig;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * Tests activation of workflows based on user role membership events.
 */
@PassportIntegrationTest(config = WorkflowsBlockingServerConfig.class)
public class RoleMembershipWorkflowTest extends AbstractWorkflowTest {

    private static final String ROLE_NAME = "myrole";

    @Test
    public void testActivateWorkflowOnRoleGrant() {
        UPConfig upConfig = managedRealm.admin().users().userProfile().getConfiguration();
        upConfig.setUnmanagedAttributePolicy(UPConfig.UnmanagedAttributePolicy.ENABLED);
        managedRealm.admin().users().userProfile().update(upConfig);

        // create a test realm role
        managedRealm.admin().roles().create(RoleConfigBuilder.create().name(ROLE_NAME).build());
        RoleRepresentation roleRep = managedRealm.admin().roles().get(ROLE_NAME).toRepresentation();

        // create the workflow that triggers on role grant
        WorkflowRepresentation expectedWorkflow = WorkflowRepresentation.withName("myworkflow")
                .onEvent(UserRoleGrantedWorkflowEventFactory.ID + "(" + ROLE_NAME + ")")
                .withSteps(
                        WorkflowStepRepresentation.create()
                                .of(SetUserAttributeStepProviderFactory.ID)
                                .withConfig("attribute", "attr1")
                                .build()
                ).build();

        WorkflowsResource workflows = managedRealm.admin().workflows();
        try (Response response = workflows.create(expectedWorkflow)) {
            assertThat(response.getStatus(), is(Response.Status.CREATED.getStatusCode()));
        }

        // create a test user and then grant them the role to trigger the workflow
        String userId;
        try (Response response = managedRealm.admin().users().create(UserConfigBuilder.create()
                .username("generic-user").email("generic-user@example.com").build())) {
            userId = ApiUtil.getCreatedId(response);
        }
        UserResource userResource = managedRealm.admin().users().get(userId);
        userResource.roles().realmLevel().add(java.util.List.of(roleRep));

        // verify that the workflow step executed and set the user attribute
        UserRepresentation userRepresentation = userResource.toRepresentation();
        assertThat(userRepresentation.getAttributes().get("attribute"), notNullValue());
        assertThat(userRepresentation.getAttributes().get("attribute").get(0), is("attr1"));
    }

    @Test
    public void testActivateWorkflowOnRoleRevoke() {
        UPConfig upConfig = managedRealm.admin().users().userProfile().getConfiguration();
        upConfig.setUnmanagedAttributePolicy(UPConfig.UnmanagedAttributePolicy.ENABLED);
        managedRealm.admin().users().userProfile().update(upConfig);

        // create a test realm role
        managedRealm.admin().roles().create(RoleConfigBuilder.create().name(ROLE_NAME).build());
        RoleRepresentation roleRep = managedRealm.admin().roles().get(ROLE_NAME).toRepresentation();

        // create the workflow that triggers on role revoke
        WorkflowRepresentation expectedWorkflow = WorkflowRepresentation.withName("myworkflow")
                .onEvent(UserRoleRevokedWorkflowEventFactory.ID + "(" + ROLE_NAME + ")")
                .withSteps(
                        WorkflowStepRepresentation.create()
                                .of(SetUserAttributeStepProviderFactory.ID)
                                .withConfig("attribute", "attr1")
                                .build()
                ).build();

        WorkflowsResource workflows = managedRealm.admin().workflows();
        try (Response response = workflows.create(expectedWorkflow)) {
            assertThat(response.getStatus(), is(Response.Status.CREATED.getStatusCode()));
        }

        // create a test user and then grant them the role - workflow should not trigger right now
        String userId;
        try (Response response = managedRealm.admin().users().create(UserConfigBuilder.create()
                .username("generic-user").email("generic-user@example.com").build())) {
            userId = ApiUtil.getCreatedId(response);
        }
        UserResource userResource = managedRealm.admin().users().get(userId);
        userResource.roles().realmLevel().add(java.util.List.of(roleRep));

        UserRepresentation userRepresentation = userResource.toRepresentation();
        assertThat(userRepresentation.getAttributes(), nullValue());

        // now revoke the role to trigger the workflow
        userResource.roles().realmLevel().remove(java.util.List.of(roleRep));

        // verify that the workflow step executed and set the user attribute
        userRepresentation = userResource.toRepresentation();
        assertThat(userRepresentation.getAttributes().get("attribute"), notNullValue());
        assertThat(userRepresentation.getAttributes().get("attribute").get(0), is("attr1"));
    }
}
