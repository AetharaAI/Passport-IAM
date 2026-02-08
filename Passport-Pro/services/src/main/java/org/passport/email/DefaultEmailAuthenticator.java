package org.passport.email;

import java.util.Map;

import jakarta.mail.MessagingException;
import jakarta.mail.Transport;

import org.passport.models.PassportSession;

public class DefaultEmailAuthenticator implements EmailAuthenticator {

    @Override
    public void connect(PassportSession session, Map<String, String> config, Transport transport) throws EmailException {
        try {
            transport.connect();
        } catch (MessagingException e) {
            throw new EmailException("Non authenticated connect failed", e);
        }
    }
}
