package org.passport.models.workflow;

import java.util.Set;

import org.passport.component.ComponentModel;
import org.passport.models.PassportSession;

public class GrantRoleStepProviderFactory implements WorkflowStepProviderFactory<GrantRoleStepProvider> {

    public static final String ID = "grant-role";

    @Override
    public GrantRoleStepProvider create(PassportSession session, ComponentModel model) {
        return new GrantRoleStepProvider(session, model);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Set<ResourceType> getSupportedResourceTypes() {
        return Set.of(ResourceType.USERS);
    }

    @Override
    public String getHelpText() {
        return "Grants one or more roles to a user";
    }
}
