package org.passport.models.workflow.conditions;

import org.passport.models.PassportSession;
import org.passport.models.workflow.WorkflowConditionProviderFactory;

public class IdentityProviderWorkflowConditionFactory implements WorkflowConditionProviderFactory<IdentityProviderWorkflowConditionProvider> {

    public static final String ID = "has-identity-provider-link";

    @Override
    public IdentityProviderWorkflowConditionProvider create(PassportSession session, String configParameter) {
        return new IdentityProviderWorkflowConditionProvider(session, configParameter);
    }

    @Override
    public String getId() {
        return ID;
    }

}
