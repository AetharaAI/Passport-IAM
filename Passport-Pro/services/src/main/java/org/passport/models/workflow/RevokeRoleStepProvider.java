package org.passport.models.workflow;

import org.passport.component.ComponentModel;
import org.passport.models.PassportSession;
import org.passport.models.RoleModel;
import org.passport.models.UserModel;

import org.jboss.logging.Logger;

public class RevokeRoleStepProvider extends RoleBasedStepProvider {

    private final Logger log = Logger.getLogger(RevokeRoleStepProvider.class);

    protected RevokeRoleStepProvider(PassportSession session, ComponentModel model) {
        super(session, model);
    }

    @Override
    protected void run(UserModel user, RoleModel role) {
        log.debugv("Revoking role %s from user %s)", role.getName(), user.getId());
        user.deleteRoleMapping(role);
    }
}
