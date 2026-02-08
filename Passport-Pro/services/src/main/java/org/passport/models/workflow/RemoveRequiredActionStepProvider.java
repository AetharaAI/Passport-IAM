package org.passport.models.workflow;

import org.passport.component.ComponentModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;

import org.jboss.logging.Logger;


public class RemoveRequiredActionStepProvider implements WorkflowStepProvider {

    public static String REQUIRED_ACTION_KEY = "action";

    private final PassportSession session;
    private final ComponentModel stepModel;
    private final Logger log = Logger.getLogger(RemoveRequiredActionStepProvider.class);

    public RemoveRequiredActionStepProvider(PassportSession session, ComponentModel model) {
        this.session = session;
        this.stepModel = model;
    }

    @Override
    public void run(WorkflowExecutionContext context) {
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, context.getResourceId());

        if (user != null) {
            try {
                UserModel.RequiredAction action = UserModel.RequiredAction.valueOf(stepModel.getConfig().getFirst(REQUIRED_ACTION_KEY));
                log.debugv("Removing required action {0} from user {1})", action, user.getId());
                user.removeRequiredAction(action);
            } catch (IllegalArgumentException e) {
                log.warnv("Invalid required action {0} configured in RemoveRequiredActionStepProvider", stepModel.getConfig().getFirst(REQUIRED_ACTION_KEY));
            }
        }
    }

    @Override
    public void close() {
    }
}
