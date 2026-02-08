package org.passport.models.workflow.expression;

import org.passport.models.PassportSession;
import org.passport.models.workflow.WorkflowEventProvider;
import org.passport.models.workflow.WorkflowExecutionContext;

import static org.passport.models.workflow.Workflows.getEventProvider;

public class EventEvaluator extends AbstractBooleanEvaluator {

    private final WorkflowExecutionContext context;
    private final PassportSession session;

    public EventEvaluator(PassportSession session, WorkflowExecutionContext context) {
        this.context = context;
        this.session = session;
    }

    @Override
    public Boolean visitConditionCall(BooleanConditionParser.ConditionCallContext ctx) {
        String name = ctx.Identifier().getText();
        WorkflowEventProvider provider = getEventProvider(session, name.replace("_", "-").toLowerCase(), super.extractParameter(ctx.parameter()));
        return provider.evaluate(context);
    }
}
