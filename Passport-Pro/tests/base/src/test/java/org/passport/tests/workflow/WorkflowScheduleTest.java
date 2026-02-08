package org.passport.tests.workflow;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.passport.admin.client.resource.UserResource;
import org.passport.models.workflow.SetUserAttributeStepProviderFactory;
import org.passport.representations.workflows.WorkflowRepresentation;
import org.passport.representations.workflows.WorkflowScheduleRepresentation;
import org.passport.representations.workflows.WorkflowStepRepresentation;
import org.passport.testframework.annotations.InjectUser;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.realm.ManagedUser;
import org.passport.testframework.realm.UserConfig;
import org.passport.testframework.realm.UserConfigBuilder;
import org.passport.tests.workflow.config.WorkflowsBlockingServerConfig;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.notNullValue;

@PassportIntegrationTest(config = WorkflowsBlockingServerConfig.class)
public class WorkflowScheduleTest extends AbstractWorkflowTest {

    @InjectUser(ref = "alice", config = DefaultUserConfig.class, lifecycle = LifeCycle.METHOD, realmRef = DEFAULT_REALM_NAME)
    private ManagedUser userAlice;

    @Test
    public void testSchedule() {
        WorkflowRepresentation expectedWorkflow = WorkflowRepresentation.withName("myworkflow")
                .schedule(WorkflowScheduleRepresentation.create().after("1s").batchSize(10).build())
                .withSteps(
                        WorkflowStepRepresentation.create().of(SetUserAttributeStepProviderFactory.ID)
                                .withConfig("test", "test")
                                .build()
                ).build();

        managedRealm.admin().workflows().create(expectedWorkflow).close();

        Awaitility.await()
                .timeout(Duration.ofSeconds(15))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    UserResource user = managedRealm.admin().users().get(userAlice.getId());
                    Map<String, List<String>> attributes = user.getUnmanagedAttributes();
                    assertThat(attributes, notNullValue());
                    assertThat(attributes.get("test"), containsInAnyOrder("test"));
                });
    }

    private static class DefaultUserConfig implements UserConfig {

        @Override
        public UserConfigBuilder configure(UserConfigBuilder user) {
            user.username("alice");
            user.password("alice");
            user.name("alice", "alice");
            user.email("master-admin@email.org");
            return user;
        }
    }
}
