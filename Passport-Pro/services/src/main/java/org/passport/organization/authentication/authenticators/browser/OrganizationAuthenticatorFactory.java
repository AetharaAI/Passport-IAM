/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.passport.organization.authentication.authenticators.browser;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.passport.Config.Scope;
import org.passport.authentication.Authenticator;
import org.passport.authentication.authenticators.browser.IdentityProviderAuthenticatorFactory;
import org.passport.authentication.authenticators.browser.WebAuthnConditionalUIAuthenticator;
import org.passport.common.Profile;
import org.passport.common.Profile.Feature;
import org.passport.models.PassportSession;
import org.passport.models.credential.WebAuthnCredentialModel;
import org.passport.provider.EnvironmentDependentProviderFactory;
import org.passport.provider.ProviderConfigProperty;

import static org.passport.provider.ProviderConfigProperty.BOOLEAN_TYPE;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class OrganizationAuthenticatorFactory extends IdentityProviderAuthenticatorFactory implements EnvironmentDependentProviderFactory {

    public static final String ID = "organization";
    public static final String REQUIRES_USER_MEMBERSHIP = "requiresUserMembership";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayType() {
        return "Organization Identity-First Login";
    }

    @Override
    public String getHelpText() {
        return "If organizations are enabled, automatically redirects users to the corresponding identity provider.";
    }

    @Override
    public Authenticator create(PassportSession session) {
        return new OrganizationAuthenticator(session);
    }

    @Override
    public boolean isSupported(Scope config) {
        return Profile.isFeatureEnabled(Feature.ORGANIZATION);
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.singletonList(new ProviderConfigProperty(REQUIRES_USER_MEMBERSHIP, "Requires user membership", "Enforces that users authenticating in the scope of an organization are members. If not a member, the user won't be able to proceed authenticating to the realm", BOOLEAN_TYPE, null));
    }

    @Override
    public Set<String> getOptionalReferenceCategories(PassportSession session) {
        return WebAuthnConditionalUIAuthenticator.isPasskeysEnabled(session)
                ? Collections.singleton(WebAuthnCredentialModel.TYPE_PASSWORDLESS)
                : super.getOptionalReferenceCategories(session);
    }
}
