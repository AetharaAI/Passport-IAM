/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.passport.testsuite.federation.ldap;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.passport.models.ClientModel;
import org.passport.models.LDAPConstants;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.protocol.oidc.OIDCLoginProtocol;
import org.passport.protocol.oidc.mappers.UserAttributeMapper;
import org.passport.representations.IDToken;
import org.passport.storage.UserStoragePrivateUtil;
import org.passport.storage.UserStorageUtil;
import org.passport.storage.ldap.LDAPStorageProvider;
import org.passport.storage.ldap.idm.model.LDAPObject;
import org.passport.testsuite.util.LDAPRule;
import org.passport.testsuite.util.LDAPTestConfiguration;
import org.passport.testsuite.util.LDAPTestUtils;
import org.passport.testsuite.util.oauth.AccessTokenResponse;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LDAPMultipleAttributesTest extends AbstractLDAPTest {


    // Skip this test on MSAD due to lack of supported user multivalued attributes
    @ClassRule
    public static LDAPRule ldapRule = new LDAPRule()
            .assumeTrue((LDAPTestConfiguration ldapConfig) -> {

                String vendor = ldapConfig.getLDAPConfig().get(LDAPConstants.VENDOR);
                return !LDAPConstants.VENDOR_ACTIVE_DIRECTORY.equals(vendor);

            });

    @Override
    protected LDAPRule getLDAPRule() {
        return ldapRule;
    }

    @Override
    protected void afterImportTestRealm() {
        testingClient.server().run(session -> {
            LDAPTestContext ctx = LDAPTestContext.init(session);
            RealmModel appRealm = ctx.getRealm();

            LDAPTestUtils.addZipCodeLDAPMapper(appRealm, ctx.getLdapModel());
            LDAPTestUtils.addUserAttributeMapper(appRealm, ctx.getLdapModel(), "streetMapper", "street", LDAPConstants.STREET);

            // Remove current users and add default users
            LDAPStorageProvider ldapFedProvider = LDAPTestUtils.getLdapProvider(session, ctx.getLdapModel());
            LDAPTestUtils.removeAllLDAPUsers(ldapFedProvider, appRealm);

            LDAPObject james = LDAPTestUtils.addLDAPUser(ldapFedProvider, appRealm, "jbrown", "James", "Brown", "jbrown@passport-pro.ai", null, "88441");
            LDAPTestUtils.updateLDAPPassword(ldapFedProvider, james, "Password1");

            // User for testing duplicating surname and postalCode
            LDAPObject bruce = LDAPTestUtils.addLDAPUser(ldapFedProvider, appRealm, "bwilson", "Bruce", "Wilson", "bwilson@passport-pro.ai", "Elm 5", "88441", "77332");
            bruce.setAttribute("sn", new LinkedHashSet<>(Arrays.asList("Wilson", "Schneider")));
            ldapFedProvider.getLdapIdentityStore().update(bruce);
            LDAPTestUtils.updateLDAPPassword(ldapFedProvider, bruce, "Password1");

            // Create ldap-portal client
            ClientModel ldapClient = appRealm.addClient("ldap-portal");
            ldapClient.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
            ldapClient.addRedirectUri("/ldap-portal");
            ldapClient.addRedirectUri("/ldap-portal/*");
            ldapClient.setManagementUrl("/ldap-portal");
            ldapClient.addProtocolMapper(UserAttributeMapper.createClaimMapper("postalCode", "postal_code", "postal_code", "String", true, true, true, true));
            ldapClient.addProtocolMapper(UserAttributeMapper.createClaimMapper("street", "street", "street", "String", true, true, true, false));
            ldapClient.addScopeMapping(appRealm.getRole("user"));
            ldapClient.setSecret("password");
        });
    }


    @Test
    public void testUserImport() {
        testingClient.server().run(session -> {
            LDAPTestContext ctx = LDAPTestContext.init(session);
            UserStorageUtil.userCache(session).clear();
            RealmModel appRealm = ctx.getRealm();

            // Test user imported in local storage now
            UserModel user = session.users().getUserByUsername(appRealm, "jbrown");
            Assert.assertNotNull(UserStoragePrivateUtil.userLocalStorage(session).getUserById(appRealm, user.getId()));
            LDAPTestAsserts.assertUserImported(UserStoragePrivateUtil.userLocalStorage(session), appRealm, "jbrown", "James", "Brown", "jbrown@passport-pro.ai", "88441");
        });
    }


    @Test
    public void testModel() {
        testingClient.server().run(session -> {
            LDAPTestContext ctx = LDAPTestContext.init(session);
            UserStorageUtil.userCache(session).clear();
            RealmModel appRealm = ctx.getRealm();

            UserModel user = session.users().getUserByUsername(appRealm, "bwilson");
            Assert.assertEquals("bwilson@passport-pro.ai", user.getEmail());
            Assert.assertEquals("Bruce", user.getFirstName());

            // There are 2 lastnames in ldif
            Assert.assertTrue("Wilson".equals(user.getLastName()) || "Schneider".equals(user.getLastName()));

            // Actually there are 2 postalCodes
            List<String> postalCodes = user.getAttributeStream("postal_code").collect(Collectors.toList());
            assertPostalCodes(postalCodes, "88441", "77332");
            List<String> tmp = new LinkedList<>();
            tmp.addAll(postalCodes);
            postalCodes = tmp;
            postalCodes.remove("77332");
            user.setAttribute("postal_code", postalCodes);

        });

        testingClient.server().run(session -> {
            LDAPTestContext ctx = LDAPTestContext.init(session);
            RealmModel appRealm = ctx.getRealm();

            UserModel user = session.users().getUserByUsername(appRealm, "bwilson");
            List<String> postalCodes = user.getAttributeStream("postal_code").collect(Collectors.toList());
            assertPostalCodes(postalCodes, "88441");
            List<String> tmp = new LinkedList<>();
            tmp.addAll(postalCodes);
            postalCodes = tmp;
            postalCodes.add("77332");
            user.setAttribute("postal_code", postalCodes);
        });

        testingClient.server().run(session -> {
            LDAPTestContext ctx = LDAPTestContext.init(session);
            RealmModel appRealm = ctx.getRealm();

            UserModel user = session.users().getUserByUsername(appRealm, "bwilson");
            assertPostalCodes(user.getAttributeStream("postal_code").collect(Collectors.toList()), "88441", "77332");
        });
    }

    private static void assertPostalCodes(List<String> postalCodes, String... expectedPostalCodes) {
        if (expectedPostalCodes == null && postalCodes.isEmpty()) {
            return;
        }


        Assert.assertEquals(expectedPostalCodes.length, postalCodes.size());
        for (String expected : expectedPostalCodes) {
            if (!postalCodes.contains(expected)) {
                Assert.fail("postalCode '" + expected + "' not in postalCodes: " + postalCodes);
            }
        }
    }

    @Test
    public void ldapPortalEndToEndTest() {
        // Login as bwilson
        oauth.client("ldap-portal", "password");
        oauth.redirectUri(suiteContext.getAuthServerInfo().getContextRoot().toString() + "/ldap-portal");

        loginPage.open();
        loginPage.login("bwilson", "Password1");

        String code = oauth.parseLoginResponse().getCode();
        AccessTokenResponse response = oauth.doAccessTokenRequest(code);

        Assert.assertEquals(200, response.getStatusCode());
        IDToken idToken = oauth.verifyIDToken(response.getIdToken());

        Assert.assertEquals("Bruce Wilson", idToken.getName());
        Assert.assertEquals("Elm 5", idToken.getOtherClaims().get("street"));
        Collection postalCodes = (Collection) idToken.getOtherClaims().get("postal_code");
        Assert.assertEquals(2, postalCodes.size());
        Assert.assertTrue(postalCodes.contains("88441"));
        Assert.assertTrue(postalCodes.contains("77332"));

        oauth.doLogout(response.getRefreshToken());

        // Login as jbrown
        loginPage.open();
        loginPage.login("jbrown", "Password1");

        code = oauth.parseLoginResponse().getCode();
        response = oauth.doAccessTokenRequest(code);

        org.passport.testsuite.Assert.assertEquals(200, response.getStatusCode());
        idToken = oauth.verifyIDToken(response.getIdToken());

        Assert.assertEquals("James Brown", idToken.getName());
        Assert.assertNull(idToken.getOtherClaims().get("street"));
        postalCodes = (Collection) idToken.getOtherClaims().get("postal_code");
        Assert.assertEquals(1, postalCodes.size());
        Assert.assertTrue(postalCodes.contains("88441"));
        Assert.assertFalse(postalCodes.contains("77332"));

        oauth.doLogout(response.getRefreshToken());
    }


}
