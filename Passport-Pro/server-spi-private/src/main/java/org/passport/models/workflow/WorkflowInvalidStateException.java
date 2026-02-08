package org.passport.models.workflow;

import org.passport.models.ModelValidationException;

public class WorkflowInvalidStateException extends ModelValidationException {

    public WorkflowInvalidStateException(String message) {
        super(message);
    }
}
