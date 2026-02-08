package org.passport.models.workflow;

import java.util.Set;

import org.passport.component.ComponentModel;
import org.passport.component.ComponentValidationException;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;

public final class RestartWorkflowStepProviderFactory implements WorkflowStepProviderFactory<RestartWorkflowStepProvider> {

    public static final String ID = "restart";
    public static final String CONFIG_POSITION = "position";

    @Override
    public RestartWorkflowStepProvider create(PassportSession session, ComponentModel model) {
        return new RestartWorkflowStepProvider(getPosition(model));
    }

    @Override
    public void validateConfiguration(PassportSession session, RealmModel realm, ComponentModel model) throws ComponentValidationException {
        if (getPosition(model) < 0) {
            throw new ComponentValidationException("Position must be a non-negative integer");
        }
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Set<ResourceType> getSupportedResourceTypes() {
        // Usable for all resource types.
        return Set.of(ResourceType.values());
    }

    @Override
    public String getHelpText() {
        return "Restarts the current workflow";
    }

    private int getPosition(ComponentModel model) {
        return model.get(CONFIG_POSITION, 0);
    }
}
