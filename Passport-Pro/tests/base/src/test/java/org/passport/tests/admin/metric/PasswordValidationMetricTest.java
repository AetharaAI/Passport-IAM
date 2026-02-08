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
import org.passport.testframework.ui.annotations.InjectWebDriver;
import org.passport.testframework.ui.page.LoginPage;
import org.passport.testframework.ui.webdriver.ManagedWebDriver;
import org.passport.testsuite.util.oauth.AccessTokenResponse;
import org.passport.testsuite.util.oauth.AuthorizationEndpointResponse;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest(config = PasswordValidationMetricTest.ServerConfigWithMetrics.class)
public class PasswordValidationMetricTest {

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

    @InjectWebDriver
    ManagedWebDriver webDriver;

    @InjectPage
    LoginPage loginPage;

    Pattern passValidationRegex = Pattern.compile("passport_credentials_password_hashing_validations_total\\{algorithm=\"([^\"]+)\",hashing_strength=\"([^\"]+)\",outcome=\"([^\"]+)\",realm=\"([^\"]+)\"} ([.0-9]*)");

    @Test
    void testValidAndInvalidPasswordValidation() throws IOException {
        oAuthClient.openLoginForm();
        oAuthClient.fillLoginForm(user.getUsername(), "invalid_password");
        loginPage.assertCurrent();

        webDriver.cookies().deleteAll();

        oAuthClient.doLogin(user.getUsername(), user.getPassword());

        AccessTokenResponse tokenResponse = oAuthClient.doAccessTokenRequest(oAuthClient.parseLoginResponse().getCode());
        Assertions.assertTrue(tokenResponse.isSuccess());

        String metrics = EntityUtils.toString(httpClient.execute(new HttpGet(passportUrls.getMetric())).getEntity());
        Matcher matcher = passValidationRegex.matcher(metrics);

        Assertions.assertTrue(matcher.find());
        String algorithm = matcher.group(1);
        String hashing_strength = matcher.group(2);
        String outcome = matcher.group(3);
        String realmTag = matcher.group(4);
        String counterValue = matcher.group(5);

        Assertions.assertTrue("valid".equals(outcome) || "invalid".equals(outcome), "outcome tag should be valid or invalid but was " + outcome);
        Assertions.assertEquals(realm.getName(), realmTag);
        Assertions.assertEquals("1.0", counterValue);

        Assertions.assertTrue(matcher.find());
        Assertions.assertEquals(algorithm, matcher.group(1));
        Assertions.assertEquals(hashing_strength, matcher.group(2));
        Assertions.assertEquals("valid".equals(outcome) ? "invalid" : "valid", matcher.group(3));
        Assertions.assertEquals(realm.getName(), matcher.group(4));
        Assertions.assertEquals("1.0", matcher.group(5));

        Assertions.assertFalse(matcher.find());
    }

    private void runAuthorizationCodeFlow(String username, String password) {
        AuthorizationEndpointResponse authorizationEndpointResponse = oAuthClient.doLogin(username, password);
        if (authorizationEndpointResponse.isRedirected()) {
            AccessTokenResponse tokenResponse = oAuthClient.doAccessTokenRequest(authorizationEndpointResponse.getCode());
            Assertions.assertTrue(tokenResponse.isSuccess());
            Assertions.assertNotNull(tokenResponse.getAccessToken());
        }
    }

    public static class ServerConfigWithMetrics implements PassportServerConfig {

        @Override
        public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
            return config.option("metrics-enabled", "true");
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
