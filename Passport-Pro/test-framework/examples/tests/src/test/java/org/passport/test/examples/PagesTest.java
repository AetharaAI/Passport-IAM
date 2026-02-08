package org.passport.test.examples;

import org.passport.admin.client.Passport;
import org.passport.testframework.annotations.InjectAdminClient;
import org.passport.testframework.annotations.InjectPassportUrls;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.remote.runonserver.InjectRunOnServer;
import org.passport.testframework.remote.runonserver.RunOnServerClient;
import org.passport.testframework.server.PassportUrls;
import org.passport.testframework.ui.annotations.InjectPage;
import org.passport.testframework.ui.annotations.InjectWebDriver;
import org.passport.testframework.ui.page.LoginPage;
import org.passport.testframework.ui.webdriver.BrowserType;
import org.passport.testframework.ui.webdriver.ManagedWebDriver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

@PassportIntegrationTest
public class PagesTest {

    @InjectAdminClient
    Passport adminClient;

    @InjectRunOnServer
    RunOnServerClient runOnServer;

    @InjectWebDriver
    ManagedWebDriver webDriver;

    @InjectPage
    LoginPage loginPage;

    @InjectPassportUrls
    PassportUrls passportUrls;

    @Test
    public void testLoginFromWelcome() {
        webDriver.open(passportUrls.getBaseUrl());

        if (webDriver.getBrowserType().equals(BrowserType.HTML_UNIT)) {
            String pageId = webDriver.findElement(By.xpath("//body")).getAttribute("data-page-id");
            Assertions.assertEquals("admin", pageId);
            Assertions.assertTrue(webDriver.getCurrentUrl().endsWith("/admin/master/console/"));
        } else {
            loginPage.assertCurrent();

            loginPage.fillLogin("admin", "admin");
            loginPage.submit();
        }

    }

}
