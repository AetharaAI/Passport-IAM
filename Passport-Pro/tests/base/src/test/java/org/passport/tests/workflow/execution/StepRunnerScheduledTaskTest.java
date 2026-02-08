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

package org.passport.tests.workflow.execution;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.passport.admin.client.Passport;
import org.passport.admin.client.resource.RealmResource;
import org.passport.common.util.Time;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.UserProvider;
import org.passport.models.workflow.DisableUserStepProviderFactory;
import org.passport.models.workflow.SetUserAttributeStepProviderFactory;
import org.passport.models.workflow.WorkflowStepRunnerSuccessEvent;
import org.passport.models.workflow.events.UserCreatedWorkflowEventFactory;
import org.passport.provider.ProviderEventListener;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.representations.workflows.WorkflowRepresentation;
import org.passport.representations.workflows.WorkflowStepRepresentation;
import org.passport.storage.UserStoragePrivateUtil;
import org.passport.testframework.annotations.InjectAdminClient;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.UserConfigBuilder;
import org.passport.tests.workflow.AbstractWorkflowTest;
import org.passport.tests.workflow.config.WorkflowsScheduledTaskServerConfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@PassportIntegrationTest(config = WorkflowsScheduledTaskServerConfig.class)
public class StepRunnerScheduledTaskTest extends AbstractWorkflowTest {

    @InjectAdminClient(mode = InjectAdminClient.Mode.BOOTSTRAP, realmRef = DEFAULT_REALM_NAME)
    Passport adminClient;

    @Test
    public void testStepRunnerScheduledTask() {
        for (int i = 0; i < 2; i++) {
            RealmRepresentation realm = new RealmRepresentation();

            realm.setRealm(DEFAULT_REALM_NAME.concat("-").concat(String.valueOf(i)));
            realm.setEnabled(true);

            adminClient.realms().create(realm);

            assertStepRuns(realm.getRealm());
        }
    }

    private void assertStepRuns(String realmName) {
        RealmResource realm = adminClient.realm(realmName);

        realm.workflows().create(WorkflowRepresentation.withName("myworkflow")
                .onEvent(UserCreatedWorkflowEventFactory.ID)
                .withSteps(
                        WorkflowStepRepresentation.create().of(SetUserAttributeStepProviderFactory.ID)
                                .after(Duration.ofDays(5))
                                .withConfig("message", "message")
                                .build(),
                        WorkflowStepRepresentation.create().of(DisableUserStepProviderFactory.ID)
                                .after(Duration.ofDays(5))
                                .build()
                ).build()).close();

        realm.users().create(UserConfigBuilder.create()
                .username("alice")
                .email("alice@passport-pro.ai")
                .name("alice", "wonderland")
                .build())
                .close();

        runOnServer.run((session -> {
            PassportSessionFactory sessionFactory = session.getPassportSessionFactory();
            CountDownLatch count = new CountDownLatch(2);

            ProviderEventListener listener = event -> {
                if (event instanceof WorkflowStepRunnerSuccessEvent e) {
                    PassportSession s = e.session();
                    RealmModel r = s.getContext().getRealm();

                    if (!realmName.equals(r.getName())) {
                        return;
                    }

                    UserProvider provider = UserStoragePrivateUtil.userLocalStorage(s);
                    UserModel user = provider.getUserByUsername(r, "alice");
                    if (user.isEnabled() && user.getAttributes().containsKey("message")) {
                        // notified
                        count.countDown();
                        // force execution of next step
                        user.removeAttribute("message");
                        Time.setOffset(Math.toIntExact(Duration.ofDays(20).toSeconds()));
                    } else if (!user.isEnabled()) {
                        // disabled
                        count.countDown();
                    }
                }
            };

            try {
                sessionFactory.register(listener);
                Time.setOffset(Math.toIntExact(Duration.ofDays(12).toSeconds()));
                System.out.println("Waiting for steps to be run for realm " + realmName);
                assertTrue(count.await(15, TimeUnit.SECONDS));
                System.out.println("... steps run for realm " + realmName);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                sessionFactory.unregister(listener);
                Time.setOffset(0);
            }
        }));
    }
}
