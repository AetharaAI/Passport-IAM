package org.passport.test.examples;

import java.io.IOException;
import java.net.URL;

import org.passport.admin.client.Passport;
import org.passport.testframework.annotations.InjectAdminClient;
import org.passport.testframework.annotations.InjectHttpClient;
import org.passport.testframework.annotations.InjectPassportUrls;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.https.CertificatesConfig;
import org.passport.testframework.https.CertificatesConfigBuilder;
import org.passport.testframework.https.InjectCertificates;
import org.passport.testframework.https.ManagedCertificates;
import org.passport.testframework.oauth.OAuthClient;
import org.passport.testframework.oauth.annotations.InjectOAuthClient;
import org.passport.testframework.server.PassportUrls;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest
public class TlsEnabledTest {

    @InjectHttpClient
    HttpClient httpClient;

    @InjectOAuthClient
    OAuthClient oAuthClient;

    @InjectAdminClient
    Passport adminClient;

    @InjectCertificates(config = TlsEnabledConfig.class)
    ManagedCertificates managedCertificates;

    @InjectPassportUrls
    PassportUrls passportUrls;

    @Test
    public void testCertSupplier() {
        Assertions.assertNotNull(managedCertificates);

        Assertions.assertNotNull(managedCertificates.getServerKeyStorePath());
        Assertions.assertNull(managedCertificates.getServerTrustStorePath());

        Assertions.assertNotNull(managedCertificates.getClientSSLContext());
    }

    @Test
    public void testHttpClient() throws IOException {
        URL baseUrl = passportUrls.getBaseUrl();
        Assertions.assertEquals("https", baseUrl.getProtocol());

        HttpGet req = new HttpGet(baseUrl.toString());
        HttpResponse resp = httpClient.execute(req);
        Assertions.assertEquals(200, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testAdminClient() {
        adminClient.realm("default");
    }

    @Test
    public void testOAuthClient() {
        Assertions.assertTrue(oAuthClient.doWellKnownRequest().getTokenEndpoint().startsWith("https://"));
    }

    private static class TlsEnabledConfig implements CertificatesConfig {

        @Override
        public CertificatesConfigBuilder configure(CertificatesConfigBuilder config) {
            return config.tlsEnabled(true);
        }
    }
}
