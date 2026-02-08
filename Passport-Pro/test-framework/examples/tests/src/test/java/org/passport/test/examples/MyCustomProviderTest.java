package org.passport.test.examples;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.server.PassportServerConfig;
import org.passport.testframework.server.PassportServerConfigBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 *
 * @author <a href="mailto:svacek@redhat.com">Simon Vacek</a>
 */
@PassportIntegrationTest(config = MyCustomProviderTest.ServerConfig.class)
public class MyCustomProviderTest {

    @InjectRealm
    ManagedRealm realm;

    @Test
    public void httpGetTest() {
        String url = realm.getBaseUrl();

        HttpUriRequest request = new HttpGet(url + "/custom-provider/hello");
        try {
            HttpResponse response = HttpClientBuilder.create().build().execute(request);
            Assertions.assertEquals(200, response.getStatusLine().getStatusCode());

            String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Assertions.assertEquals("Hello World!", content);
        } catch (IOException ignored) {}
    }

    public static class ServerConfig implements PassportServerConfig {

        @Override
        public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
            return config.dependency("org.passport.testframework", "passport-test-framework-example-providers");
        }

    }
}
