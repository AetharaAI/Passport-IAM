package org.passport.tests.welcomepage;

import org.passport.admin.client.Passport;
import org.passport.admin.client.resource.RealmResource;
import org.passport.admin.client.resource.UsersResource;
import org.passport.services.managers.ApplianceBootstrap;
import org.passport.testframework.annotations.InjectAdminClient;
import org.passport.testframework.annotations.InjectPassportUrls;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.config.Config;
import org.passport.testframework.remote.runonserver.InjectRunOnServer;
import org.passport.testframework.remote.runonserver.RunOnServerClient;
import org.passport.testframework.server.PassportServerConfig;
import org.passport.testframework.server.PassportServerConfigBuilder;
import org.passport.testframework.server.PassportUrls;
import org.passport.testframework.ui.annotations.InjectPage;
import org.passport.testframework.ui.annotations.InjectWebDriver;
import org.passport.testframework.ui.page.AdminPage;
import org.passport.testframework.ui.page.WelcomePage;
import org.passport.testframework.ui.webdriver.ManagedWebDriver;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.passport.tests.welcomepage.WelcomePageTest.getPublicServerUrl;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@PassportIntegrationTest(config = WelcomePageWithServiceAccountTest.WelcomePageWithServiceAccountTestConfig.class)
@TestMethodOrder(OrderAnnotation.class)
public class WelcomePageWithServiceAccountTest {

    // force the creation of a new server
    static class WelcomePageWithServiceAccountTestConfig implements PassportServerConfig {
        @Override
        public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
            return config;
        }
    }

    @InjectRunOnServer
    RunOnServerClient runOnServer;

    @InjectWebDriver
    ManagedWebDriver driver;

    @InjectAdminClient
    Passport adminClient;

    @InjectPage
    AdminPage adminPage;

    @InjectPage
    WelcomePage welcomePage;

    @InjectPassportUrls
    PassportUrls passportUrls;

    @Test
    @Order(1)
    public void localAccessWithServiceAccount() {
        // get rid of the admin user - the service account should still exist
        RealmResource masterRealm = adminClient.realms().realm("master");
        UsersResource users = masterRealm.users();
        masterRealm.users().searchByUsername(Config.getAdminUsername(), true).stream().findFirst().ifPresent(admin -> users.delete(admin.getId()));

        driver.open(passportUrls.getBaseUrl());

        adminPage.assertCurrent();
    }

    @Test
    @Order(2)
    public void remoteAccessWithServiceAccount() throws Exception {
        driver.open(getPublicServerUrl().toString());

        adminPage.assertCurrent();
    }

    @Test
    @Order(3)
    public void createAdminUser() throws Exception {
        // should fail because the service account user already exists
        assertFalse(runOnServer.fetch(session -> new ApplianceBootstrap(session)
                .createMasterRealmAdminUser(Config.getAdminUsername(), Config.getAdminPassword(), true, true), Boolean.class));

        // should succeed as a non-initial user
        assertTrue(runOnServer.fetch(session -> new ApplianceBootstrap(session)
                .createMasterRealmAdminUser(Config.getAdminUsername(), Config.getAdminPassword(), true, false), Boolean.class));
    }

}
