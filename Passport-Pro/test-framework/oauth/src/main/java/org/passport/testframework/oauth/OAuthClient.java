package org.passport.testframework.oauth;

import org.passport.OAuth2Constants;
import org.passport.client.registration.ClientRegistration;
import org.passport.testframework.ui.page.LoginPage;
import org.passport.testframework.ui.webdriver.ManagedWebDriver;
import org.passport.testsuite.util.oauth.AbstractOAuthClient;
import org.passport.testsuite.util.oauth.AuthorizationEndpointResponse;
import org.passport.testsuite.util.oauth.OAuthClientConfig;

import org.apache.http.impl.client.CloseableHttpClient;
import org.openqa.selenium.support.PageFactory;

public class OAuthClient extends AbstractOAuthClient<OAuthClient> {

    private final ManagedWebDriver managedWebDriver;

    public OAuthClient(String baseUrl, CloseableHttpClient httpClient, ManagedWebDriver managedWebDriver) {
        super(baseUrl, httpClient, managedWebDriver.driver());
        this.managedWebDriver = managedWebDriver;

        config = new OAuthClientConfig()
                .responseType(OAuth2Constants.CODE);
    }

    @Override
    public void fillLoginForm(String username, String password) {
        LoginPage loginPage = new LoginPage(managedWebDriver);
        PageFactory.initElements(driver, loginPage);
        loginPage.fillLogin(username, password);
        loginPage.submit();
    }

    @Override
    public AuthorizationEndpointResponse parseLoginResponse() {
        managedWebDriver.waiting().waitForOAuthCallback();
        return super.parseLoginResponse();
    }

    public ClientRegistration clientRegistration() {
        return ClientRegistration.create().httpClient(httpClient().get()).url(baseUrl, config.getRealm()).build();
    }

    public void close() {
    }

}
