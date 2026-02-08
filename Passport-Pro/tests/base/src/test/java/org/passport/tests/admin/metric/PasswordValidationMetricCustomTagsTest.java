package org.passport.tests.admin.metric;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.passport.testframework.annotations.InjectHttpClient;
import org.passport.testframework.annotations.InjectPassportUrls;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.InjectUser;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.oauth.OAuthClient;
import org.passport.testframework.oauth.annotations.InjectOAuthClient;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.ManagedUser;
import org.passport.testframework.realm.UserConfig;
import org.passport.testframework.realm.UserConfigBuilder;
import org.passport.testframework.server.PassportServerConfig;
import org.passport.testframework.server.PassportServerConfigBuilder;
import org.passport.testframework.server.PassportUrls;
import org.passport.testframework.ui.annotations.InjectPage;
import org.passport.testframework.ui.page.LoginPage;
import org.passport.testsuite.util.oauth.AuthorizationEndpointResponse;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest(config = PasswordValidationMetricCustomTagsTest.ServerConfigWithMetrics.class)
public class PasswordValidationMetricCustomTagsTest {

    @InjectUser(config = OAuthUserConfig.class)
    ManagedUser user;

    @InjectRealm
    ManagedRealm realm;

    @InjectOAuthClient
    OAuthClient oAuthClient;

    @InjectPassportUrls
    PassportUrls passportUrls;

    @InjectHttpClient
    HttpClient httpClient;

    @InjectPage
    LoginPage loginPage;

    Pattern passValidationRegex = Pattern.compile("passport_credentials_password_hashing_validations_total\\{realm=\"([^\"]+)\"} ([.0-9]*)");

    @Test
    void testValidAndInvalidPasswordValidation() throws IOException {
        oAuthClient.openLoginForm();
        oAuthClient.fillLoginForm(user.getUsername(), "invalid_password");
        loginPage.assertCurrent();

        AuthorizationEndpointResponse authorizationEndpointResponse = oAuthClient.doLogin(user.getUsername(), user.getPassword());
        Assertions.assertTrue(oAuthClient.doAccessTokenRequest(authorizationEndpointResponse.getCode()).isSuccess());

        String metrics = EntityUtils.toString(httpClient.execute(new HttpGet(passportUrls.getMetric())).getEntity());
        Matcher matcher = passValidationRegex.matcher(metrics);

        Assertions.assertTrue(matcher.find());
        Assertions.assertEquals(realm.getName(), matcher.group(1));
        Assertions.assertEquals("2.0", matcher.group(2));
        Assertions.assertFalse(matcher.find());
    }

    public static class ServerConfigWithMetrics implements PassportServerConfig {

        @Override
        public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
            return config
                    .option("metrics-enabled", "true")
                    .option("spi-credential-passport-password-validations-counter-tags", "realm");
        }
    }

    public static class OAuthUserConfig implements UserConfig {

        @Override
        public UserConfigBuilder configure(UserConfigBuilder user) {
            return user.name("First", "Last")
                    .email("test@local")
                    .password("password");
        }
    }
}
