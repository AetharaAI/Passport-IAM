package org.passport.models.workflow;

import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.timer.ScheduledTask;

import org.jboss.logging.Logger;

public class ScheduledWorkflowRunner implements ScheduledTask {

    private static final Logger log = Logger.getLogger(DefaultWorkflowProvider.class);

    private final String workflowId;
    private final String realmId;

    public ScheduledWorkflowRunner(String workflowId, String realmId) {
        this.workflowId = workflowId;
        this.realmId = realmId;
    }

    @Override
    public void run(PassportSession session) {
        RealmModel realm = session.realms().getRealm(realmId);

        if (realm == null) {
            log.warnf("Realm %s for scheduled workflow %s not found, cancelling task", realmId, workflowId);
            throw new IllegalStateException("Realm for scheduled workflow not found: " + realmId);
        }

        session.getContext().setRealm(realm);
        WorkflowProvider provider = session.getProvider(WorkflowProvider.class);
        Workflow workflow = provider.getWorkflow(workflowId);

        if (workflow == null) {
            log.warnf("Scheduled workflow %s in realm %s not found, cancelling task", workflowId, realmId);
            throw new IllegalStateException("Scheduled workflow not found: " + workflowId);
        }

        log.debugf("Executing scheduled workflow '%s' in realm %s", workflow.getName(), realm.getName());

        try {
            provider.activateForAllEligibleResources(workflow);
        } catch (Exception e) {
            log.errorf(e, "Error while executing scheduled workflow %s in realm %s", workflow.getName(), realm.getName());
        }

        log.debugf("Finished executing scheduled workflow '%s' in realm %s", workflow.getName(), realm.getName());
    }

    @Override
    public String getTaskName() {
        return "workflow-" + workflowId;
    }
}
