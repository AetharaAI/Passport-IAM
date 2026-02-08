package org.passport.email;

import java.util.Map;

import jakarta.mail.MessagingException;
import jakarta.mail.Transport;

import org.passport.models.PassportSession;
import org.passport.vault.VaultStringSecret;

public class PasswordAuthEmailAuthenticator implements EmailAuthenticator {

    @Override
    public void connect(PassportSession session, Map<String, String> config, Transport transport) throws EmailException {
        try (VaultStringSecret vaultStringSecret = session.vault().getStringSecret(config.get("password"))) {
            transport.connect(config.get("user"), vaultStringSecret.get().orElse(config.get("password")));
        } catch (MessagingException e) {
            throw new EmailException("Password based SMTP connect failed", e);
        }
    }

}
