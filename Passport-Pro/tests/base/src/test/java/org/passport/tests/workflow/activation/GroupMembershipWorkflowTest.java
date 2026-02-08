package org.passport.tests.workflow.activation;

import java.time.Duration;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.passport.admin.client.resource.UserResource;
import org.passport.admin.client.resource.WorkflowsResource;
import org.passport.models.workflow.SetUserAttributeStepProviderFactory;
import org.passport.models.workflow.events.UserGroupMembershipAddedWorkflowEventFactory;
import org.passport.models.workflow.events.UserGroupMembershipRemovedWorkflowEventFactory;
import org.passport.representations.idm.UserRepresentation;
import org.passport.representations.userprofile.config.UPConfig;
import org.passport.representations.userprofile.config.UPConfig.UnmanagedAttributePolicy;
import org.passport.representations.workflows.WorkflowRepresentation;
import org.passport.representations.workflows.WorkflowStepRepresentation;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.GroupConfigBuilder;
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
 * Tests activation of workflows based on user group membership events.
 */
@PassportIntegrationTest(config = WorkflowsBlockingServerConfig.class)
public class GroupMembershipWorkflowTest extends AbstractWorkflowTest {

    private static final String GROUP_NAME = "generic-group";

    /**
     * Test that a workflow activating on user group membership addition is correctly triggered when a user joins the correct group.
     */
    @Test
    public void testActivateWorkflowOnGroupMembershipJoin() {
        UPConfig upConfig = managedRealm.admin().users().userProfile().getConfiguration();
        upConfig.setUnmanagedAttributePolicy(UnmanagedAttributePolicy.ENABLED);
        managedRealm.admin().users().userProfile().update(upConfig);
        String groupId;

        try (Response response = managedRealm.admin().groups().add(GroupConfigBuilder.create()
                .name("generic-group").build())) {
            groupId = ApiUtil.getCreatedId(response);
        }

        WorkflowRepresentation expectedWorkflow = WorkflowRepresentation.withName("myworkflow")
                .onEvent(UserGroupMembershipAddedWorkflowEventFactory.ID + "(" + GROUP_NAME + ")")
                .withSteps(
                        WorkflowStepRepresentation.create()
                                .of(SetUserAttributeStepProviderFactory.ID)
                                .withConfig("attribute", "attr1")
                                .after(Duration.ofDays(5))
                                .build()
                ).build();

        WorkflowsResource workflows = managedRealm.admin().workflows();

        try (Response response = workflows.create(expectedWorkflow)) {
            assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));
        }

        String userId;
        try (Response response = managedRealm.admin().users().create(UserConfigBuilder.create()
                .username("generic-user").email("generic-user@example.com").build())) {
            userId = ApiUtil.getCreatedId(response);
        }

        UserResource userResource = managedRealm.admin().users().get(userId);
        userResource.joinGroup(groupId);

        // set offset to 6 days - notify step should run now
        runScheduledSteps(Duration.ofDays(6));

        UserRepresentation rep = userResource.toRepresentation();
        assertThat(rep.getAttributes().get("attribute"), notNullValue());
        assertThat(rep.getAttributes().get("attribute").get(0), is("attr1"));
    }

    /**
     * Test that a workflow activating on user group membership removal is correctly triggered when a user leaves a group.
     */
    @Test
    public void testActivateWorkflowOnGroupMembershipLeave() {
        UPConfig upConfig = managedRealm.admin().users().userProfile().getConfiguration();
        upConfig.setUnmanagedAttributePolicy(UPConfig.UnmanagedAttributePolicy.ENABLED);
        managedRealm.admin().users().userProfile().update(upConfig);
        String groupId;

        // create a test group
        try (Response response = managedRealm.admin().groups().add(GroupConfigBuilder.create().name(GROUP_NAME).build())) {
            groupId = ApiUtil.getCreatedId(response);
        }

        WorkflowRepresentation expectedWorkflow = WorkflowRepresentation.withName("myworkflow")
                .onEvent(UserGroupMembershipRemovedWorkflowEventFactory.ID + "(" + GROUP_NAME + ")")
                .withSteps(
                        WorkflowStepRepresentation.create()
                                .of(SetUserAttributeStepProviderFactory.ID)
                                .withConfig("attribute", "attr1")
                                .after(Duration.ofDays(5))
                                .build()
                ).build();

        // create the workflow that activates on group membership removal
        WorkflowsResource workflows = managedRealm.admin().workflows();
        try (Response response = workflows.create(expectedWorkflow)) {
            assertThat(response.getStatus(), is(Response.Status.CREATED.getStatusCode()));
        }

        // now create a user and add them to the group
        String userId;
        try (Response response = managedRealm.admin().users().create(UserConfigBuilder.create()
                .username("generic-user").email("generic-user@example.com").build())) {
            userId = ApiUtil.getCreatedId(response);
        }
        UserResource userResource = managedRealm.admin().users().get(userId);
        userResource.joinGroup(groupId);

        // set offset to 6 days - no steps should run as the workflow shouldn't have activated yet
        runScheduledSteps(Duration.ofDays(6));
        UserRepresentation rep = userResource.toRepresentation();
        assertThat(rep.getAttributes(), nullValue());

        // now remove the user from the group - this should trigger the workflow
        userResource.leaveGroup(groupId);
        // set offset to 6 days - set attribute step should run now
        runScheduledSteps(Duration.ofDays(6));
        rep = userResource.toRepresentation();
        assertThat(rep.getAttributes(), notNullValue());
        assertThat(rep.getAttributes().get("attribute").get(0), is("attr1"));
    }

}
