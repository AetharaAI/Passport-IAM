package org.passport.models.workflow.events;

import org.passport.models.ClientModel;
import org.passport.models.PassportSession;
import org.passport.models.workflow.AbstractWorkflowEventProvider;
import org.passport.models.workflow.ResourceType;
import org.passport.provider.ProviderEvent;

public class ClientCreatedWorkflowEventProvider extends AbstractWorkflowEventProvider {

    public ClientCreatedWorkflowEventProvider(final PassportSession session, final String configParameter, final String providerId) {
        super(session, configParameter, providerId);
    }

    @Override
    public ResourceType getSupportedResourceType() {
        return ResourceType.CLIENTS;
    }

    @Override
    public boolean supports(ProviderEvent providerEvent) {
        return providerEvent instanceof ClientModel.ClientCreationEvent;
    }

    @Override
    protected String resolveResourceId(ProviderEvent providerEvent) {
        if (providerEvent instanceof ClientModel.ClientCreationEvent cce) {
            return cce.getCreatedClient().getId();
        }
        return null;
    }
}
