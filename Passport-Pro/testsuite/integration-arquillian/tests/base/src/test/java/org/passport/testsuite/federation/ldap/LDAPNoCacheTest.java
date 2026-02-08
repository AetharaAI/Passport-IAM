/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.passport.testsuite.federation.ldap;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.passport.models.PassportSession;
import org.passport.models.LDAPConstants;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.UserProvider;
import org.passport.storage.UserStoragePrivateUtil;
import org.passport.storage.UserStorageProviderModel;
import org.passport.storage.ldap.LDAPStorageProvider;
import org.passport.storage.ldap.idm.model.LDAPObject;
import org.passport.storage.ldap.mappers.UserAttributeLDAPStorageMapper;
import org.passport.storage.ldap.mappers.UserAttributeLDAPStorageMapperFactory;
import org.passport.testsuite.Assert;
import org.passport.testsuite.client.PassportTestingClient;
import org.passport.testsuite.pages.AppPage;
import org.passport.testsuite.pages.LoginPage;
import org.passport.testsuite.pages.LoginPasswordResetPage;
import org.passport.testsuite.pages.LoginPasswordUpdatePage;
import org.passport.testsuite.util.GreenMailRule;
import org.passport.testsuite.util.LDAPRule;
import org.passport.testsuite.util.LDAPTestUtils;
import org.passport.testsuite.util.MailUtils;

import org.jboss.arquillian.graphene.page.Page;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;

/**
 * Test for the scenarios with disabled cache for LDAP provider. This involves scenarios when something is changed directly in LDAP server
 * and changes are supposed to be immediately visible on Passport side
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LDAPNoCacheTest extends AbstractLDAPTest {

    @ClassRule
    public static LDAPRule ldapRule = new LDAPRule();

    @Override
    protected LDAPRule getLDAPRule() {
        return ldapRule;
    }

    @Override
    protected void afterImportTestRealm() {
        testingClient.server().run(session -> {
            LDAPTestContext ctx = LDAPTestContext.init(session);

            // Switch to NO_CACHE
            RealmModel appRealm = ctx.getRealm();
            ctx.getLdapModel().setCachePolicy(UserStorageProviderModel.CachePolicy.NO_CACHE);
            appRealm.updateComponent(ctx.getLdapModel());

            // Switch mappers to "Always read value from LDAP". Changed attributes in LDAP should be immediately visible on Passport side
            appRealm.getComponentsStream(ctx.getLdapModel().getId())
                    .filter(mapper -> UserAttributeLDAPStorageMapperFactory.PROVIDER_ID.equals(mapper.getProviderId()))
                    .forEach(mapper -> {
                        mapper.put(UserAttributeLDAPStorageMapper.ALWAYS_READ_VALUE_FROM_LDAP, true);
                        appRealm.updateComponent(mapper);

                    });

            // Delete all LDAP users and add some new for testing
            LDAPTestUtils.removeAllLDAPUsers(ctx.getLdapProvider(), appRealm);

            LDAPObject john = LDAPTestUtils.addLDAPUser(ctx.getLdapProvider(), appRealm, "johnpassport", "John", "Doe", "john_old@email.org", null, "1234");
            LDAPTestUtils.updateLDAPPassword(ctx.getLdapProvider(), john, "Password1");

        });
    }

    @Rule
    public GreenMailRule greenMail = new GreenMailRule();

    @Page
    protected AppPage appPage;

    @Page
    protected LoginPage loginPage;

    @Page
    protected LoginPasswordResetPage resetPasswordPage;

    @Page
    protected LoginPasswordUpdatePage updatePasswordPage;

    private static void checkEmailAddressMultipleVariants(PassportTestingClient testingClient, String username, String newEmail) {
        testingClient.server().run((PassportSession session) -> {
            LDAPTestContext ctx = LDAPTestContext.init(session);

            RealmModel realm = ctx.getRealm();
            UserModel user = session.users().getUserByUsername(realm, username);
            Assert.assertNotNull("User not found", user);
            Assert.assertEquals(newEmail, user.getEmail());
            Assert.assertEquals(Collections.singletonList(newEmail), user.getAttributes().get(UserModel.EMAIL));
            Assert.assertEquals(Collections.singletonList(newEmail), user.getAttributeStream(UserModel.EMAIL).collect(Collectors.toList()));
        });
    }


    // PASSPORT-10852
    @Test
    public void resetPasswordLink() throws IOException, MessagingException {
        // Trigger reset password from the login page
        loginPage.open();

        // Send and email to the current email address of john. This will sync john to the Passport DB
        triggerForgetPasswordForUser("john_old@email.org", 1, "john_old@email.org");

        // Change the email address of user directly in LDAP
        changeEmailAddressInLDAP(testingClient,"john_new@email.org");

        try {
            // Search for the user and check email is new address in the multiple attribute variants
            checkEmailAddressMultipleVariants(testingClient, "johnpassport", "john_new@email.org");

            // Test 1 - Use username on the ResetPassword form. Mail should be sent to new address
            triggerForgetPasswordForUser("johnpassport", 2, "john_new@email.org");

            // Test 2 - Use old email on the ResetPassword form. Mail should NOT be sent and count of messages should be still the same
            triggerForgetPasswordForUser("john_old@email.org", 2, "john_new@email.org");

            // Test 3 - Use new email on the ResetPassword form. Mail should be sent to new address
            triggerForgetPasswordForUser("john_new@email.org", 3, "john_new@email.org");
        } finally {
            // Revert email address in LDAP
            changeEmailAddressInLDAP(testingClient, "john_old@email.org");
        }
    }

    @Test
    public void resetPasswordLinkCheckOldAddressLast() throws IOException, MessagingException {
        // Trigger reset password from the login page
        loginPage.open();

        triggerForgetPasswordForUser("john_old@email.org", 1, "john_old@email.org");

        changeEmailAddressInLDAP(testingClient,"john_new@email.org");

        try {
            // Test 1 - Use username on the ResetPassword form. Mail should be sent to new address
            triggerForgetPasswordForUser("johnpassport", 2, "john_new@email.org");

            // Test 2 - Use new email on the ResetPassword form. Mail should be sent to new address
            triggerForgetPasswordForUser("john_new@email.org", 3, "john_new@email.org");

            // Test 3 - Use old email on the ResetPassword form. Mail should NOT be sent and count of messages should be still the same
            triggerForgetPasswordForUser("john_old@email.org", 3, "john_new@email.org");
        } finally {
            // Revert email address in LDAP
            changeEmailAddressInLDAP(testingClient, "john_old@email.org");
        }
    }

    /**
     * Trigger "Forget password" for the user and test mail was sent to expected address.
     * Assumption is that browser (webDriver) is on loginPage when this method is triggered
     *
     * @param usernameInput username or email, which will be used on "Reset Password" form
     * @param expectedCountOfMessages expected count of delivered messages. Important to test if new message was sent or not
     * @param expectedEmail Expected email address where the last email message was sent
     *
     */
    private void triggerForgetPasswordForUser(String usernameInput, int expectedCountOfMessages, String expectedEmail) throws MessagingException {
        loginPage.resetPassword();

        resetPasswordPage.assertCurrent();
        resetPasswordPage.changePassword(usernameInput);

        loginPage.assertCurrent();
        assertEquals("You should receive an email shortly with further instructions.", loginPage.getSuccessMessage());

        MimeMessage[] messages = greenMail.getReceivedMessages();
        Assert.assertEquals(expectedCountOfMessages, messages.length);
        MimeMessage message = greenMail.getReceivedMessages()[expectedCountOfMessages - 1];

        String emailAddress = MailUtils.getRecipient(message);
        Assert.assertEquals(expectedEmail, emailAddress);
    }

    private static void changeEmailAddressInLDAP(PassportTestingClient testingClient, String newEmail) {
        testingClient.server().run((PassportSession session) -> {
            LDAPTestContext ctx = LDAPTestContext.init(session);

            RealmModel realm = ctx.getRealm();
            LDAPStorageProvider ldapProvider = ctx.getLdapProvider();
            LDAPObject ldapUser = ldapProvider.loadLDAPUserByUsername(realm, "johnpassport");
            ldapUser.setSingleAttribute(LDAPConstants.EMAIL, newEmail);
            ctx.getLdapProvider().getLdapIdentityStore().update(ldapUser);

        });
    }

    // PASSPORT-13817
    @Test
    public void lookupByAttributeAfterImportWithAttributeValueAlwaysReadFromLdapMustSucceed() {
        testingClient.server().run(session -> {
            LDAPTestContext ctx = LDAPTestContext.init(session);
            RealmModel realm = ctx.getRealm();
            ctx.getLdapModel().setImportEnabled(true);
            realm.updateComponent(ctx.getLdapModel());

            UserProvider localStorage = UserStoragePrivateUtil.userLocalStorage(session);
            LDAPStorageProvider ldapProvider = ctx.getLdapProvider();

            // assume no user imported
            UserModel user = localStorage.getUserByUsername(realm, "johnpassport");
            assumeThat(user, is(nullValue()));

            // trigger import
            List<UserModel> byEmail = ldapProvider.searchForUserByUserAttributeStream(realm, "email", "john_old@email.org")
                    .collect(Collectors.toList());
            assumeThat(byEmail, hasSize(1));

            // assume that user has been imported
            user = localStorage.getUserByUsername(realm, "johnpassport");
            assumeThat(user, is(not(nullValue())));

            // search a second time
            byEmail = ldapProvider.searchForUserByUserAttributeStream(realm, "email", "john_old@email.org")
                    .collect(Collectors.toList());
            assertThat(byEmail, hasSize(1));
        });
    }

}
