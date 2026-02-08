package org.passport.tests.workflow.activation;

import jakarta.ws.rs.core.Response;

import org.passport.admin.client.resource.UserResource;
import org.passport.models.workflow.SetUserAttributeStepProviderFactory;
import org.passport.models.workflow.events.UserCreatedWorkflowEventFactory;
import org.passport.representations.idm.UserRepresentation;
import org.passport.representations.userprofile.config.UPConfig;
import org.passport.representations.workflows.WorkflowRepresentation;
import org.passport.representations.workflows.WorkflowStepRepresentation;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.UserConfigBuilder;
import org.passport.testframework.util.ApiUtil;
import org.passport.tests.workflow.AbstractWorkflowTest;
import org.passport.tests.workflow.config.WorkflowsBlockingServerConfig;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Tests activation of workflows based on user creation events.
 */
@PassportIntegrationTest(config = WorkflowsBlockingServerConfig.class)
public class UserCreationWorkflowTest extends AbstractWorkflowTest {

    @Test
    public void testActivateWorkflowOnUserCreation() {
        UPConfig upConfig = managedRealm.admin().users().userProfile().getConfiguration();
        upConfig.setUnmanagedAttributePolicy(UPConfig.UnmanagedAttributePolicy.ENABLED);
        managedRealm.admin().users().userProfile().update(upConfig);

        // create the workflow that triggers on user creation
        WorkflowRepresentation workflow = WorkflowRepresentation.withName("myworkflow")
                .onEvent(UserCreatedWorkflowEventFactory.ID)
                .withSteps(
                        WorkflowStepRepresentation.create()
                                .of(SetUserAttributeStepProviderFactory.ID)
                                .withConfig("attribute", "attr1")
                                .build()
                ).build();
        managedRealm.admin().workflows().create(workflow).close();

        // create a test user to trigger the workflow
        String userId;
        try (Response response = managedRealm.admin().users().create(UserConfigBuilder.create()
                .username("generic-user").email("generic-user@example.com").build())) {
            userId = ApiUtil.getCreatedId(response);
        }
        UserResource userResource = managedRealm.admin().users().get(userId);
        UserRepresentation representation = userResource.toRepresentation();

        // assert that the workflow step ran and set the user attribute
        assertThat(representation.getAttributes(), notNullValue());
        assertThat(representation.getAttributes().get("attribute"), notNullValue());
        assertThat(representation.getAttributes().get("attribute").get(0), is("attr1"));
    }
}
