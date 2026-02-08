package org.passport.models.workflow.events;

import org.passport.models.PassportSession;
import org.passport.models.RoleModel.RoleGrantedEvent;
import org.passport.models.workflow.AbstractWorkflowEventProvider;
import org.passport.models.workflow.ResourceType;
import org.passport.models.workflow.WorkflowExecutionContext;
import org.passport.provider.ProviderEvent;

public class UserRoleGrantedWorkflowEventProvider extends AbstractWorkflowEventProvider {

    public UserRoleGrantedWorkflowEventProvider(final PassportSession session, final String configParameter, final String providerId) {
        super(session, configParameter, providerId);
    }

    @Override
    public ResourceType getSupportedResourceType() {
        return ResourceType.USERS;
    }

    @Override
    public boolean supports(ProviderEvent providerEvent) {
        return providerEvent instanceof RoleGrantedEvent;
    }

    @Override
    protected String resolveResourceId(ProviderEvent providerEvent) {
        if (providerEvent instanceof RoleGrantedEvent rge) {
            return rge.getUser().getId();
        }
        return null;
    }

    @Override
    public boolean evaluate(WorkflowExecutionContext context) {
        if (!super.evaluate(context)) {
            return false;
        }
        if (super.configParameter != null) {
            // this is the case when the role name is passed as a parameter to the event provider - like user-role-granted(myrole)
            ProviderEvent roleEvent = (ProviderEvent) context.getEvent().getEvent();
            if (roleEvent instanceof RoleGrantedEvent roleGrantedEvent) {
                return configParameter.equals(roleGrantedEvent.getRole().getName());
            } else {
                return false;
            }
        } else {
            // nothing else to check
            return true;
        }
    }
}
