package org.passport.models.workflow;

import org.passport.representations.workflows.WorkflowConstants;

final class AdhocWorkflowEvent extends WorkflowEvent {

    AdhocWorkflowEvent(ResourceType type, String resourceId) {
        super(type, resourceId, null, WorkflowConstants.AD_HOC);
    }
}
