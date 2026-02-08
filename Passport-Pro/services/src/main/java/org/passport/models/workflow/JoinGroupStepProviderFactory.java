package org.passport.models.workflow;

import java.util.Set;

import org.passport.component.ComponentModel;
import org.passport.models.PassportSession;

public class JoinGroupStepProviderFactory implements WorkflowStepProviderFactory<JoinGroupStepProvider> {

    public static final String ID = "join-group";

    @Override
    public JoinGroupStepProvider create(PassportSession session, ComponentModel model) {
        return new JoinGroupStepProvider(session, model);
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
        return "Adds user to one or more groups";
    }
}
