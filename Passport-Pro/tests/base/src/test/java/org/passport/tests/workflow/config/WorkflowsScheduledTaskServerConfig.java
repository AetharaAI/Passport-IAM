package org.passport.tests.workflow.config;

import org.passport.models.workflow.WorkflowsEventListenerFactory;
import org.passport.testframework.server.PassportServerConfigBuilder;

public class WorkflowsScheduledTaskServerConfig extends WorkflowsBlockingServerConfig {

    @Override
    public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
        return super.configure(config)
                .option("spi-events-listener--" + WorkflowsEventListenerFactory.ID + "--step-runner-task-interval", "1s");
    }
}
