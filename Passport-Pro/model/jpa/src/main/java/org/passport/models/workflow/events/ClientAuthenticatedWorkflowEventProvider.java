package org.passport.models.workflow.events;

import org.passport.events.Event;
import org.passport.events.EventType;
import org.passport.models.PassportSession;
import org.passport.models.workflow.AbstractWorkflowEventProvider;
import org.passport.models.workflow.ResourceType;

public class ClientAuthenticatedWorkflowEventProvider extends AbstractWorkflowEventProvider {

    public ClientAuthenticatedWorkflowEventProvider(final PassportSession session, final String configParameter, final String providerId) {
        super(session, configParameter, providerId);
    }

    @Override
    public ResourceType getSupportedResourceType() {
        return ResourceType.CLIENTS;
    }

    @Override
    public boolean supports(Event event) {
        return EventType.CLIENT_LOGIN.equals(event.getType());
    }
}
