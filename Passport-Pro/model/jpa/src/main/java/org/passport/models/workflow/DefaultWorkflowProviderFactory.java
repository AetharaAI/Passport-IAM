package org.passport.models.workflow;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.passport.Config.Scope;
import org.passport.common.util.DurationConverter;
import org.passport.component.ComponentModel;
import org.passport.executors.ExecutorsProvider;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RealmModel.RealmRemovedEvent;
import org.passport.models.utils.PassportModelUtils;
import org.passport.models.utils.PostMigrationEvent;
import org.passport.provider.ProviderConfigProperty;
import org.passport.provider.ProviderConfigurationBuilder;
import org.passport.provider.ProviderEvent;
import org.passport.provider.ProviderEventListener;

public class DefaultWorkflowProviderFactory implements WorkflowProviderFactory<DefaultWorkflowProvider>, ProviderEventListener {

    static final String ID = "default";
    private static final long DEFAULT_EXECUTOR_TASK_TIMEOUT = 1000L;

    private WorkflowExecutor executor;
    private boolean blocking;
    private long taskTimeout;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public DefaultWorkflowProvider create(PassportSession session, ComponentModel model) {
        return new DefaultWorkflowProvider(session, executor);
    }

    @Override
    public DefaultWorkflowProvider create(PassportSession session) {
        return new DefaultWorkflowProvider(session, executor);
    }

    @Override
    public void init(Scope config) {
        blocking = config.getBoolean("executorBlocking", false);
        String executorTimeoutStr = config.get("executorTaskTimeout");
        taskTimeout = executorTimeoutStr == null ? DEFAULT_EXECUTOR_TASK_TIMEOUT : DurationConverter.parseDuration(executorTimeoutStr).toMillis();
    }

    @Override
    public void postInit(PassportSessionFactory factory) {
        this.executor = new WorkflowExecutor(getTaskExecutor(factory), blocking, taskTimeout);
        factory.register(this);
    }

    @Override
    public void onEvent(ProviderEvent event) {
        if (event instanceof PostMigrationEvent ev) {
            PassportModelUtils.runJobInTransaction(ev.getFactory(), session ->
                    session.realms().getRealmsStream().forEach(realm -> {
                        session.getContext().setRealm(realm);
                        DefaultWorkflowProvider provider = create(session);

                        try {
                            provider.getWorkflows().forEach(provider::rescheduleWorkflow);
                        } finally {
                            session.getContext().setRealm(null);
                            provider.close();
                        }
                    }));
        } else if (event instanceof RealmRemovedEvent ev) {
            PassportSession session = ev.getPassportSession();
            DefaultWorkflowProvider provider = create(session);

            try {
                provider.getWorkflows().forEach(provider::cancelScheduledWorkflow);
            } finally {
                provider.close();
            }
        }
    }


    @Override
    public void close() {

    }

    @Override
    public String getHelpText() {
        return null;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of();
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name("executor-task-timeout")
                .type("long")
                .helpText("The time in milliseconds before a workflow task is marked as timed out .")
                .defaultValue(DEFAULT_EXECUTOR_TASK_TIMEOUT)
                .add().build();
    }

    private ExecutorService getTaskExecutor(PassportSessionFactory factory) {
        return factory.getProviderFactory(ExecutorsProvider.class).create(null).getExecutor("workflow-event-executor");
    }
}
