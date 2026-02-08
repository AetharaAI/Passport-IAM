package org.passport.tests.cors;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.passport.http.simple.SimpleHttp;
import org.passport.http.simple.SimpleHttpResponse;
import org.passport.services.cors.Cors;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.InjectSimpleHttp;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.server.PassportServerConfig;
import org.passport.testframework.server.PassportServerConfigBuilder;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest(config = CustomCorsAllowedHeadersTest.CustomCorsAllowedHeadersServerConfig.class)
public class CustomCorsAllowedHeadersTest {

    @InjectRealm
    ManagedRealm realm;

    @InjectSimpleHttp
    SimpleHttp simpleHttp;

    @Test
    public void testCustomAllowedHeaders() throws IOException {
        List<String> list;
        try (SimpleHttpResponse response = simpleHttp.doOptions(realm.getBaseUrl() + "/.well-known/openid-configuration").header("Origin", "https://something").asResponse()) {
            Assertions.assertEquals(200, response.getStatus());
            list = Arrays.stream(response.getFirstHeader(Cors.ACCESS_CONTROL_ALLOW_HEADERS).split(", ")).map(String::trim).toList();
        }
        MatcherAssert.assertThat(list, Matchers.hasItems("uber-trace-id", "x-b3-traceid"));
    }

    public static class CustomCorsAllowedHeadersServerConfig implements PassportServerConfig {

        @Override
        public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
            return config.spiOption("cors", "default", "allowed-headers", "uber-trace-id,x-b3-traceid");
        }
    }

}
