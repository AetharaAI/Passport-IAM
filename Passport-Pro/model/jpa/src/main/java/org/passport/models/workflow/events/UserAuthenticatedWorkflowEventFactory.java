package org.passport.models.workflow.events;

import org.passport.models.PassportSession;
import org.passport.models.workflow.WorkflowEventProvider;
import org.passport.models.workflow.WorkflowEventProviderFactory;

public class UserAuthenticatedWorkflowEventFactory implements WorkflowEventProviderFactory<WorkflowEventProvider> {

    public static final String ID = "user-authenticated";

    @Override
    public WorkflowEventProvider create(PassportSession session, String configParameter) {
        return new UserAuthenticatedWorkflowEventProvider(session, configParameter, this.getId());
    }

    @Override
    public String getId() {
        return ID;
    }
}
