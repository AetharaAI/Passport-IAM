package org.passport.email;

import java.util.Map;

import jakarta.mail.Transport;

import org.passport.models.PassportSession;

public interface EmailAuthenticator {

    void connect(PassportSession session, Map<String, String> config, Transport transport) throws EmailException;

    enum AuthenticatorType {
        NONE,
        BASIC,
        TOKEN
    }
}
