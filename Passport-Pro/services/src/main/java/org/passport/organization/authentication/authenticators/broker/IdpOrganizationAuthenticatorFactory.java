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

package org.passport.organization.authentication.authenticators.broker;

import java.util.List;

import org.passport.Config;
import org.passport.Config.Scope;
import org.passport.authentication.Authenticator;
import org.passport.authentication.AuthenticatorFactory;
import org.passport.common.Profile;
import org.passport.common.Profile.Feature;
import org.passport.models.AuthenticationExecutionModel;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.provider.EnvironmentDependentProviderFactory;
import org.passport.provider.ProviderConfigProperty;

public class IdpOrganizationAuthenticatorFactory implements AuthenticatorFactory, EnvironmentDependentProviderFactory {

    public static final String ID = "idp-add-organization-member";

    @Override
    public Authenticator create(PassportSession session) {
        return new IdpAddOrganizationMemberAuthenticator();
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(PassportSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getReferenceCategory() {
        return "organization";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getDisplayType() {
        return "Organization Member Onboard";
    }

    @Override
    public String getHelpText() {
        return "Adds a federated user as a member of an organization";
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of();
    }

    @Override
    public boolean isSupported(Scope config) {
        return Profile.isFeatureEnabled(Feature.ORGANIZATION);
    }
}
