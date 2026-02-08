package org.passport.models.workflow;

import java.util.Set;

import org.passport.component.ComponentModel;
import org.passport.models.PassportSession;

public class RevokeRoleStepProviderFactory implements WorkflowStepProviderFactory<RevokeRoleStepProvider> {

    public static final String ID = "revoke-role";

    @Override
    public RevokeRoleStepProvider create(PassportSession session, ComponentModel model) {
        return new RevokeRoleStepProvider(session, model);
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
        return "Revokes roles assigned to the user";
    }
}
