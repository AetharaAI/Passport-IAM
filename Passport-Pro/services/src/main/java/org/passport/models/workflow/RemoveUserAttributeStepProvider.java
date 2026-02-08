package org.passport.models.workflow;

import java.util.List;

import org.passport.component.ComponentModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;

import org.jboss.logging.Logger;


public class RemoveUserAttributeStepProvider implements WorkflowStepProvider {

    private final PassportSession session;
    private final ComponentModel stepModel;
    private final Logger log = Logger.getLogger(RemoveUserAttributeStepProvider.class);

    public static final String CONFIG_ATTRIBUTE = "attribute";

    public RemoveUserAttributeStepProvider(PassportSession session, ComponentModel model) {
        this.session = session;
        this.stepModel = model;
    }

    @Override
    public void close() {
    }

    @Override
    public void run(WorkflowExecutionContext context) {
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, context.getResourceId());

        if (user != null) {
            try {
                List<String> attrs = stepModel.getConfig().getOrDefault(CONFIG_ATTRIBUTE, List.of());
                for (String attr : attrs) {
                    log.debugv("Removing attribute {0} from user {1}", attr, user.getId());
                    user.removeAttribute(attr);
                }
            } catch (Exception e) {
                log.errorf(e, "Failed to remove attributes from user %s", user.getId());
            }
        }
    }
}
