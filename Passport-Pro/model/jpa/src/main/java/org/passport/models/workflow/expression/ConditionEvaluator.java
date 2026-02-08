package org.passport.models.workflow.expression;

import org.passport.models.PassportSession;
import org.passport.models.workflow.WorkflowConditionProvider;
import org.passport.models.workflow.WorkflowExecutionContext;

import static org.passport.models.workflow.Workflows.getConditionProvider;

public class ConditionEvaluator extends AbstractBooleanEvaluator {

    protected final PassportSession session;
    protected final WorkflowExecutionContext context;

    public ConditionEvaluator(PassportSession session, WorkflowExecutionContext context) {
        this.session = session;
        this.context = context;
    }

    @Override
    public Boolean visitConditionCall(BooleanConditionParser.ConditionCallContext ctx) {
        String conditionName = ctx.Identifier().getText();
        WorkflowConditionProvider conditionProvider = getConditionProvider(session, conditionName.replace("_", "-").toLowerCase(), super.extractParameter(ctx.parameter()));
        return conditionProvider.evaluate(context);
    }

}
