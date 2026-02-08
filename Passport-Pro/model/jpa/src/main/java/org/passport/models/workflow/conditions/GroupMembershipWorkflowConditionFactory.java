package org.passport.models.workflow.conditions;

import org.passport.models.PassportSession;
import org.passport.models.workflow.WorkflowConditionProviderFactory;

public class GroupMembershipWorkflowConditionFactory implements WorkflowConditionProviderFactory<GroupMembershipWorkflowConditionProvider> {

    public static final String ID = "is-member-of";

    @Override
    public GroupMembershipWorkflowConditionProvider create(PassportSession session, String configParameter) {
        return new GroupMembershipWorkflowConditionProvider(session, configParameter);
    }

    @Override
    public String getId() {
        return ID;
    }
}
