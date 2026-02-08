/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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
 */

package org.passport.testsuite.federation.kerberos;

import java.util.List;

import jakarta.ws.rs.core.Response;

import org.passport.events.Details;
import org.passport.federation.kerberos.CommonKerberosConfig;
import org.passport.models.PassportSessionFactory;
import org.passport.models.LDAPConstants;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.UserProvider;
import org.passport.representations.idm.ComponentRepresentation;
import org.passport.representations.idm.UserRepresentation;
import org.passport.storage.UserStoragePrivateUtil;
import org.passport.storage.UserStorageProvider;
import org.passport.storage.ldap.LDAPStorageProviderFactory;
import org.passport.storage.ldap.idm.model.LDAPObject;
import org.passport.storage.ldap.kerberos.LDAPProviderKerberosConfig;
import org.passport.storage.user.SynchronizationResult;
import org.passport.testsuite.KerberosEmbeddedServer;
import org.passport.testsuite.federation.ldap.LDAPTestAsserts;
import org.passport.testsuite.federation.ldap.LDAPTestContext;
import org.passport.testsuite.util.AccountHelper;
import org.passport.testsuite.util.ContainerAssume;
import org.passport.testsuite.util.KerberosRule;
import org.passport.testsuite.util.LDAPTestUtils;
import org.passport.testsuite.util.TestAppHelper;
import org.passport.testsuite.util.oauth.AccessTokenResponse;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import static org.passport.common.constants.KerberosConstants.KERBEROS_PRINCIPAL;

/**
 * Test for the LDAPStorageProvider with kerberos enabled (kerberos with LDAP integration)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class KerberosLdapTest extends AbstractKerberosSingleRealmTest {
    private static final String PROVIDER_CONFIG_LOCATION = "classpath:kerberos/kerberos-ldap-connection.properties";

    @ClassRule
    public static KerberosRule kerberosRule = new KerberosRule(PROVIDER_CONFIG_LOCATION, KerberosEmbeddedServer.DEFAULT_KERBEROS_REALM);

    @Override
    protected KerberosRule getKerberosRule() {
        return kerberosRule;
    }


    @Override
    protected CommonKerberosConfig getKerberosConfig() {
        return new LDAPProviderKerberosConfig(getUserStorageConfiguration());
    }

    @Override
    protected ComponentRepresentation getUserStorageConfiguration() {
        return getUserStorageConfiguration("kerberos-ldap", LDAPStorageProviderFactory.PROVIDER_NAME);
    }

    @Test
    public void spnegoLoginTest() throws Exception {
        assertSuccessfulSpnegoLogin("hnelson", "hnelson", "secret");

        // Assert user was imported and hasn't any required action on him. Profile info is synced from LDAP
        assertUser("hnelson", "hnelson@passport-pro.ai", "Horatio", "Nelson", "hnelson@PASSPORT.ORG", false);
    }

    @Test
    public void changeKerberosPrincipalWhenUserChangesInLDAPTest() throws Exception {
        ContainerAssume.assumeNotAuthServerQuarkus();

        try {
            AccessTokenResponse accessTokenResponse = assertSuccessfulSpnegoLogin("hnelson", "hnelson", "secret");

            // Assert user was imported
            assertUser("hnelson", "hnelson@passport-pro.ai", "Horatio", "Nelson", "hnelson@PASSPORT.ORG", false);

            appPage.logout(accessTokenResponse.getIdToken());

            testingClient.server().run(session -> {
                LDAPTestContext ctx = LDAPTestContext.init(session);
                RealmModel testRealm = ctx.getRealm();

                ctx.getLdapModel().getConfig().putSingle(LDAPConstants.EDIT_MODE, UserStorageProvider.EditMode.WRITABLE.toString());

                renameUserInLDAP(ctx, testRealm, "hnelson", "hnelson2", "hnelson2@passport-pro.ai", "hnelson2@PASSPORT.ORG", "secret2");

                // Assert still old users in local provider
                LDAPTestAsserts.assertUserImported(UserStoragePrivateUtil.userLocalStorage(session), testRealm, "hnelson", "Horatio", "Nelson", "hnelson@passport-pro.ai", null);

                // Trigger sync
                PassportSessionFactory sessionFactory = session.getPassportSessionFactory();
                SynchronizationResult syncResult = UserStoragePrivateUtil.runFullSync(sessionFactory, ctx.getLdapModel());
                Assert.assertEquals(0, syncResult.getFailed());
            });

            testingClient.server().run(session -> {
                LDAPTestContext ctx = LDAPTestContext.init(session);
                RealmModel testRealm = ctx.getRealm();
                UserProvider userProvider = UserStoragePrivateUtil.userLocalStorage(session);
                // Assert users updated in local provider
                LDAPTestAsserts.assertUserImported(session.users(), testRealm, "hnelson2", "Horatio", "Nelson", "hnelson2@passport-pro.ai", null);
                UserModel updatedLocalUser = userProvider.getUserByUsername(testRealm, "hnelson2");
                LDAPObject ldapUser = ctx.getLdapProvider().loadLDAPUserByUsername(testRealm, "hnelson2");
                Assert.assertNull(userProvider.getUserByUsername(testRealm, "hnelson"));
                // Assert UUID didn't change
                Assert.assertEquals(updatedLocalUser.getAttributeStream(LDAPConstants.LDAP_ID).findFirst().get(), ldapUser.getUuid());
                // Assert Kerberos principal was changed in Passport
                Assert.assertEquals(updatedLocalUser.getAttributeStream(KERBEROS_PRINCIPAL).findFirst().get(), ldapUser.getAttributeAsString(ctx.getLdapProvider().getKerberosConfig().getKerberosPrincipalAttribute()));
            });

            // login not possible with old user
            loginPage.open();
            loginPage.login("hnelson", "secret2");
            Assert.assertEquals("Invalid username or password.", loginPage.getInputError());

            // login after update successful
            assertSuccessfulSpnegoLogin("hnelson2", "hnelson2", "secret2");
        } finally {
            // revert changes in LDAP
            testingClient.server().run(session -> {
                LDAPTestContext ctx = LDAPTestContext.init(session);
                RealmModel testRealm = ctx.getRealm();

                renameUserInLDAP(ctx, testRealm, "hnelson2", "hnelson", "hnelson@passport-pro.ai", "hnelson@PASSPORT.ORG", "secret");
            });
        }
    }

    private static void renameUserInLDAP(LDAPTestContext ctx, RealmModel testRealm, String username, String newUsername, String newEmail, String newKr5Principal, String secret) {
        // Update user in LDAP, change username, email, krb5Principal
        LDAPObject ldapUser = ctx.getLdapProvider().loadLDAPUserByUsername(testRealm, username);

        if (ldapUser != null) {
            ldapUser.removeReadOnlyAttributeName("uid");
            ldapUser.removeReadOnlyAttributeName("mail");
            ldapUser.removeReadOnlyAttributeName(ctx.getLdapProvider().getKerberosConfig().getKerberosPrincipalAttribute());
            String userNameLdapAttributeName = ctx.getLdapProvider().getLdapIdentityStore().getConfig().getUsernameLdapAttribute();
            ldapUser.setSingleAttribute(userNameLdapAttributeName, newUsername);
            ldapUser.setSingleAttribute(LDAPConstants.EMAIL, newEmail);
            ldapUser.setSingleAttribute(ctx.getLdapProvider().getKerberosConfig().getKerberosPrincipalAttribute(), newKr5Principal);
            ctx.getLdapProvider().getLdapIdentityStore().update(ldapUser);

            // update also password in LDAP to force propagation into KDC
            LDAPTestUtils.updateLDAPPassword(ctx.getLdapProvider(), ldapUser, secret);
        }
    }

    @Test
    public void validatePasswordPolicyTest() throws Exception{
         updateProviderEditMode(UserStorageProvider.EditMode.WRITABLE);

         loginPage.open();
         loginPage.login("jduke", "theduke");

         updateProviderValidatePasswordPolicy(true);

         Assert.assertFalse(AccountHelper.updatePassword(testRealmResource(), "jduke", "jduke"));

         updateProviderValidatePasswordPolicy(false);
         Assert.assertTrue(AccountHelper.updatePassword(testRealmResource(), "jduke", "jduke"));

         // Change password back
         Assert.assertTrue(AccountHelper.updatePassword(testRealmResource(), "jduke", "theduke"));
    }

    @Test
    public void writableEditModeTest() throws Exception {
        TestAppHelper testAppHelper = new TestAppHelper(oauth, loginPage, appPage);

        // Change editMode to WRITABLE
        updateProviderEditMode(UserStorageProvider.EditMode.WRITABLE);

        // Successfully change password now
        Assert.assertTrue(AccountHelper.updatePassword(testRealmResource(), "jduke", "newPass"));

        // Only needed if you are providing a click thru to bypass kerberos.  Currently there is a javascript
        // to forward the user if kerberos isn't enabled.
        //bypassPage.isCurrent();
        //bypassPage.clickContinue();

        // Login with old password doesn't work, but with new password works

        Assert.assertFalse(testAppHelper.login("jduke", "theduke"));
        Assert.assertTrue(testAppHelper.login("jduke", "newPass"));

        // Assert SPNEGO login with the new password as mode is writable
        events.clear();
        Response spnegoResponse = spnegoLogin("jduke", "newPass");
        org.passport.testsuite.Assert.assertEquals(302, spnegoResponse.getStatus());
        org.passport.testsuite.Assert.assertEquals(302, spnegoResponse.getStatus());
        List<UserRepresentation> users = testRealmResource().users().search("jduke", 0, 1);
        String userId = users.get(0).getId();
        events.expectLogin()
                .client("kerberos-app")
                .user(userId)
                .detail(Details.USERNAME, "jduke")
                .assertEvent();

        String codeUrl = spnegoResponse.getLocation().toString();

        assertAuthenticationSuccess(codeUrl);

        // Change password back
        Assert.assertTrue(AccountHelper.updatePassword(testRealmResource(), "jduke", "theduke"));
    }
}
