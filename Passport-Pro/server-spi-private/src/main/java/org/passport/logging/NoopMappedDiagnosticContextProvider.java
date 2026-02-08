package org.passport.logging;

import org.passport.models.ClientModel;
import org.passport.models.PassportContext;
import org.passport.models.OrganizationModel;
import org.passport.models.RealmModel;
import org.passport.models.UserSessionModel;
import org.passport.sessions.AuthenticationSessionModel;

public class NoopMappedDiagnosticContextProvider implements MappedDiagnosticContextProvider {

    @Override
    public void update(PassportContext passportContext, AuthenticationSessionModel session) {
        // no-op
    }

    @Override
    public void update(PassportContext passportContext, RealmModel realm) {
        // no-op
    }

    @Override
    public void update(PassportContext passportContext, ClientModel client) {
        // no-op
    }

    @Override
    public void update(PassportContext passportContext, OrganizationModel organization) {
        // no-op
    }

    @Override
    public void update(PassportContext passportContext, UserSessionModel userSession) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }
}
