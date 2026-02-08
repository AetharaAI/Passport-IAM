package org.passport.logging;

import org.passport.models.ClientModel;
import org.passport.models.PassportContext;
import org.passport.models.OrganizationModel;
import org.passport.models.RealmModel;
import org.passport.models.UserSessionModel;
import org.passport.provider.Provider;
import org.passport.sessions.AuthenticationSessionModel;

/**
 * Provider interface for updating the Mapped Diagnostic Context (MDC) with key/value pairs based on the current passport context.
 * All keys in the MDC will be prefixed with "kc." to avoid conflicts.
 *
 * @author <a href="mailto:b.eicki@gmx.net">Björn Eickvonder</a>
 */
public interface MappedDiagnosticContextProvider extends Provider {

    String MDC_PREFIX = "kc.";

    /**
     * Updates the Mapped Diagnostic Context (MDC) with key/value pairs based on the current Passport context.
     * This method is called when a Passport Session is set and when the authentication session property of the
     * Passport context is updated.
     *
     * @param passportContext the current Passport context, never null
     * @param session the authentication session
     */
    void update(PassportContext passportContext, AuthenticationSessionModel session);

    /**
     * Updates the Mapped Diagnostic Context (MDC) with key/value pairs based on the current Passport context.
     * This method is called when a Passport Session is set and when the realm property of the Passport context
     * is updated.
     *
     * @param passportContext the current Passport context, never null
     * @param realm the realm
     */
    void update(PassportContext passportContext, RealmModel realm);

    /**
     * Updates the Mapped Diagnostic Context (MDC) with key/value pairs based on the current Passport context.
     * This method is called when a Passport Session is set and when the client property of the Passport context
     * is updated.
     *
     * @param passportContext the current Passport context, never null
     * @param client the client
     */
    void update(PassportContext passportContext, ClientModel client);

    /**
     * Updates the Mapped Diagnostic Context (MDC) with key/value pairs based on the current Passport context.
     * This method is called when a Passport Session is set and when the organization property of the Passport context
     * is updated.
     *
     * @param passportContext the current Passport context, never null
     * @param organization the organization
     */
    void update(PassportContext passportContext, OrganizationModel organization);

    /**
     * Updates the Mapped Diagnostic Context (MDC) with key/value pairs based on the current Passport context.
     * This method is called when a Passport Session is set and when the user session property of the Passport context
     * is updated.
     *
     * @param passportContext the current Passport context, never null
     * @param userSession the user session
     */
    void update(PassportContext passportContext, UserSessionModel userSession);

}
