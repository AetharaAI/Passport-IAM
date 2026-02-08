package org.passport.authentication.authenticators.conditional;

import org.passport.authentication.AuthenticationFlowContext;
import org.passport.models.AuthenticatorConfigModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.RoleModel;
import org.passport.models.UserModel;
import org.passport.models.utils.PassportModelUtils;

import org.jboss.logging.Logger;

public class ConditionalRoleAuthenticator implements ConditionalAuthenticator {
    public static final ConditionalRoleAuthenticator SINGLETON = new ConditionalRoleAuthenticator();
    private static final Logger logger = Logger.getLogger(ConditionalRoleAuthenticator.class);

    @Override
    public boolean matchCondition(AuthenticationFlowContext context) {
        UserModel user = context.getUser();
        RealmModel realm = context.getRealm();
        AuthenticatorConfigModel authConfig = context.getAuthenticatorConfig();
        if (user != null && authConfig!=null && authConfig.getConfig()!=null) {
            String requiredRole = authConfig.getConfig().get(ConditionalRoleAuthenticatorFactory.CONDITIONAL_USER_ROLE);
            boolean negateOutput = Boolean.parseBoolean(authConfig.getConfig().get(ConditionalRoleAuthenticatorFactory.CONF_NEGATE));
            RoleModel role = PassportModelUtils.getRoleFromString(realm, requiredRole);
            if (role == null) {
                logger.errorv("Invalid role name submitted: {0}", requiredRole);
                return false;
            }

            return negateOutput != user.hasRole(role);
        }
        return false;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // Not used
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public void setRequiredActions(PassportSession session, RealmModel realm, UserModel user) {
        // Not used
    }

    @Override
    public void close() {
        // Does nothing
    }
}
