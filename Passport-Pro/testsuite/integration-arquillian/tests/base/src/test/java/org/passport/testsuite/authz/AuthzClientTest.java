package org.passport.testsuite.authz;

import java.io.ByteArrayInputStream;

import org.passport.authorization.client.AuthzClient;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class AuthzClientTest {

    @Rule
    public final EnvironmentVariables envVars = new EnvironmentVariables();

    @Test
    public void testCreateWithEnvVars() {
        envVars.set("PASSPORT_REALM", "test");
        envVars.set("PASSPORT_AUTH_SERVER", "http://test");

        RuntimeException runtimeException = Assert.assertThrows(RuntimeException.class, () -> {

            AuthzClient.create(new ByteArrayInputStream(("{\n"
                    + "  \"realm\": \"${env.PASSPORT_REALM}\",\n"
                    + "  \"auth-server-url\": \"${env.PASSPORT_AUTH_SERVER}\",\n"
                    + "  \"ssl-required\": \"external\",\n"
                    + "  \"enable-cors\": true,\n"
                    + "  \"resource\": \"my-server\",\n"
                    + "  \"credentials\": {\n"
                    + "    \"secret\": \"${env.PASSPORT_SECRET}\"\n"
                    + "  },\n"
                    + "  \"confidential-port\": 0,\n"
                    + "  \"policy-enforcer\": {\n"
                    + "    \"enforcement-mode\": \"ENFORCING\"\n"
                    + "  }\n"
                    + "}").getBytes()));
        });

        MatcherAssert.assertThat(runtimeException.getMessage(), Matchers.containsString("Could not obtain configuration from server"));
    }
}
