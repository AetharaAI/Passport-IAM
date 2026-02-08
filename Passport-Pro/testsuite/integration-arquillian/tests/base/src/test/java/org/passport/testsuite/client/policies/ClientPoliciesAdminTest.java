/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.passport.testsuite.client.policies;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.passport.OAuthErrorException;
import org.passport.authentication.authenticators.client.ClientIdAndSecretAuthenticator;
import org.passport.authentication.authenticators.client.JWTClientAuthenticator;
import org.passport.authentication.authenticators.client.JWTClientSecretAuthenticator;
import org.passport.authentication.authenticators.client.X509ClientAuthenticator;
import org.passport.common.Profile;
import org.passport.models.AdminRoles;
import org.passport.models.Constants;
import org.passport.models.OAuth2DeviceConfig;
import org.passport.models.utils.PassportModelUtils;
import org.passport.protocol.oidc.OIDCConfigAttributes;
import org.passport.representations.idm.ClientRepresentation;
import org.passport.representations.idm.CredentialRepresentation;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.representations.idm.UserRepresentation;
import org.passport.services.clientpolicy.ClientPolicyException;
import org.passport.services.clientpolicy.condition.ClientUpdaterContextConditionFactory;
import org.passport.services.clientpolicy.executor.SecureClientAuthenticatorExecutorFactory;
import org.passport.testsuite.arquillian.annotation.EnableFeature;
import org.passport.testsuite.pages.ErrorPage;
import org.passport.testsuite.pages.LogoutConfirmPage;
import org.passport.testsuite.pages.OAuthGrantPage;
import org.passport.testsuite.util.ClientBuilder;
import org.passport.testsuite.util.ClientPoliciesUtil.ClientPoliciesBuilder;
import org.passport.testsuite.util.ClientPoliciesUtil.ClientPolicyBuilder;
import org.passport.testsuite.util.ClientPoliciesUtil.ClientProfileBuilder;
import org.passport.testsuite.util.ClientPoliciesUtil.ClientProfilesBuilder;
import org.passport.testsuite.util.UserBuilder;

import org.jboss.arquillian.graphene.page.Page;
import org.junit.Test;

import static org.passport.testsuite.AbstractAdminTest.loadJson;
import static org.passport.testsuite.util.ClientPoliciesUtil.createClientUpdateContextConditionConfig;
import static org.passport.testsuite.util.ClientPoliciesUtil.createSecureClientAuthenticatorExecutorConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * This test class is for testing client policies' related actions done through an admin console, admin CLI, and admin REST API.
 * 
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
@EnableFeature(value = Profile.Feature.CLIENT_SECRET_ROTATION)
public class ClientPoliciesAdminTest extends AbstractClientPoliciesTest {

    @Page
    protected OAuthGrantPage grantPage;

    @Page
    protected ErrorPage errorPage;

    @Page
    protected LogoutConfirmPage logoutConfirmPage;

    @Override
    public void addTestRealms(List<RealmRepresentation> testRealms) {
        RealmRepresentation realm = loadJson(getClass().getResourceAsStream("/testrealm.json"), RealmRepresentation.class);

        List<UserRepresentation> users = realm.getUsers();

        LinkedList<CredentialRepresentation> credentials = new LinkedList<>();
        CredentialRepresentation password = new CredentialRepresentation();
        password.setType(CredentialRepresentation.PASSWORD);
        password.setValue("password");
        credentials.add(password);

        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername("manage-clients");
        user.setCredentials(credentials);
        user.setClientRoles(Collections.singletonMap(Constants.REALM_MANAGEMENT_CLIENT_ID, Collections.singletonList(AdminRoles.MANAGE_CLIENTS)));

        users.add(user);

        user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername("create-clients");
        user.setCredentials(credentials);
        user.setClientRoles(Collections.singletonMap(Constants.REALM_MANAGEMENT_CLIENT_ID, Collections.singletonList(AdminRoles.CREATE_CLIENT)));
        user.setGroups(List.of("topGroup")); // defined in testrealm.json

        users.add(user);

        realm.setUsers(users);

        List<ClientRepresentation> clients = realm.getClients();

        ClientRepresentation app = ClientBuilder.create()
                .id(PassportModelUtils.generateId())
                .clientId("test-device")
                .secret("secret")
                .attribute(OAuth2DeviceConfig.OAUTH2_DEVICE_AUTHORIZATION_GRANT_ENABLED, "true")
                .attribute(OIDCConfigAttributes.POST_LOGOUT_REDIRECT_URIS, "+")
                .build();
        clients.add(app);

        ClientRepresentation appPublic = ClientBuilder.create().id(PassportModelUtils.generateId()).publicClient()
                .clientId(DEVICE_APP_PUBLIC)
                .attribute(OAuth2DeviceConfig.OAUTH2_DEVICE_AUTHORIZATION_GRANT_ENABLED, "true")
                .attribute(OIDCConfigAttributes.POST_LOGOUT_REDIRECT_URIS, "+")
                .build();
        clients.add(appPublic);

        userId = PassportModelUtils.generateId();
        UserRepresentation deviceUser = UserBuilder.create()
                .id(userId)
                .username("device-login")
                .email("device-login@localhost")
                .password("password")
                .build();
        users.add(deviceUser);

        testRealms.add(realm);
    }

    @Test
    public void testAdminClientRegisterUnacceptableAuthType() throws Exception {
        setupPolicyClientIdAndSecretNotAcceptableAuthType(POLICY_NAME);
        try {
            createClientByAdmin(generateSuffixedName(CLIENT_NAME), (ClientRepresentation clientRep) -> clientRep.setClientAuthenticatorType(ClientIdAndSecretAuthenticator.PROVIDER_ID));
            fail();
        } catch (ClientPolicyException e) {
            assertEquals(OAuthErrorException.INVALID_CLIENT_METADATA, e.getMessage());
        }
    }

    @Test
    public void testAdminClientRegisterAcceptableAuthType() throws Exception {
        setupPolicyClientIdAndSecretNotAcceptableAuthType(POLICY_NAME);
        String cId = createClientByAdmin(generateSuffixedName(CLIENT_NAME), (ClientRepresentation clientRep) -> clientRep.setClientAuthenticatorType(JWTClientSecretAuthenticator.PROVIDER_ID));
        assertEquals(JWTClientSecretAuthenticator.PROVIDER_ID, getClientByAdmin(cId).getClientAuthenticatorType());
    }

    @Test
    public void testAdminClientRegisterDefaultAuthType() throws Exception {
        setupPolicyClientIdAndSecretNotAcceptableAuthType(POLICY_NAME);
        try {
            createClientByAdmin(generateSuffixedName(CLIENT_NAME), (ClientRepresentation clientRep) -> {});
            fail();
        } catch (ClientPolicyException e) {
            assertEquals(OAuthErrorException.INVALID_CLIENT_METADATA, e.getMessage());
        }
    }

    @Test
    public void testAdminClientUpdateUnacceptableAuthType() throws Exception {
        setupPolicyClientIdAndSecretNotAcceptableAuthType(POLICY_NAME);
        String cId = createClientByAdmin(generateSuffixedName(CLIENT_NAME), (ClientRepresentation clientRep) -> clientRep.setClientAuthenticatorType(JWTClientSecretAuthenticator.PROVIDER_ID));
        assertEquals(JWTClientSecretAuthenticator.PROVIDER_ID, getClientByAdmin(cId).getClientAuthenticatorType());
        try {
            updateClientByAdmin(cId, (ClientRepresentation clientRep) -> clientRep.setClientAuthenticatorType(ClientIdAndSecretAuthenticator.PROVIDER_ID));
            fail();
        } catch (ClientPolicyException cpe) {
            assertEquals(OAuthErrorException.INVALID_CLIENT_METADATA, cpe.getError());
        }
        assertEquals(JWTClientSecretAuthenticator.PROVIDER_ID, getClientByAdmin(cId).getClientAuthenticatorType());
    }

    @Test
    public void testAdminClientUpdateAcceptableAuthType() throws Exception {
        setupPolicyClientIdAndSecretNotAcceptableAuthType(POLICY_NAME);

        String cId = createClientByAdmin(generateSuffixedName(CLIENT_NAME), (ClientRepresentation clientRep) -> clientRep.setClientAuthenticatorType(JWTClientSecretAuthenticator.PROVIDER_ID));

        assertEquals(JWTClientSecretAuthenticator.PROVIDER_ID, getClientByAdmin(cId).getClientAuthenticatorType());

        updateClientByAdmin(cId, (ClientRepresentation clientRep) -> clientRep.setClientAuthenticatorType(JWTClientAuthenticator.PROVIDER_ID));
        assertEquals(JWTClientAuthenticator.PROVIDER_ID, getClientByAdmin(cId).getClientAuthenticatorType());
    }

    @Test
    public void testAdminClientUpdateDefaultAuthType() throws Exception {
        setupPolicyClientIdAndSecretNotAcceptableAuthType(POLICY_NAME);

        String cId = createClientByAdmin(generateSuffixedName(CLIENT_NAME), (ClientRepresentation clientRep) -> clientRep.setClientAuthenticatorType(JWTClientSecretAuthenticator.PROVIDER_ID));

        assertEquals(JWTClientSecretAuthenticator.PROVIDER_ID, getClientByAdmin(cId).getClientAuthenticatorType());

        updateClientByAdmin(cId, (ClientRepresentation clientRep) -> clientRep.setServiceAccountsEnabled(Boolean.FALSE));
        assertEquals(JWTClientSecretAuthenticator.PROVIDER_ID, getClientByAdmin(cId).getClientAuthenticatorType());
        assertEquals(Boolean.FALSE, getClientByAdmin(cId).isServiceAccountsEnabled());
    }

    @Test
    public void testAdminClientAutoConfiguredClientAuthType() throws Exception {
        // register profiles
        String json = (new ClientProfilesBuilder()).addProfile(
                (new ClientProfileBuilder()).createProfile(PROFILE_NAME, "Pershyy Profil")
                        .addExecutor(SecureClientAuthenticatorExecutorFactory.PROVIDER_ID,
                                createSecureClientAuthenticatorExecutorConfig(
                                        Arrays.asList(JWTClientAuthenticator.PROVIDER_ID, JWTClientSecretAuthenticator.PROVIDER_ID, X509ClientAuthenticator.PROVIDER_ID),
                                        X509ClientAuthenticator.PROVIDER_ID))
                        .toRepresentation()
        ).toString();
        updateProfiles(json);

        // register policies
        json = (new ClientPoliciesBuilder()).addPolicy(
                (new ClientPolicyBuilder()).createPolicy(POLICY_NAME, "Persha Polityka", Boolean.TRUE)
                        .addCondition(ClientUpdaterContextConditionFactory.PROVIDER_ID,
                                createClientUpdateContextConditionConfig(List.of(ClientUpdaterContextConditionFactory.BY_AUTHENTICATED_USER)))
                        .addProfile(PROFILE_NAME)
                        .toRepresentation()
        ).toString();
        updatePolicies(json);

        // Attempt to create client with set authenticator to ClientIdAndSecretAuthenticator. Should fail
        try {
            createClientByAdmin(generateSuffixedName(CLIENT_NAME), (ClientRepresentation clientRep) -> clientRep.setClientAuthenticatorType(ClientIdAndSecretAuthenticator.PROVIDER_ID));
            fail();
        } catch (ClientPolicyException e) {
            assertEquals(OAuthErrorException.INVALID_CLIENT_METADATA, e.getMessage());
        }

        // Attempt to create client without set authenticator. Default authenticator should be set
        String cId = createClientByAdmin(generateSuffixedName(CLIENT_NAME), (ClientRepresentation clientRep) -> {});

        assertEquals(X509ClientAuthenticator.PROVIDER_ID, getClientByAdmin(cId).getClientAuthenticatorType());

        // update profiles
        json = (new ClientProfilesBuilder()).addProfile(
                (new ClientProfileBuilder()).createProfile(PROFILE_NAME, "Pershyy Profil")
                        .addExecutor(SecureClientAuthenticatorExecutorFactory.PROVIDER_ID,
                                createSecureClientAuthenticatorExecutorConfig(
                                        Arrays.asList(JWTClientAuthenticator.PROVIDER_ID, JWTClientSecretAuthenticator.PROVIDER_ID, X509ClientAuthenticator.PROVIDER_ID),
                                        JWTClientAuthenticator.PROVIDER_ID))
                        .toRepresentation()
        ).toString();
        updateProfiles(json);

        // It is allowed to update authenticator to one of allowed client authenticators. Default client authenticator is not explicitly set in this case
        updateClientByAdmin(cId, (ClientRepresentation clientRep) -> clientRep.setClientAuthenticatorType(JWTClientSecretAuthenticator.PROVIDER_ID));
        assertEquals(JWTClientSecretAuthenticator.PROVIDER_ID, getClientByAdmin(cId).getClientAuthenticatorType());
    }
}
