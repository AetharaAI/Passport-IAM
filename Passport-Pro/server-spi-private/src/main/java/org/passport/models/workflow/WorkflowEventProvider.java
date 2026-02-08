package org.passport.models.workflow;

import org.passport.events.Event;
import org.passport.events.admin.AdminEvent;
import org.passport.provider.Provider;
import org.passport.provider.ProviderEvent;

public interface WorkflowEventProvider extends Provider {

    ResourceType getSupportedResourceType();

    WorkflowEvent create(Event event);

    WorkflowEvent create(AdminEvent adminEvent);

    WorkflowEvent create(ProviderEvent providerEvent);

    boolean supports(Event event);

    boolean supports(AdminEvent adminEvent);

    boolean supports(ProviderEvent providerEvent);

    boolean evaluate(WorkflowExecutionContext context);

    @Override
    default void close() {
        // no-op
    }

}
