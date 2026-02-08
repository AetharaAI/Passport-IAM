package org.passport.tests.workflow.step;

import java.time.Duration;

import org.passport.models.UserModel;
import org.passport.models.workflow.RemoveRequiredActionStepProvider;
import org.passport.models.workflow.RemoveRequiredActionStepProviderFactory;
import org.passport.models.workflow.events.UserCreatedWorkflowEventFactory;
import org.passport.representations.workflows.WorkflowRepresentation;
import org.passport.representations.workflows.WorkflowStepRepresentation;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.UserConfigBuilder;
import org.passport.tests.workflow.AbstractWorkflowTest;
import org.passport.tests.workflow.config.WorkflowsBlockingServerConfig;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * Tests the execution of the 'remove-required-action' workflow step.
 */
@PassportIntegrationTest(config = WorkflowsBlockingServerConfig.class)
public class RemoveRequiredActionTest extends AbstractWorkflowTest {

    @Test
    public void testStepRun() {
        managedRealm.admin().workflows().create(WorkflowRepresentation.withName("remove-action-workflow")
                .onEvent(UserCreatedWorkflowEventFactory.ID)
                .withSteps(
                        WorkflowStepRepresentation.create()
                                .of(RemoveRequiredActionStepProviderFactory.ID)
                                .withConfig(RemoveRequiredActionStepProvider.REQUIRED_ACTION_KEY, "UPDATE_PASSWORD")
                                .build()
                ).build()).close();

        managedRealm.admin().users().create(UserConfigBuilder.create()
                .username("testuser_remove")
                .requiredActions(UserModel.RequiredAction.UPDATE_PASSWORD.name())
                .build()).close();

        Awaitility.await()
                .timeout(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    var users = managedRealm.admin().users().search("testuser_remove");
                    assertThat(users, hasSize(1));
                    var userRepresentation = users.get(0);
                    Assertions.assertTrue(userRepresentation.getRequiredActions() == null || userRepresentation.getRequiredActions().isEmpty());
                });
    }
}
