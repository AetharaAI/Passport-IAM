package org.passport.models.workflow;

import java.util.List;
import java.util.Set;

import org.passport.Config;
import org.passport.component.ComponentModel;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.provider.ProviderConfigProperty;

public class RemoveUserAttributeStepProviderFactory implements WorkflowStepProviderFactory<RemoveUserAttributeStepProvider> {

    public static final String ID = "remove-user-attribute";

    @Override
    public RemoveUserAttributeStepProvider create(PassportSession session, ComponentModel model) {
        return new RemoveUserAttributeStepProvider(session, model);
    }

    @Override
    public void init(Config.Scope config) {
        // no-op
    }

    @Override
    public void postInit(PassportSessionFactory factory) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
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
        return "Removes attributes from a user. Configure attributes to remove using the 'attribute' configuration key with the attribute names.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        // No specific config properties exposed in the UI currently. Attributes are read from the 'attribute' config key.
        return List.of();
    }
}
