package org.passport.models.workflow;

import java.util.Set;

import org.passport.component.ComponentModel;
import org.passport.models.PassportSession;

public class LeaveGroupStepProviderFactory implements WorkflowStepProviderFactory<LeaveGroupStepProvider> {

    public static final String ID = "leave-group";

    @Override
    public LeaveGroupStepProvider create(PassportSession session, ComponentModel model) {
        return new LeaveGroupStepProvider(session, model);
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
        return "Removes a user from one or more groups";
    }
}
