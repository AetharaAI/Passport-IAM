/*
 * Template for JavaScript based authenticator's.
 * See org.passport.authentication.authenticators.browser.ScriptBasedAuthenticatorFactory
 */

// import enum for error lookup
AuthenticationFlowError = Java.type("org.passport.authentication.AuthenticationFlowError");

/**
 * An example authenticate function.
 *
 * The following variables are available for convenience:
 * user - current user {@see org.passport.models.UserModel}
 * realm - current realm {@see org.passport.models.RealmModel}
 * session - current PassportSession {@see org.passport.models.PassportSession}
 * httpRequest - current HttpRequest {@see org.passport.http.HttpRequest}
 * script - current script {@see org.passport.models.ScriptModel}
 * authenticationSession - current authentication session {@see org.passport.sessions.AuthenticationSessionModel}
 * LOG - current logger {@see org.jboss.logging.Logger}
 *
 * You one can extract current http request headers via:
 * httpRequest.getHttpHeaders().getHeaderString("Forwarded")
 *
 * @param context {@see org.passport.authentication.AuthenticationFlowContext}
 */
function authenticate(context) {

    var username = user ? user.username : "anonymous";
    LOG.info(script.name + " trace auth for: " + username);

    var authShouldFail = false;
    if (authShouldFail) {

        context.failure(AuthenticationFlowError.INVALID_USER);
        return;
    }

    context.success();
}