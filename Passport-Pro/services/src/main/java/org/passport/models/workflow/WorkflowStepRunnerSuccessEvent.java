package org.passport.models.workflow;

import org.passport.models.PassportSession;
import org.passport.provider.ProviderEvent;

public record WorkflowStepRunnerSuccessEvent(PassportSession session) implements ProviderEvent {
}
