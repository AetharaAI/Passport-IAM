/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.passport.testsuite.forms;

import java.util.List;

import org.passport.Config;
import org.passport.authentication.AuthenticationFlowContext;
import org.passport.authentication.AuthenticationFlowError;
import org.passport.authentication.Authenticator;
import org.passport.authentication.AuthenticatorFactory;
import org.passport.models.AuthenticationExecutionModel;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.utils.PassportModelUtils;
import org.passport.provider.ProviderConfigProperty;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PassThroughAuthenticator implements Authenticator, AuthenticatorFactory {
    public static final String PROVIDER_ID = "testsuite-dummy-passthrough";
    public static String username = "test-user@localhost";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel user = PassportModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), username);
        if (user == null) {
            context.failure(AuthenticationFlowError.UNKNOWN_USER);
            return;
        }
        context.setUser(user);
        context.success();
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(PassportSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(PassportSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void action(AuthenticationFlowContext context) {

    }

   @Override
    public String getDisplayType() {
        return "Testsuite Dummy Pass Thru";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Testsuite Dummy authenticator.  Just passes through and is hardcoded to a specific user";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public Authenticator create(PassportSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(PassportSessionFactory factory) {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
