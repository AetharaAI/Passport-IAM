/*
 * Copyright 2016 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.passport.testsuite;

import java.lang.reflect.Field;
import java.util.List;

import org.passport.admin.client.resource.RealmResource;
import org.passport.common.util.reflections.Reflections;
import org.passport.events.Details;
import org.passport.models.PassportSession;
import org.passport.representations.IDToken;
import org.passport.representations.idm.ClientRepresentation;
import org.passport.representations.idm.EventRepresentation;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.representations.idm.UserRepresentation;
import org.passport.testsuite.util.oauth.AccessTokenResponse;

import org.junit.Before;

import static org.passport.testsuite.AbstractAdminTest.loadJson;

/**
 * This class provides loading of the testRealm called "test".  It also
 * provides a few utility methods for the testRealm.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2016 Red Hat Inc.
 */
public abstract class AbstractTestRealmPassportTest extends AbstractPassportTest {
    public static final String TEST_REALM_NAME = "test";

    protected RealmResource testRealm() {
        return adminClient.realm(TEST_REALM_NAME);
    }

    protected UserRepresentation findUser(String userNameOrEmail) {
        List<UserRepresentation> repList = testRealm().users().search(userNameOrEmail, -1, -1);
        if (repList.size() != 1) throw new IllegalStateException("User search expected one result. Found " + repList.size() + " users.");
        return repList.get(0);
    }

    protected void updateUser(UserRepresentation user) {
        testRealm().users().get(user.getId()).update(user);
    }

    protected ClientRepresentation findTestApp(RealmRepresentation testRealm) {
        for (ClientRepresentation client : testRealm.getClients()) {
            if (client.getClientId().equals("test-app")) return client;
        }

        return null;
    }

    @Override
    public void addTestRealms(List<RealmRepresentation> testRealms) {
        log.debug("Adding test realm for import from testrealm.json");
        RealmRepresentation testRealm = loadJson(getClass().getResourceAsStream("/testrealm.json"), RealmRepresentation.class);

        testRealms.add(testRealm);

        configureTestRealm(testRealm);
    }


    @Before
    @Override
    public void beforeAbstractPassportTest() throws Exception {
        deleteCookies();
        super.beforeAbstractPassportTest();
    }

    public void deleteCookies() {
        deleteAllCookiesForRealm("test");
    }

    /**
     * This allows a subclass to change the configuration of the testRealm before
     * it is imported.  This method will be called prior to any @Before methods
     * in the subclass.
     *
     * @param testRealm The realm read from /testrealm.json.
     */
    public abstract void configureTestRealm(RealmRepresentation testRealm);


    protected IDToken sendTokenRequestAndGetIDToken(EventRepresentation loginEvent) {

        AccessTokenResponse response = sendTokenRequestAndGetResponse(loginEvent);
        return oauth.verifyIDToken(response.getIdToken());
    }

    protected AccessTokenResponse sendTokenRequestAndGetResponse(EventRepresentation loginEvent) {

        Field eventsField = Reflections.findDeclaredField(this.getClass(), "events");
        AssertEvents events = null;
        if(eventsField != null) {
            events = Reflections.getFieldValue(eventsField, this, AssertEvents.class);
        }

        String sessionId = loginEvent.getSessionId();
        String codeId = loginEvent.getDetails().get(Details.CODE_ID);

        if(eventsField != null) {
            events.clear();
        }
        String code = oauth.parseLoginResponse().getCode();
        AccessTokenResponse response = oauth.doAccessTokenRequest(code);

        Assert.assertEquals(200, response.getStatusCode());

        if (eventsField != null) {
            events.expectCodeToToken(codeId, sessionId).user(loginEvent.getUserId()).session(sessionId).assertEvent();
        }

        return response;
    }

    /** PASSPORT-12065 Inherit Client Connection from parent session **/
    public static PassportSession inheritClientConnection(PassportSession parentSession, PassportSession currentSession) {
        currentSession.getContext().setConnection(parentSession.getContext().getConnection());
        return currentSession;
    }
}
