package org.passport.models.workflow.events;

import org.passport.models.PassportSession;
import org.passport.models.workflow.WorkflowEventProvider;
import org.passport.models.workflow.WorkflowEventProviderFactory;

public class UserFedIdentityAddedWorkflowEventFactory implements WorkflowEventProviderFactory<WorkflowEventProvider> {

    public static final String ID = "user-federated-identity-added";

    @Override
    public WorkflowEventProvider create(PassportSession session, String configParameter) {
        return new UserFedIdentityAddedWorkflowEventProvider(session, configParameter, this.getId());
    }

    @Override
    public String getId() {
        return ID;
    }

}
