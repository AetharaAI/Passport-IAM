package org.passport.models.workflow;

import org.passport.component.ComponentModel;
import org.passport.models.PassportSession;
import org.passport.models.RoleModel;
import org.passport.models.UserModel;

import org.jboss.logging.Logger;

public class GrantRoleStepProvider extends RoleBasedStepProvider {

    private final Logger log = Logger.getLogger(GrantRoleStepProvider.class);

    protected GrantRoleStepProvider(PassportSession session, ComponentModel model) {
        super(session, model);
    }

    @Override
    protected void run(UserModel user, RoleModel role) {
        log.debugv("Granting role %s to user %s)", role.getName(), user.getId());
        user.grantRole(role);
    }
}
