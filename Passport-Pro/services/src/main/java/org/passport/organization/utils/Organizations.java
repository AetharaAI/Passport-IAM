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

package org.passport.organization.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.passport.TokenVerifier;
import org.passport.authentication.actiontoken.inviteorg.InviteOrgActionToken;
import org.passport.common.Profile;
import org.passport.common.Profile.Feature;
import org.passport.common.VerificationException;
import org.passport.http.HttpRequest;
import org.passport.models.Constants;
import org.passport.models.FederatedIdentityModel;
import org.passport.models.GroupModel;
import org.passport.models.GroupModel.Type;
import org.passport.models.IdentityProviderModel;
import org.passport.models.PassportContext;
import org.passport.models.PassportSession;
import org.passport.models.OrganizationDomainModel;
import org.passport.models.OrganizationModel;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.organization.OrganizationProvider;
import org.passport.organization.protocol.mappers.oidc.OrganizationScope;
import org.passport.services.ErrorResponse;
import org.passport.sessions.AuthenticationSessionModel;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

public class Organizations {

    public static boolean isOrganizationGroup(GroupModel group) {
        return Type.ORGANIZATION.equals(group.getType());
    }

    public static boolean canManageOrganizationGroup(PassportSession session, GroupModel group) {
        //  if it's not an organization group OR the feature is disabled, we don't need further checks
        if (!isOrganizationGroup(group) || !Profile.isFeatureEnabled(Feature.ORGANIZATION)) {
            return true;
        }

        // if an organization is in context, allow management
        if (resolveOrganization(session) != null) {
            return true;
        }

        // no organization in context, but the group is the internal org group
        return getProvider(session).getById(group.getName()) == null;
    }

    public static List<IdentityProviderModel> resolveHomeBroker(PassportSession session, UserModel user) {
        OrganizationProvider provider = getProvider(session);
        RealmModel realm = session.getContext().getRealm();
        List<OrganizationModel> organizations = Optional.ofNullable(user).stream().flatMap(provider::getByMember)
                .filter(OrganizationModel::isEnabled)
                .filter((org) -> org.isManaged(user))
                .toList();

        if (organizations.isEmpty()) {
            return List.of();
        }

        List<IdentityProviderModel> brokers = new ArrayList<>();

        for (OrganizationModel organization : organizations) {
            // user is a managed member, try to resolve the origin broker and redirect automatically
            List<IdentityProviderModel> organizationBrokers = organization.getIdentityProviders().toList();
            session.users().getFederatedIdentitiesStream(realm, user)
                    .map(f -> {
                        IdentityProviderModel broker = session.identityProviders().getByAlias(f.getIdentityProvider());

                        if (!organizationBrokers.contains(broker)) {
                            return null;
                        }

                        FederatedIdentityModel identity = session.users().getFederatedIdentity(realm, user, broker.getAlias());

                        if (identity != null) {
                            return broker;
                        }

                        return null;
                    }).filter(Objects::nonNull)
                    .forEach(brokers::add);
        }

        return brokers;
    }

    public static Consumer<GroupModel> removeGroup(PassportSession session, RealmModel realm) {
        return group -> {
            if (!Type.ORGANIZATION.equals(group.getType())) {
                realm.removeGroup(group);
                return;
            }

            OrganizationModel current = resolveOrganization(session);

            try {
                OrganizationProvider provider = getProvider(session);

                session.getContext().setOrganization(provider.getById(group.getName()));

                realm.removeGroup(group);
            } finally {
                session.getContext().setOrganization(current);
            }
        };
    }

    public static boolean isEnabledAndOrganizationsPresent(OrganizationProvider orgProvider) {
        return orgProvider != null && orgProvider.isEnabled() && orgProvider.count() != 0;
    }

    public static boolean isEnabledAndOrganizationsPresent(PassportSession session) {
        if (!Profile.isFeatureEnabled(Feature.ORGANIZATION)) {
            return false;
        }

        OrganizationProvider provider = getProvider(session);

        return isEnabledAndOrganizationsPresent(provider);
    }

    public static void checkEnabled(OrganizationProvider provider) {
        if (provider == null || !provider.isEnabled()) {
            throw ErrorResponse.error("Organizations not enabled for this realm.", Response.Status.NOT_FOUND);
        }
    }

    public static InviteOrgActionToken parseInvitationToken(HttpRequest request) throws VerificationException {
        MultivaluedMap<String, String> queryParameters = request.getUri().getQueryParameters();
        String tokenFromQuery = queryParameters.getFirst(Constants.TOKEN);

        if (tokenFromQuery == null) {
            return null;
        }

        return TokenVerifier.create(tokenFromQuery, InviteOrgActionToken.class).getToken();
    }

    public static String getEmailDomain(String email) {
        if (email == null) {
            return null;
        }

        int domainSeparator = email.indexOf('@');

        if (domainSeparator == -1) {
            return null;
        }

        return email.substring(domainSeparator + 1);
    }

    public static String getEmailDomain(UserModel user) {
        if (user == null) {
            return null;
        }
        return getEmailDomain(user.getEmail());
    }

    public static OrganizationModel resolveOrganization(PassportSession session) {
        return resolveOrganization(session, null, null);
    }

    public static OrganizationModel resolveOrganization(PassportSession session, UserModel user) {
        return resolveOrganization(session, user, null);
    }

    public static OrganizationModel resolveOrganization(PassportSession session, UserModel user, String domain) {
        PassportContext context = session.getContext();
        RealmModel realm = context.getRealm();

        if (!realm.isOrganizationsEnabled()) {
            return null;
        }

        OrganizationModel current = context.getOrganization();

        if (current != null) {
            // resolved from current passport session
            return current;
        }

        OrganizationProvider provider = getProvider(session);

        if (provider.count() == 0) {
            return null;
        }

        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        if (authSession != null) {
            OrganizationScope scope = OrganizationScope.valueOfScope(session);
            List<OrganizationModel> organizations = ofNullable(authSession.getAuthNote(OrganizationModel.ORGANIZATION_ATTRIBUTE))
                    .map(provider::getById)
                    .map(List::of)
                    .orElseGet(() -> scope == null ? List.of() : scope.resolveOrganizations(user, session).toList());

            if (organizations.size() == 1) {
                OrganizationModel organization = organizations.get(0);

                if (user == null) {
                    return organization;
                }

                // make sure the user still maps to the organization from the authentication session
                if (organization.isMember(user) || matchesOrganizationDomain(organization, user, domain)) {
                    return organization;
                }

                return null;
            } else if (scope != null && user != null) {
                return resolveUserOrganization(organizations, user, domain).orElse(null);
            }
        }

        List<OrganizationModel> organizations = ofNullable(user).stream()
                .flatMap(provider::getByMember)
                .filter(OrganizationModel::isEnabled)
                .toList();

        if (organizations.size() == 1) {
            return organizations.get(0);
        }

        return resolveUserOrganization(organizations, user, domain)
                .orElseGet(() -> resolveOrganizationByDomain(user, domain, provider));
    }

    public static OrganizationProvider getProvider(PassportSession session) {
        return session.getProvider(OrganizationProvider.class);
    }

    public static boolean isRegistrationAllowed(PassportSession session, RealmModel realm) {
        if (session.getContext().getOrganization() != null) return true;
        return realm.isRegistrationAllowed();
    }

    public static boolean isReadOnlyOrganizationMember(PassportSession session, UserModel delegate) {
        if (delegate == null) {
            return false;
        }

        if (!Profile.isFeatureEnabled(Profile.Feature.ORGANIZATION)) {
            return false;
        }

        var organizationProvider = getProvider(session);

        if (organizationProvider.count() == 0) {
            return false;
        }

        // check if provider is enabled and user is managed member of a disabled organization OR provider is disabled and user is managed member
        return organizationProvider.getByMember(delegate)
                .anyMatch((org) -> (organizationProvider.isEnabled() && org.isManaged(delegate) && !org.isEnabled()) ||
                        (!organizationProvider.isEnabled() && org.isManaged(delegate)));
    }

    private static boolean matchesOrganizationDomain(OrganizationModel organization, UserModel user, String domain) {
        if (organization == null) {
            return false;
        }

        String emailDomain = Optional.ofNullable(domain).orElseGet(() -> getEmailDomain(user));

        if (emailDomain == null) {
            return false;
        }

        Stream<OrganizationDomainModel> domains = organization.getDomains();

        return domains.map(OrganizationDomainModel::getName).anyMatch(emailDomain::equals);
    }

    private static OrganizationModel resolveOrganizationByDomain(UserModel user, String domain, OrganizationProvider provider) {
        if (user != null && domain == null) {
            domain = getEmailDomain(user);
        }

        return ofNullable(domain)
                .map(provider::getByDomainName)
                .orElse(null);
    }

    private static Optional<OrganizationModel> resolveUserOrganization(List<OrganizationModel> organizations, UserModel user, String domain) {
        OrganizationModel orgByDomain = null;

        for (OrganizationModel o : organizations) {
            if (o.isManaged(user)) {
                return of(o);
            }

            if (matchesOrganizationDomain(o, user, domain)) {
                orgByDomain = o;
            }
        }

        return ofNullable(orgByDomain);
    }
}
