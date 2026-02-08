package org.passport.test.examples;

import java.util.Map;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.passport.events.email.EmailEventListenerProviderFactory;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.InjectUser;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.mail.MailServer;
import org.passport.testframework.mail.annotations.InjectMailServer;
import org.passport.testframework.oauth.OAuthClient;
import org.passport.testframework.oauth.annotations.InjectOAuthClient;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.ManagedUser;
import org.passport.testframework.realm.RealmConfig;
import org.passport.testframework.realm.RealmConfigBuilder;
import org.passport.testframework.realm.UserConfig;
import org.passport.testframework.realm.UserConfigBuilder;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest
public class EmailTest {

    @InjectRealm(config = EmailSenderRealmConfig.class)
    ManagedRealm realm;

    @InjectUser(config = UserWithEmail.class)
    ManagedUser user;

    @InjectMailServer
    MailServer mail;

    @InjectOAuthClient
    OAuthClient oAuthClient;

    @Test
    public void testEmail() throws MessagingException {
        oAuthClient.doPasswordGrantRequest(user.getUsername(), "invalid");

        Map<String, String> smtpServer = realm.admin().toRepresentation().getSmtpServer();
        Assertions.assertEquals("auto@passport-pro.ai", smtpServer.get("from"));
        Assertions.assertEquals("localhost", smtpServer.get("host"));
        Assertions.assertEquals("3025", smtpServer.get("port"));

        mail.waitForIncomingEmail(1);
        MimeMessage lastReceivedMessage = mail.getLastReceivedMessage();
        Assertions.assertEquals("Login error", lastReceivedMessage.getSubject());
        MatcherAssert.assertThat(lastReceivedMessage.getMessageID(), Matchers.endsWith("@passport-pro.ai>"));
    }

    public static class EmailSenderRealmConfig implements RealmConfig {

        @Override
        public RealmConfigBuilder configure(RealmConfigBuilder realm) {
            return realm.eventsListeners(EmailEventListenerProviderFactory.ID);
        }
    }

    public static class UserWithEmail implements UserConfig {

        @Override
        public UserConfigBuilder configure(UserConfigBuilder user) {
            return user.username("test").email("test@local").password("password").emailVerified(true);
        }
    }

}
