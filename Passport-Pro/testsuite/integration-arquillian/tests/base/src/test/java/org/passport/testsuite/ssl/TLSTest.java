package org.passport.testsuite.ssl;

import org.passport.common.enums.SslRequired;
import org.passport.jose.jwk.JSONWebKeySet;
import org.passport.protocol.oidc.representations.OIDCConfigurationRepresentation;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.testsuite.AbstractTestRealmPassportTest;
import org.passport.testsuite.Assert;
import org.passport.testsuite.util.oauth.OpenIDProviderConfigurationResponse;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.passport.testsuite.util.ServerURLs.AUTH_SERVER_SSL_REQUIRED;

/**
 * This test checks if TLS can be explicitly switched off.
 *
 * Note, it should run only if TLS is enabled by default.
 */
public class TLSTest extends AbstractTestRealmPassportTest {

    public static final String AUTH_SERVER_ROOT_WITHOUT_TLS = "http://localhost:" + System.getProperty("auth.server.http.port", "8180") + "/auth";

    @BeforeClass
    public static void checkIfTLSIsTurnedOn() {
        Assume.assumeTrue(AUTH_SERVER_SSL_REQUIRED);
    }

    @Override
    protected boolean modifyRealmForSSL() {
        return false;
    }

    @Override
    public void configureTestRealm(RealmRepresentation testRealm) {
        testRealm.setSslRequired(SslRequired.NONE.toString());
    }

    @Test
    public void testTurningTLSOn() throws Exception {
        //given
        oauth.baseUrl(AUTH_SERVER_ROOT_WITHOUT_TLS);

        //when
        OIDCConfigurationRepresentation config = oauth.doWellKnownRequest();

        //then
        Assert.assertTrue(config.getAuthorizationEndpoint().startsWith(AUTH_SERVER_ROOT_WITHOUT_TLS));
    }

    @Test
    public void testSSLAlwaysRequired() throws Exception {
        // Switch realm SSLRequired to Always
        RealmRepresentation realmRep = testRealm().toRepresentation();
        String origSslRequired = realmRep.getSslRequired();
        realmRep.setSslRequired(SslRequired.ALL.toString());
        testRealm().update(realmRep);

        // Try access "WellKnown" endpoint unsecured. It should fail
        oauth.baseUrl(AUTH_SERVER_ROOT_WITHOUT_TLS);
        OpenIDProviderConfigurationResponse providerConfigurationResponse = oauth.wellknownRequest().send();
        Assert.assertFalse(providerConfigurationResponse.isSuccess());
        Assert.assertEquals("HTTPS required", providerConfigurationResponse.getErrorDescription());

        // Try access "JWKS URL" unsecured. It should fail
        try {
            JSONWebKeySet keySet = oauth.keys().getRealmKeys();
            Assert.fail("This should not be successful");
        } catch (Exception e) {
            // Expected
        }

        // Revert SSLRequired
        realmRep.setSslRequired(origSslRequired);
        testRealm().update(realmRep);
    }

}
