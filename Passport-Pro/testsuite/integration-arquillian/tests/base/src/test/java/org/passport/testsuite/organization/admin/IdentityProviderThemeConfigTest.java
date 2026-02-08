package org.passport.testsuite.organization.admin;

import org.passport.admin.client.resource.RealmResource;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.testsuite.AbstractAdminTest;
import org.passport.testsuite.util.IdentityProviderBuilder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IdentityProviderThemeConfigTest extends AbstractAdminTest {

    @Before
    public void onBefore() {
        RealmResource realm = testRealm();
        RealmRepresentation rep = realm.toRepresentation();
        rep.setLoginTheme("themeconfig");
        realm.update(rep);
    }

    @Test
    public void testIdentityProviderThemeConfigs() {
        testRealm().identityProviders().create(
                IdentityProviderBuilder.create()
                        .alias("broker")
                        .providerId("oidc")
                        .setAttribute("unsupported-themeConfig", "This value is not shown in the Passport theme")
                        .setAttribute("kcTheme-idpConfigValue", "This value is shown in the Passport theme")
                        .build()).close();

        oauth.realm(TEST_REALM_NAME);
        oauth.openLoginForm();
        String pageSource = driver.getPageSource();
        Assert.assertTrue(pageSource.contains("This value is shown in the Passport theme"));
        Assert.assertFalse(pageSource.contains("This value is not shown in the Passport theme"));
    }
}
