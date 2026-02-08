package org.passport.models.workflow.events;

import org.passport.models.PassportSession;
import org.passport.models.workflow.WorkflowEventProvider;
import org.passport.models.workflow.WorkflowEventProviderFactory;

public class UserGroupMembershipAddedWorkflowEventFactory implements WorkflowEventProviderFactory<WorkflowEventProvider> {

    public static final String ID = "user-group-membership-added";

    @Override
    public WorkflowEventProvider create(PassportSession session, String configParameter) {
        return new UserGroupMembershipAddedWorkflowEventProvider(session, configParameter, this.getId());
    }

    @Override
    public String getId() {
        return ID;
    }
}
