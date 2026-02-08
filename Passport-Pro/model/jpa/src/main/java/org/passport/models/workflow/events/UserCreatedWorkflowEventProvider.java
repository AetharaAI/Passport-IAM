package org.passport.models.workflow.events;

import org.passport.events.Event;
import org.passport.events.EventType;
import org.passport.events.admin.AdminEvent;
import org.passport.events.admin.OperationType;
import org.passport.models.PassportSession;
import org.passport.models.workflow.AbstractWorkflowEventProvider;
import org.passport.models.workflow.ResourceType;

public class UserCreatedWorkflowEventProvider extends AbstractWorkflowEventProvider {

    public UserCreatedWorkflowEventProvider(PassportSession session, String configParameter, String providerId) {
        super(session, configParameter, providerId);
    }

    @Override
    public ResourceType getSupportedResourceType() {
        return ResourceType.USERS;
    }

    @Override
    public boolean supports(Event event) {
        return EventType.REGISTER.equals(event.getType());
    }

    @Override
    public boolean supports(AdminEvent adminEvent) {
        return org.passport.events.admin.ResourceType.USER.equals(adminEvent.getResourceType())
                && OperationType.CREATE.equals(adminEvent.getOperationType());
    }
}
