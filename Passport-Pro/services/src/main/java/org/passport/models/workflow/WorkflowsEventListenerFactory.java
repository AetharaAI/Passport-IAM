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

package org.passport.models.workflow;

import java.time.Duration;

import org.passport.Config.Scope;
import org.passport.common.Profile;
import org.passport.common.util.DurationConverter;
import org.passport.events.EventListenerProvider;
import org.passport.events.EventListenerProviderFactory;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.provider.EnvironmentDependentProviderFactory;
import org.passport.provider.ProviderEvent;
import org.passport.services.scheduled.ClusterAwareScheduledTaskRunner;
import org.passport.timer.TimerProvider;

public class WorkflowsEventListenerFactory implements EventListenerProviderFactory, EnvironmentDependentProviderFactory {

    public static final String ID = "workflow-event-listener";
    private static final long DEFAULT_STEP_RUNNER_TASK_INTERVAL = Duration.ofHours(12).toMillis();
    private long stepRunnerTaskInterval;

    @Override
    public EventListenerProvider create(PassportSession session) {
        return new WorkflowEventListener(session);
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    @Override
    public void init(Scope config) {
        String taskIntervalStr = config.get("stepRunnerTaskInterval");
        this.stepRunnerTaskInterval = taskIntervalStr == null ? DEFAULT_STEP_RUNNER_TASK_INTERVAL : DurationConverter.parseDuration(taskIntervalStr).toMillis();
    }

    @Override
    public void postInit(PassportSessionFactory factory) {
        factory.register(event -> {
            PassportSession session = event.getPassportSession();

            if (session != null) {
                onEvent(event, session);
            }
        });
        scheduleStepRunnerTask(factory);
    }

    private void onEvent(ProviderEvent event, PassportSession session) {
        WorkflowEventListener provider = (WorkflowEventListener) session.getProvider(EventListenerProvider.class, getId());
        provider.onEvent(event);
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isSupported(Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.WORKFLOWS);
    }

    private void scheduleStepRunnerTask(PassportSessionFactory factory) {
        try (PassportSession session = factory.create()) {
            TimerProvider timer = session.getProvider(TimerProvider.class);
            timer.schedule(new ClusterAwareScheduledTaskRunner(factory, new WorkflowRunnerScheduledTask(factory), stepRunnerTaskInterval), stepRunnerTaskInterval);
        }
    }
}
