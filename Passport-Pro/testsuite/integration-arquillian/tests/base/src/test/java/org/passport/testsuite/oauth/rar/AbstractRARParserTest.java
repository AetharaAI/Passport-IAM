/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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
 *
 */
package org.passport.testsuite.oauth.rar;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.passport.OAuth2Constants;
import org.passport.common.Profile;
import org.passport.models.AuthenticatedClientSessionModel;
import org.passport.models.ClientModel;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.UserSessionModel;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.representations.idm.UserRepresentation;
import org.passport.services.util.AuthorizationContextUtil;
import org.passport.testsuite.AbstractTestRealmPassportTest;
import org.passport.testsuite.AssertEvents;
import org.passport.testsuite.arquillian.annotation.EnableFeature;
import org.passport.testsuite.util.ClientManager;
import org.passport.testsuite.util.RealmBuilder;
import org.passport.testsuite.util.UserBuilder;

import org.junit.Before;
import org.junit.Rule;

import static org.junit.Assert.assertNotNull;


/**
 * An abstract class that prepares the environment to test Dynamic Scopes (And RAR in the future)
 *
 * @author <a href="mailto:dgozalob@redhat.com">Daniel Gozalo</a>
 */
@EnableFeature(value = Profile.Feature.DYNAMIC_SCOPES, skipRestart = true)
public abstract class AbstractRARParserTest extends AbstractTestRealmPassportTest {

    @Rule
    public AssertEvents events = new AssertEvents(this);

    protected static String userId;

    @Override
    public void configureTestRealm(RealmRepresentation testRealm) {
        UserRepresentation user = UserBuilder.create()
                .id(UUID.randomUUID().toString())
                .username("rar-test")
                .email("rar@test.com")
                .enabled(true)
                .password("password")
                .build();

        RealmBuilder.edit(testRealm)
                .user(user);
    }

    @Override
    public void importTestRealms() {
        super.importTestRealms();
        userId = adminClient.realm("test").users().search("rar-test", true).get(0).getId();
    }

    @Before
    public void clientConfiguration() {
        ClientManager.realm(adminClient.realm("test")).clientId("test-app").directAccessGrant(true);
        oauth.clientId("test-app");
        oauth.scope(null);
    }

    /**
     * Fetch the {@link org.passport.rar.AuthorizationRequestContext} for the current Client session from the server
     * then create a local representation of the data to avoid an infinite recursion when trying to serialize the
     * ClientScopeModel object.
     *
     * @return the {@link AuthorizationRequestContextHolder} local testsuite representation of the Authorization Request Context
     * with all the parsed authorization_detail objects.
     */
    protected AuthorizationRequestContextHolder fetchAuthorizationRequestContextHolder(String userId) {
        AuthorizationRequestContextHolder authorizationRequestContextHolder = testingClient.server("test").fetch(session -> {
            final RealmModel realm = session.realms().getRealmByName("test");
            final UserModel user = session.users().getUserById(realm, userId);
            final UserSessionModel userSession = session.sessions().getUserSessionsStream(realm, user).findFirst().get();
            final ClientModel client = realm.getClientByClientId("test-app");
            String clientUUID = client.getId();
            AuthenticatedClientSessionModel clientSession = userSession.getAuthenticatedClientSessionByClient(clientUUID);
            session.getContext().setClient(client);
            List<AuthorizationRequestContextHolder.AuthorizationRequestHolder> authorizationRequestHolders = AuthorizationContextUtil.getAuthorizationRequestContextFromScopes(session, clientSession.getNote(OAuth2Constants.SCOPE))
                    .getAuthorizationDetailEntries().stream()
                    .map(AuthorizationRequestContextHolder.AuthorizationRequestHolder::new)
                    .collect(Collectors.toList());
            return new AuthorizationRequestContextHolder(authorizationRequestHolders);
        }, AuthorizationRequestContextHolder.class);
        assertNotNull("the fetched AuthorizationRequestContext can't be null", authorizationRequestContextHolder);
        return authorizationRequestContextHolder;
    }

}
