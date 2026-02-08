package org.passport.models.workflow.events;

import org.passport.models.PassportSession;
import org.passport.models.workflow.WorkflowEventProvider;
import org.passport.models.workflow.WorkflowEventProviderFactory;

public class ClientAuthenticatedWorkflowEventFactory implements WorkflowEventProviderFactory<WorkflowEventProvider> {

    public static final String ID = "client-authenticated";

    @Override
    public WorkflowEventProvider create(PassportSession session, String configParameter) {
        return new ClientAuthenticatedWorkflowEventProvider(session, configParameter, this.getId());
    }

    @Override
    public String getId() {
        return ID;
    }
}
