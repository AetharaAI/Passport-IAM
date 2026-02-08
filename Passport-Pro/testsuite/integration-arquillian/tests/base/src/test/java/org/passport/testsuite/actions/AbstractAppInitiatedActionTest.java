/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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
package org.passport.testsuite.actions;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.ws.rs.core.UriBuilder;

import org.passport.protocol.oidc.OIDCLoginProtocolService;
import org.passport.testsuite.AbstractChangeImportedUserPasswordsTest;
import org.passport.testsuite.AssertEvents;
import org.passport.testsuite.pages.AppPage;
import org.passport.testsuite.pages.AppPage.RequestType;
import org.passport.testsuite.pages.LoginPage;
import org.passport.testsuite.util.WaitUtils;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.junit.Rule;

import static org.passport.OAuth2Constants.REDIRECT_URI;
import static org.passport.OAuth2Constants.RESPONSE_TYPE;
import static org.passport.OAuth2Constants.SCOPE;
import static org.passport.models.Constants.CLIENT_ID;
import static org.passport.models.Constants.KC_ACTION;
import static org.passport.models.Constants.KC_ACTION_STATUS;
import static org.passport.testsuite.util.ServerURLs.getAuthServerContextRoot;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Stan Silvert
 */
public abstract class AbstractAppInitiatedActionTest extends AbstractChangeImportedUserPasswordsTest {

    protected static final String SUCCESS = "success";
    protected static final String CANCELLED = "cancelled";

    @Page
    protected LoginPage loginPage;
    
    @Page
    protected AppPage appPage;
    
    @Rule
    public AssertEvents events = new AssertEvents(this);

    protected abstract String getAiaAction();
    
    protected void doAIA() {
        UriBuilder builder = OIDCLoginProtocolService.authUrl(authServerPage.createUriBuilder());
        String uri = builder.queryParam(KC_ACTION, getAiaAction())
                            .queryParam(RESPONSE_TYPE, "code")
                            .queryParam(CLIENT_ID, "test-app")
                            .queryParam(SCOPE, "openid")
                            .queryParam(REDIRECT_URI, getAuthServerContextRoot() + "/auth/realms/master/app/auth")
                            .build(TEST_REALM_NAME).toString();
        driver.navigate().to(uri);
        WaitUtils.waitForPageToLoad();
    }

    protected void assertKcActionStatus(String expectedStatus) {
        assertThat(appPage.getRequestType(),is(RequestType.AUTH_RESPONSE));
        String kcActionStatus = getCurrentUrlParam(KC_ACTION_STATUS);
        assertThat(kcActionStatus, is(expectedStatus));
    }

    protected void assertKcAction(String expectedKcAction) {
        assertThat(appPage.getRequestType(),is(RequestType.AUTH_RESPONSE));
        String kcAction = getCurrentUrlParam(KC_ACTION);
        assertThat(kcAction, is(expectedKcAction));
    }

    protected String getCurrentUrlParam(String paramName) {
        final URI url;
        try {
            url = new URI(this.driver.getCurrentUrl());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        List<NameValuePair> pairs = URLEncodedUtils.parse(url, StandardCharsets.UTF_8);
        for (NameValuePair p : pairs) {
            if (p.getName().equals(paramName)) {
                return p.getValue();
            }
        }
        return null;
    }
    
    protected void assertSilentCancelMessage() {
        String url = this.driver.getCurrentUrl();
        assertThat("Expected no 'error=' in url", url, not(containsString("error=")));
        assertThat("Expected no 'error_description=' in url", url, not(containsString("error_description=")));
    }
}
