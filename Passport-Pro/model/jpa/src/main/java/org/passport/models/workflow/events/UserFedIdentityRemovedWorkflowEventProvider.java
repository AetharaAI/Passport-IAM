package org.passport.models.workflow.events;

import org.passport.models.FederatedIdentityModel.FederatedIdentityRemovedEvent;
import org.passport.models.PassportSession;
import org.passport.models.workflow.AbstractWorkflowEventProvider;
import org.passport.models.workflow.ResourceType;
import org.passport.models.workflow.WorkflowExecutionContext;
import org.passport.provider.ProviderEvent;

public class UserFedIdentityRemovedWorkflowEventProvider extends AbstractWorkflowEventProvider {

    public UserFedIdentityRemovedWorkflowEventProvider(final PassportSession session, final String configParameter, final String providerId) {
        super(session, configParameter, providerId);
    }

    @Override
    public ResourceType getSupportedResourceType() {
        return ResourceType.USERS;
    }

    @Override
    public boolean supports(ProviderEvent providerEvent) {
        return providerEvent instanceof FederatedIdentityRemovedEvent;
    }

    @Override
    protected String resolveResourceId(ProviderEvent providerEvent) {
        if (providerEvent instanceof FederatedIdentityRemovedEvent fie) {
            return fie.getUser().getId();
        }
        return null;
    }

    @Override
    public boolean evaluate(WorkflowExecutionContext context) {
        if (!super.evaluate(context)) {
            return false;
        }
        if (super.configParameter != null) {
            // this is the case when the idp alias is passed as a parameter to the event provider - like user-federated-identity-removed(myidp)
            ProviderEvent fedIdentityEvent = (ProviderEvent) context.getEvent().getEvent();
            if (fedIdentityEvent instanceof FederatedIdentityRemovedEvent fie) {
                return configParameter.equals(fie.getFederatedIdentity().getIdentityProvider());
            } else {
                return false;
            }
        } else {
            // nothing else to check
            return true;
        }
    }
}
