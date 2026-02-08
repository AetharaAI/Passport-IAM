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

import java.util.stream.Stream;

import org.passport.authentication.AuthenticationFlowContext;
import org.passport.authentication.AuthenticationFlowError;
import org.passport.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.passport.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.passport.broker.provider.BrokeredIdentityContext;
import org.passport.models.IdentityProviderModel;
import org.passport.models.PassportSession;
import org.passport.models.OrganizationModel;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.organization.OrganizationProvider;
import org.passport.organization.utils.Organizations;

import static org.passport.organization.utils.Organizations.isEnabledAndOrganizationsPresent;

public class IdpAddOrganizationMemberAuthenticator extends AbstractIdpAuthenticator {

    @Override
    protected void actionImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
    }

    @Override
    protected void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        PassportSession session = context.getSession();
        OrganizationProvider provider = session.getProvider(OrganizationProvider.class);
        UserModel user = context.getUser();
        OrganizationModel organization = Organizations.resolveOrganization(session);

        if (organization == null) {
            context.attempted();
            return;
        }

        Stream<IdentityProviderModel> expectedBrokers = organization.getIdentityProviders();
        IdentityProviderModel broker = brokerContext.getIdpConfig();

        if (expectedBrokers.noneMatch(broker::equals)) {
            context.failure(AuthenticationFlowError.ACCESS_DENIED);
            return;
        }

        provider.addManagedMember(organization, user);
        context.success();
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(PassportSession session, RealmModel realm, UserModel user) {
        OrganizationProvider provider = session.getProvider(OrganizationProvider.class);

        if (!isEnabledAndOrganizationsPresent(provider)) {
            return false;
        }

        OrganizationModel organization = Organizations.resolveOrganization(session);

        if (organization == null || !organization.isEnabled()) {
            return false;
        }

        return provider.getIdentityProviders(organization).findAny().isPresent();
    }
}
