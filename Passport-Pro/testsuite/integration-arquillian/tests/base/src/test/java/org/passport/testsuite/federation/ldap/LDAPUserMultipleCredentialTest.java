package org.passport.testsuite.federation.ldap;


import java.util.Arrays;

import org.passport.admin.client.resource.RealmResource;
import org.passport.authentication.authenticators.browser.OTPFormAuthenticatorFactory;
import org.passport.authentication.authenticators.browser.PasswordFormFactory;
import org.passport.authentication.authenticators.browser.UsernameFormFactory;
import org.passport.models.AuthenticationExecutionModel;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.credential.OTPCredentialModel;
import org.passport.models.utils.DefaultAuthenticationFlows;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.storage.ldap.idm.model.LDAPObject;
import org.passport.testsuite.arquillian.annotation.EnableVault;
import org.passport.testsuite.client.PassportTestingClient;
import org.passport.testsuite.pages.LoginTotpPage;
import org.passport.testsuite.pages.LoginUsernameOnlyPage;
import org.passport.testsuite.pages.PasswordPage;
import org.passport.testsuite.pages.SelectAuthenticatorPage;
import org.passport.testsuite.util.FlowUtil;
import org.passport.testsuite.util.LDAPRule;
import org.passport.testsuite.util.LDAPTestConfiguration;
import org.passport.testsuite.util.LDAPTestUtils;

import org.jboss.arquillian.graphene.page.Page;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Test user login with multiple credential providers, both local and federated through LDAP.
 *
 * @author Sophie Tauchert
 */
@EnableVault
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LDAPUserMultipleCredentialTest extends AbstractLDAPTest {
    @Page
    protected LoginUsernameOnlyPage loginUsernameOnlyPage;

    @Page
    protected PasswordPage passwordPage;
    
    @Page
    protected SelectAuthenticatorPage selectAuthenticatorPage;

    @Page
    protected LoginTotpPage loginTotpPage;

    @Rule
    // Start an embedded LDAP server with configuration derived from test annotations before each test
    public LDAPRule ldapRule = new LDAPRule()
            .assumeTrue(LDAPTestConfiguration::isStartEmbeddedLdapServer);

    @Override
    protected LDAPRule getLDAPRule() {
        return ldapRule;
    }

    @Override
    protected void afterImportTestRealm() {
        getTestingClient().server().run(session -> {
            LDAPTestContext ctx = LDAPTestContext.init(session);
            RealmModel appRealm = ctx.getRealm();

            // Delete all LDAP users
            LDAPTestUtils.removeAllLDAPUsers(ctx.getLdapProvider(), appRealm);
            // Add some new LDAP users for testing
            LDAPObject user1 = LDAPTestUtils.addLDAPUser
                    (
                            ctx.getLdapProvider(),
                            appRealm,
                            "test-user",
                            "John",
                            "Doe",
                            "test-user@something.org",
                            "some street",
                            "00000"
                    );
            LDAPTestUtils.updateLDAPPassword(ctx.getLdapProvider(), user1, "some-password");
            LDAPObject user2 = LDAPTestUtils.addLDAPUser
                    (
                            ctx.getLdapProvider(),
                            appRealm,
                            "test-user-with-otp",
                            "John",
                            "Doe",
                            "test-user-with-otp@something.org",
                            "some street",
                            "00000"
                    );
            LDAPTestUtils.updateLDAPPassword(ctx.getLdapProvider(), user2, "some-other-password");
            UserModel userWithOtp = session.users().getUserByUsername(appRealm, "test-user-with-otp");
            OTPCredentialModel otpCredential = OTPCredentialModel.createHOTP("DJmQfC73VGFhw7D4QJ8A", 6, 0, "HmacSHA1");
            userWithOtp.credentialManager().createStoredCredential(otpCredential);
        });
    }

    @Test
    public void testUserCredentialsAvailable() {
        configureBrowserFlowWithAlternativeCredentials(testingClient);

        try {
            log.info("Trying login as user without OTP");
            loginUsernameOnlyPage.open();
            loginUsernameOnlyPage.login("test-user");
            passwordPage.assertCurrent();
            passwordPage.assertTryAnotherWayLinkAvailability(false);

            log.info("Trying login as user with OTP");
            loginUsernameOnlyPage.open();
            loginUsernameOnlyPage.login("test-user-with-otp");
            // OTP is locally stored, so takes precedence in Passport
            loginTotpPage.assertCurrent();
            loginTotpPage.assertTryAnotherWayLinkAvailability(true);
            loginTotpPage.clickTryAnotherWayLink();
            selectAuthenticatorPage.assertCurrent();
            // make sure password method exists as well
            Assert.assertEquals(Arrays.asList(SelectAuthenticatorPage.AUTHENTICATOR_APPLICATION, SelectAuthenticatorPage.PASSWORD), selectAuthenticatorPage.getAvailableLoginMethods());

        } finally {
            // Revert flow binding
            resetDefaultBrowserFlow(testRealm());
        }
    }

    static void resetDefaultBrowserFlow(RealmResource realm) {
        RealmRepresentation realmRep = realm.toRepresentation();
        realmRep.setBrowserFlow(DefaultAuthenticationFlows.BROWSER_FLOW);
        realm.update(realmRep);
        realm.flows()
                .getFlows()
                .stream()
                .filter(flowRep -> flowRep.getAlias().equals("browser - alternative"))
                .findFirst()
                .ifPresent(authenticationFlowRepresentation ->
                        realm.flows().deleteFlow(authenticationFlowRepresentation.getId()));
    }

    static void configureBrowserFlowWithAlternativeCredentials(PassportTestingClient testingClient) {
        final String newFlowAlias = "browser - alternative";
        testingClient.server("test").run(session -> FlowUtil.inCurrentRealm(session).copyBrowserFlow(newFlowAlias));
        testingClient.server("test").run(session -> FlowUtil.inCurrentRealm(session)
                .selectFlow(newFlowAlias)
                .inForms(forms -> forms
                        .clear()
                        .addAuthenticatorExecution(AuthenticationExecutionModel.Requirement.REQUIRED, UsernameFormFactory.PROVIDER_ID)
                        .addSubFlowExecution(AuthenticationExecutionModel.Requirement.REQUIRED, altSubFlow -> altSubFlow
                                // Add 2 basic authenticator executions
                                .addAuthenticatorExecution(AuthenticationExecutionModel.Requirement.ALTERNATIVE, PasswordFormFactory.PROVIDER_ID)
                                .addAuthenticatorExecution(AuthenticationExecutionModel.Requirement.ALTERNATIVE, OTPFormAuthenticatorFactory.PROVIDER_ID)
                        )
                )
                .defineAsBrowserFlow()
        );
    }
}
