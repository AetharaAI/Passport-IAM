package org.passport.tests.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;

import org.passport.connections.jpa.JpaConnectionProvider;
import org.passport.credential.CredentialModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.credential.OTPCredentialModel;
import org.passport.models.jpa.entities.CredentialEntity;
import org.passport.models.utils.PassportModelUtils;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.RealmConfig;
import org.passport.testframework.realm.RealmConfigBuilder;
import org.passport.testframework.remote.annotations.TestOnServer;

import org.junit.jupiter.api.Assertions;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@PassportIntegrationTest
public class CredentialModelTest {

    @InjectRealm(config = CredentialModelRealm.class)
    ManagedRealm realm;

    @TestOnServer
    public void testCredentialCRUD(PassportSession session) throws Exception {
        AtomicReference<String> passwordId = new AtomicReference<>();
        AtomicReference<String> otp1Id = new AtomicReference<>();
        AtomicReference<String> otp2Id = new AtomicReference<>();

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), (PassportSession currentSession) -> {
            RealmModel realm = currentSession.realms().getRealmByName("test");
            currentSession.getContext().setRealm(realm);

            UserModel user = currentSession.users().getUserByUsername(realm, "test-user@localhost");
            List<CredentialModel> list = user.credentialManager().getStoredCredentialsStream()
                    .collect(Collectors.toList());
            Assertions.assertEquals(1, list.size());
            passwordId.set(list.get(0).getId());

            // Create 2 OTP credentials (password was already created)
            CredentialModel otp1 = OTPCredentialModel.createFromPolicy(realm, "secret1", "label1");
            CredentialModel otp2 = OTPCredentialModel.createFromPolicy(realm, "secret2", "label2");
            otp1 = user.credentialManager().createStoredCredential(otp1);
            otp2 = user.credentialManager().createStoredCredential(otp2);
            otp1Id.set(otp1.getId());
            otp2Id.set(otp2.getId());
        });

        try {

            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), (PassportSession currentSession) -> {
                RealmModel realm = currentSession.realms().getRealmByName("test");
                currentSession.getContext().setRealm(realm);
                UserModel user = currentSession.users().getUserByUsername(realm, "test-user@localhost");

                // Assert priorities: password, otp1, otp2
                List<CredentialModel> list = user.credentialManager().getStoredCredentialsStream()
                        .collect(Collectors.toList());
                assertOrder(list, passwordId.get(), otp1Id.get(), otp2Id.get());

                // Assert can't move password when newPreviousCredential not found
                Assertions.assertFalse(user.credentialManager().moveStoredCredentialTo(passwordId.get(), "not-known"));

                // Assert can't move credential when not found
                Assertions.assertFalse(user.credentialManager().moveStoredCredentialTo(otp2Id.get(), "not-known"));

                // Move otp2 up 1 position
                Assertions.assertTrue(user.credentialManager().moveStoredCredentialTo(otp2Id.get(), passwordId.get()));
            });

            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), (PassportSession currentSession) -> {
                RealmModel realm = currentSession.realms().getRealmByName("test");
                currentSession.getContext().setRealm(realm);
                UserModel user = currentSession.users().getUserByUsername(realm, "test-user@localhost");

                // Assert priorities: password, otp2, otp1
                List<CredentialModel> list = user.credentialManager().getStoredCredentialsStream()
                        .collect(Collectors.toList());
                assertOrder(list, passwordId.get(), otp2Id.get(), otp1Id.get());

                // Move otp2 to the top
                Assertions.assertTrue(user.credentialManager().moveStoredCredentialTo(otp2Id.get(), null));
            });

            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), (PassportSession currentSession) -> {
                RealmModel realm = currentSession.realms().getRealmByName("test");
                currentSession.getContext().setRealm(realm);
                UserModel user = currentSession.users().getUserByUsername(realm, "test-user@localhost");

                // Assert priorities: otp2, password, otp1
                List<CredentialModel> list = user.credentialManager().getStoredCredentialsStream()
                        .collect(Collectors.toList());
                assertOrder(list, otp2Id.get(), passwordId.get(), otp1Id.get());

                // Move password down
                Assertions.assertTrue(user.credentialManager().moveStoredCredentialTo(passwordId.get(), otp1Id.get()));
            });

            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), (PassportSession currentSession) -> {
                RealmModel realm = currentSession.realms().getRealmByName("test");
                currentSession.getContext().setRealm(realm);
                UserModel user = currentSession.users().getUserByUsername(realm, "test-user@localhost");

                // Assert priorities: otp2, otp1, password
                List<CredentialModel> list = user.credentialManager().getStoredCredentialsStream()
                        .collect(Collectors.toList());
                assertOrder(list, otp2Id.get(), otp1Id.get(), passwordId.get());

                // Remove otp2 down two positions
                Assertions.assertTrue(user.credentialManager().moveStoredCredentialTo(otp2Id.get(), passwordId.get()));
            });

            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), (PassportSession currentSession) -> {
                RealmModel realm = currentSession.realms().getRealmByName("test");
                currentSession.getContext().setRealm(realm);
                UserModel user = currentSession.users().getUserByUsername(realm, "test-user@localhost");

                // Assert priorities: otp2, otp1, password
                List<CredentialModel> list = user.credentialManager().getStoredCredentialsStream()
                        .collect(Collectors.toList());
                assertOrder(list, otp1Id.get(), passwordId.get(), otp2Id.get());

                // Remove password
                Assertions.assertTrue(user.credentialManager().removeStoredCredentialById(passwordId.get()));
            });

            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), (PassportSession currentSession) -> {
                RealmModel realm = currentSession.realms().getRealmByName("test");
                currentSession.getContext().setRealm(realm);
                UserModel user = currentSession.users().getUserByUsername(realm, "test-user@localhost");

                // Assert priorities: otp2, password
                List<CredentialModel> list = user.credentialManager().getStoredCredentialsStream()
                        .collect(Collectors.toList());
                assertOrder(list, otp1Id.get(), otp2Id.get());
            });

        } finally {
            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), (PassportSession currentSession) -> {
                RealmModel realm = currentSession.realms().getRealmByName("test");
                currentSession.getContext().setRealm(realm);
                UserModel user = currentSession.users().getUserByUsername(realm, "test-user@localhost");
                user.credentialManager().removeStoredCredentialById(otp1Id.get());
                user.credentialManager().removeStoredCredentialById(otp2Id.get());
            });
        }
    }

    @TestOnServer
    public void testCredentialUpdateWithDuplicateLabel(PassportSession session) {
        AtomicReference<String> otp1Id = new AtomicReference<>();
        AtomicReference<String> otp2Id = new AtomicReference<>();

        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), (PassportSession currentSession) -> {
            RealmModel realm = currentSession.realms().getRealmByName("test");
            currentSession.getContext().setRealm(realm);

            UserModel user = currentSession.users().getUserByUsername(realm, "test-user@localhost");

            // Create 2 OTP credentials with the same label (as it was in pre-26.3)
            CredentialModel otp1 = OTPCredentialModel.createFromPolicy(realm, "secret1", "label1");
            CredentialModel otp2 = OTPCredentialModel.createFromPolicy(realm, "secret2", "label2");
            otp1 = user.credentialManager().createStoredCredential(otp1);
            otp2 = user.credentialManager().createStoredCredential(otp2);
            otp1Id.set(otp1.getId());
            otp2Id.set(otp2.getId());

            // Fake a duplicate label by setting the label directly on the JPA level
            EntityManager em = currentSession.getProvider(JpaConnectionProvider.class).getEntityManager();
            CredentialEntity credentialEntity = em.find(CredentialEntity.class, otp2.getId());
            credentialEntity.setUserLabel("label1");
        });

        try {

            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), (PassportSession currentSession) -> {
                RealmModel realm = currentSession.realms().getRealmByName("test");
                currentSession.getContext().setRealm(realm);
                UserModel user = currentSession.users().getUserByUsername(realm, "test-user@localhost");
                CredentialModel credential = user.credentialManager().getStoredCredentialById(otp1Id.get());

                // must not throw an exception even when the two labels are the same
                credential.setSecretData("newsecret");
                user.credentialManager().updateStoredCredential(credential);
            });

        } finally {
            PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), (PassportSession currentSession) -> {
                RealmModel realm = currentSession.realms().getRealmByName("test");
                currentSession.getContext().setRealm(realm);
                UserModel user = currentSession.users().getUserByUsername(realm, "test-user@localhost");
                user.credentialManager().removeStoredCredentialById(otp1Id.get());
                user.credentialManager().removeStoredCredentialById(otp2Id.get());
            });
        }

    }

    private void assertOrder(List<CredentialModel> creds, String... expectedIds) {
        Assertions.assertEquals(expectedIds.length, creds.size());

        if (creds.isEmpty()) return;

        for (int i=0 ; i<expectedIds.length ; i++) {
            Assertions.assertEquals(creds.get(i).getId(), expectedIds[i]);
        }
    }

    public static class CredentialModelRealm implements RealmConfig {

        @Override
        public RealmConfigBuilder configure(RealmConfigBuilder realm) {
            realm.name("test");
            realm.addUser("test-user@localhost").password("password");
            return realm;
        }
    }

}
