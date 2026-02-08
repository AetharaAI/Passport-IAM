package org.passport.models.workflow.conditions;

import org.passport.models.PassportSession;
import org.passport.models.workflow.WorkflowConditionProviderFactory;

public class UserAttributeWorkflowConditionFactory implements WorkflowConditionProviderFactory<UserAttributeWorkflowConditionProvider> {

    public static final String ID = "has-user-attribute";

    @Override
    public UserAttributeWorkflowConditionProvider create(PassportSession session, String keyValuePair) {
        return new UserAttributeWorkflowConditionProvider(session, keyValuePair);
    }

    @Override
    public String getId() {
        return ID;
    }

}
