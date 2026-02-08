/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.passport.services.scheduled;

import java.util.concurrent.Callable;

import org.passport.cluster.ClusterProvider;
import org.passport.cluster.ExecutionResult;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.timer.ScheduledTask;

import org.jboss.logging.Logger;

/**
 * Ensures that there are not concurrent executions of same task (either on this host or any other cluster host)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClusterAwareScheduledTaskRunner extends ScheduledTaskRunner {

    private static final Logger logger = Logger.getLogger(ClusterAwareScheduledTaskRunner.class);

    private final int intervalSecs;

    public ClusterAwareScheduledTaskRunner(PassportSessionFactory sessionFactory, ScheduledTask task, long intervalMillis) {
        super(sessionFactory, task);
        this.intervalSecs = (int) (intervalMillis / 1000);
    }

    @Override
    protected void runTask(final PassportSession session) {
        ClusterProvider clusterProvider = session.getProvider(ClusterProvider.class);
        String taskKey = task.getClass().getSimpleName();

        // copying over the value as parent class is in another module that wouldn't allow access from the lambda in Wildfly
        ScheduledTask localTask = this.task;
        ExecutionResult<Void> result = clusterProvider.executeIfNotExecuted(taskKey, intervalSecs, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                localTask.run(session);
                return null;
            }

        });

        if (result.isExecuted()) {
            logger.debugf("Executed scheduled task %s", taskKey);
        } else {
            logger.debugf("Skipped execution of task %s as other cluster node is executing it", taskKey);
        }
    }


}
