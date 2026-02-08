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
 */

package org.passport.authentication.authenticators.conditional;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.passport.authentication.AuthenticationFlowContext;
import org.passport.authentication.AuthenticationFlowError;
import org.passport.authentication.AuthenticationFlowException;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.utils.PassportModelUtils;

public class ConditionalUserAttributeValue implements ConditionalAuthenticator {

    static final ConditionalUserAttributeValue SINGLETON = new ConditionalUserAttributeValue();

    @Override
    public boolean matchCondition(AuthenticationFlowContext context) {
        // Retrieve configuration
        Map<String, String> config = context.getAuthenticatorConfig().getConfig();
        String attributeName = config.get(ConditionalUserAttributeValueFactory.CONF_ATTRIBUTE_NAME);
        String attributeValue = config.get(ConditionalUserAttributeValueFactory.CONF_ATTRIBUTE_EXPECTED_VALUE);
        boolean includeGroupAttributes = Boolean.parseBoolean(config.get(ConditionalUserAttributeValueFactory.CONF_INCLUDE_GROUP_ATTRIBUTES));
        boolean negateOutput = Boolean.parseBoolean(config.get(ConditionalUserAttributeValueFactory.CONF_NOT));
        boolean regexOutput = Boolean.parseBoolean(config.get(ConditionalUserAttributeValueFactory.REGEX));

        UserModel user = context.getUser();
        if (user == null) {
            throw new AuthenticationFlowException("Cannot find user for obtaining particular user attributes. Authenticator: " + ConditionalUserAttributeValueFactory.PROVIDER_ID, AuthenticationFlowError.UNKNOWN_USER);
        }

        boolean result = user.getAttributeStream(attributeName).anyMatch(attr -> regexOutput ? Pattern.compile(attributeValue).matcher(attr).matches() : Objects.equals(attr, attributeValue));
        if (!result && includeGroupAttributes) {
            result = PassportModelUtils.resolveAttribute(user, attributeName, true).stream().anyMatch(attr -> regexOutput ? Pattern.compile(attributeValue).matcher(attr).matches() : Objects.equals(attr, attributeValue));
        }
        return negateOutput != result;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // Not used
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public void setRequiredActions(PassportSession session, RealmModel realm, UserModel user) {
        // Not used
    }

    @Override
    public void close() {
        // Does nothing
    }
}
