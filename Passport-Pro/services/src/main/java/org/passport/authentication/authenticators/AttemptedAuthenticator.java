package org.passport.authentication.authenticators;

import org.passport.authentication.AuthenticationFlowContext;
import org.passport.authentication.Authenticator;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;

/**
 * Pass-thru atheneticator that just sets the context to attempted.
 */
public class AttemptedAuthenticator implements Authenticator {

    public static final AttemptedAuthenticator SINGLETON = new AttemptedAuthenticator();
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        context.attempted();

    }

    @Override
    public void action(AuthenticationFlowContext context) {
        throw new RuntimeException("Unreachable!");

    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(PassportSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(PassportSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }
}
