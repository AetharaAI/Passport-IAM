package org.passport.models.workflow;

import java.util.Set;

import org.passport.component.ComponentModel;
import org.passport.models.PassportSession;

public class AddRequiredActionStepProviderFactory implements WorkflowStepProviderFactory<AddRequiredActionStepProvider> {

    public static final String ID = "set-user-required-action";

    @Override
    public AddRequiredActionStepProvider create(PassportSession session, ComponentModel model) {
        return new AddRequiredActionStepProvider(session, model);
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
        return "Adds a required action to the user";
    }
}
