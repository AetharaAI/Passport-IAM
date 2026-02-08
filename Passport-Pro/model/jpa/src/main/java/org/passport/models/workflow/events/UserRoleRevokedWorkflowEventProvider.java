package org.passport.models.workflow.events;

import org.passport.models.PassportSession;
import org.passport.models.RoleModel.RoleRevokedEvent;
import org.passport.models.workflow.AbstractWorkflowEventProvider;
import org.passport.models.workflow.ResourceType;
import org.passport.models.workflow.WorkflowExecutionContext;
import org.passport.provider.ProviderEvent;

public class UserRoleRevokedWorkflowEventProvider extends AbstractWorkflowEventProvider {

    public UserRoleRevokedWorkflowEventProvider(final PassportSession session, final String configParameter, final String providerId) {
        super(session, configParameter, providerId);
    }

    @Override
    public ResourceType getSupportedResourceType() {
        return ResourceType.USERS;
    }

    @Override
    public boolean supports(ProviderEvent providerEvent) {
        return providerEvent instanceof RoleRevokedEvent;
    }

    @Override
    protected String resolveResourceId(ProviderEvent providerEvent) {
        if (providerEvent instanceof RoleRevokedEvent rre) {
            return rre.getUser().getId();
        }
        return null;
    }

    @Override
    public boolean evaluate(WorkflowExecutionContext context) {
        if (!super.evaluate(context)) {
            return false;
        }
        if (super.configParameter != null) {
            // this is the case when the role name is passed as a parameter to the event provider - like user-role-revoked(myrole)
            ProviderEvent roleEvent = (ProviderEvent) context.getEvent().getEvent();
            if (roleEvent instanceof RoleRevokedEvent roleRevokedEvent) {
                return configParameter.equals(roleRevokedEvent.getRole().getName());
            } else {
                return false;
            }
        } else {
            // nothing else to check
            return true;
        }
    }
}
