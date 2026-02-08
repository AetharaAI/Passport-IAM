package org.passport.testsuite.broker;

import java.util.Map;

import jakarta.ws.rs.core.Response;

import org.passport.admin.client.resource.UserProfileResource;
import org.passport.common.util.MultivaluedHashMap;
import org.passport.models.IdentityProviderSyncMode;
import org.passport.models.LDAPConstants;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.representations.idm.IdentityProviderRepresentation;
import org.passport.representations.idm.UserRepresentation;
import org.passport.representations.userprofile.config.UPConfig;
import org.passport.storage.UserStorageProvider.EditMode;
import org.passport.storage.UserStorageProviderModel;
import org.passport.storage.ldap.LDAPStorageProviderFactory;
import org.passport.storage.ldap.idm.model.LDAPObject;
import org.passport.testsuite.admin.ApiUtil;
import org.passport.testsuite.federation.ldap.LDAPTestContext;
import org.passport.testsuite.pages.IdpConfirmLinkPage;
import org.passport.testsuite.util.LDAPRule;
import org.passport.testsuite.util.LDAPTestUtils;

import org.jboss.arquillian.graphene.page.Page;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import static org.passport.models.utils.ModelToRepresentation.toRepresentationWithoutConfig;

import static org.junit.Assert.assertEquals;

public final class KcOidcBrokerLdapReadOnlyTest extends AbstractInitializedBaseBrokerTest {

    @ClassRule
    public static LDAPRule ldapRule = new LDAPRule();

    @Page
    private IdpConfirmLinkPage confirmLinkPage;

    @Override
    protected BrokerConfiguration getBrokerConfiguration() {
        return new KcOidcBrokerConfiguration() {
            @Override
            public IdentityProviderRepresentation setUpIdentityProvider(IdentityProviderSyncMode syncMode) {
                return super.setUpIdentityProvider(IdentityProviderSyncMode.FORCE);
            }
        };
    }

    @Before
    public void onBefore() {
        createLdapStorageProvider();
        addLdapUser(bc.getUserLogin(), bc.getUserEmail());
    }

    @Test
    public void testDoNotUpdateEmail() {
        // email as optional in both realms
        UserProfileResource userProfile = adminClient.realm(bc.consumerRealmName()).users().userProfile();
        UPConfig upConfig = userProfile.getConfiguration();
        upConfig.getAttribute(UserModel.EMAIL).setRequired(null);
        userProfile.update(upConfig);
        userProfile = adminClient.realm(bc.providerRealmName()).users().userProfile();
        upConfig = userProfile.getConfiguration();
        upConfig.getAttribute(UserModel.EMAIL).setRequired(null);
        userProfile.update(upConfig);

        // federate user and link account
        oauth.clientId("broker-app");
        loginPage.open(bc.consumerRealmName());
        logInWithBroker(bc);
        updateAccountInformationPage.updateAccountInformation(bc.getUserLogin(), bc.getUserEmail(), "f", "l");
        confirmLinkPage.clickLinkAccount();
        loginPage.login(bc.getUserLogin(), "Password1");
        appPage.assertCurrent();

        // unset email on the provider realm
        UserRepresentation user = adminClient.realm(bc.providerRealmName()).users().search(bc.getUserLogin()).get(0);
        user.setEmail("");
        adminClient.realm(bc.providerRealmName()).users().get(user.getId()).update(user);

        // logout user on the consumer realm and login again
        user = adminClient.realm(bc.consumerRealmName()).users().search(bc.getUserLogin()).get(0);
        adminClient.realm(bc.consumerRealmName()).users().get(user.getId()).logout();
        oauth.clientId("broker-app");
        loginPage.open(bc.consumerRealmName());
        logInWithBroker(bc);
        appPage.assertCurrent();

        // email should remain unchanged
        user = adminClient.realm(bc.consumerRealmName()).users().search(bc.getUserLogin()).get(0);
        assertEquals(bc.getUserEmail(), user.getEmail());
    }

    private void createLdapStorageProvider() {
        String providerName = "ldap";
        String providerId = LDAPStorageProviderFactory.PROVIDER_NAME;

        Map<String,String> ldapConfig = ldapRule.getConfig();
        ldapConfig.put(LDAPConstants.SYNC_REGISTRATIONS, "false");
        ldapConfig.put(LDAPConstants.EDIT_MODE, EditMode.READ_ONLY.name());
        ldapConfig.put(UserStorageProviderModel.IMPORT_ENABLED, "true");
        MultivaluedHashMap<String, String> config = toComponentConfig(ldapConfig);

        UserStorageProviderModel model = new UserStorageProviderModel();
        model.setLastSync(0);
        model.setChangedSyncPeriod(-1);
        model.setFullSyncPeriod(-1);
        model.setName(providerName);
        model.setPriority(0);
        model.setProviderId(providerId);
        model.setConfig(config);

        Response resp = adminClient.realm(bc.consumerRealmName()).components().add(toRepresentationWithoutConfig(model));
        getCleanup().addComponentId(ApiUtil.getCreatedId(resp));
    }

    private MultivaluedHashMap<String, String> toComponentConfig(Map<String, String> ldapConfig) {
        MultivaluedHashMap<String, String> config = new MultivaluedHashMap<>();
        for (Map.Entry<String, String> entry : ldapConfig.entrySet()) {
            config.add(entry.getKey(), entry.getValue());

        }
        return config;
    }

    private void addLdapUser(String username, String email) {
        String realmName = bc.consumerRealmName();

        testingClient.server().run(session -> {
            LDAPTestContext ctx = LDAPTestContext.init(session, realmName, null);
            RealmModel appRealm = ctx.getRealm();

            LDAPTestUtils.removeAllLDAPUsers(ctx.getLdapProvider(), appRealm);
            LDAPObject user = LDAPTestUtils.addLDAPUser(ctx.getLdapProvider(), appRealm, username, "f", "l", email , new MultivaluedHashMap<>());
            LDAPTestUtils.updateLDAPPassword(ctx.getLdapProvider(), user, "Password1");
        });
    }
}
