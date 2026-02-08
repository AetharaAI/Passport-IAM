package org.passport.models.workflow.events;

import org.passport.events.Event;
import org.passport.events.EventType;
import org.passport.models.PassportSession;
import org.passport.models.workflow.AbstractWorkflowEventProvider;
import org.passport.models.workflow.ResourceType;
import org.passport.models.workflow.WorkflowExecutionContext;

public class UserAuthenticatedWorkflowEventProvider extends AbstractWorkflowEventProvider {

    public UserAuthenticatedWorkflowEventProvider(PassportSession session, String configParameter, String providerId) {
        super(session, configParameter,  providerId);
    }

    @Override
    public ResourceType getSupportedResourceType() {
        return ResourceType.USERS;
    }

    @Override
    public boolean supports(Event event) {
        return EventType.LOGIN.equals(event.getType());
    }

    @Override
    public boolean evaluate(WorkflowExecutionContext context) {
        if (!super.evaluate(context)) {
            return false;
        }
        if (super.configParameter != null) {
            // this is the case when the clientId is passed as a parameter to the event provider - like user-logged-in(account-console)
            Event loginEvent = (Event) context.getEvent().getEvent();
            return loginEvent != null && configParameter.equals(loginEvent.getClientId());
        } else {
            // nothing else to check
            return true;
        }
    }
}
