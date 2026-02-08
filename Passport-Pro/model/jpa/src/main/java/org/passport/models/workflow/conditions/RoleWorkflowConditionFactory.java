package org.passport.models.workflow.conditions;

import org.passport.models.PassportSession;
import org.passport.models.workflow.WorkflowConditionProviderFactory;

public class RoleWorkflowConditionFactory implements WorkflowConditionProviderFactory<RoleWorkflowConditionProvider> {

    public static final String ID = "has-role";

    @Override
    public RoleWorkflowConditionProvider create(PassportSession session, String expectedRole) {
        return new RoleWorkflowConditionProvider(session, expectedRole);
    }

    @Override
    public String getId() {
        return ID;
    }

}
