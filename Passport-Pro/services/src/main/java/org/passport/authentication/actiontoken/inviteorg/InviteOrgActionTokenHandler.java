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
package org.passport.authentication.actiontoken.inviteorg;

import java.net.URI;
import java.util.Objects;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.passport.TokenVerifier.Predicate;
import org.passport.authentication.AuthenticationProcessor;
import org.passport.authentication.actiontoken.AbstractActionTokenHandler;
import org.passport.authentication.actiontoken.ActionTokenContext;
import org.passport.authentication.actiontoken.TokenUtils;
import org.passport.events.Details;
import org.passport.events.Errors;
import org.passport.events.EventBuilder;
import org.passport.events.EventType;
import org.passport.forms.login.LoginFormsProvider;
import org.passport.models.Constants;
import org.passport.models.PassportSession;
import org.passport.models.OrganizationInvitationModel;
import org.passport.models.OrganizationModel;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.organization.InvitationManager;
import org.passport.organization.OrganizationProvider;
import org.passport.protocol.oidc.OIDCLoginProtocol;
import org.passport.services.Urls;
import org.passport.services.managers.AuthenticationManager;
import org.passport.services.messages.Messages;
import org.passport.sessions.AuthenticationSessionCompoundId;
import org.passport.sessions.AuthenticationSessionModel;

/**
 * Action token handler for handling invitation of an existing user to an organization. A new user is handled in registration {@link org.passport.services.resources.LoginActionsService}.
 */
public class InviteOrgActionTokenHandler extends AbstractActionTokenHandler<InviteOrgActionToken> {

    public InviteOrgActionTokenHandler() {
        super(
          InviteOrgActionToken.TOKEN_TYPE,
          InviteOrgActionToken.class,
          Messages.STALE_INVITE_ORG_LINK,
          EventType.INVITE_ORG,
          Errors.INVALID_TOKEN
        );
    }

    @Override
    public Predicate<? super InviteOrgActionToken>[] getVerifiers(ActionTokenContext<InviteOrgActionToken> tokenContext) {
        return TokenUtils.predicates(
          TokenUtils.checkThat(
            t -> Objects.equals(t.getEmail(), tokenContext.getAuthenticationSession().getAuthenticatedUser().getEmail()),
            Errors.INVALID_EMAIL, getDefaultErrorMessage()
          )
        );
    }

    @Override
    public Response preHandleToken(InviteOrgActionToken token, ActionTokenContext<InviteOrgActionToken> tokenContext) {
        PassportSession session = tokenContext.getSession();
        OrganizationProvider orgProvider = session.getProvider(OrganizationProvider.class);
        OrganizationModel organization = orgProvider.getById(token.getOrgId());

        if (organization == null) {
            return invalidOrganizationResponse(tokenContext, token);
        }

        session.getContext().setOrganization(organization);

        InvitationManager invitationManager = orgProvider.getInvitationManager();
        OrganizationInvitationModel invitation = invitationManager.getById(token.getId());

        if (invitation == null || invitation.isExpired()) {
            return invalidTokenResponse(tokenContext, token);
        }

        return super.preHandleToken(token, tokenContext);
    }

    @Override
    public Response handleToken(InviteOrgActionToken token, ActionTokenContext<InviteOrgActionToken> tokenContext) {
        UserModel user = tokenContext.getAuthenticationSession().getAuthenticatedUser();
        PassportSession session = tokenContext.getSession();
        OrganizationProvider orgProvider = session.getProvider(OrganizationProvider.class);
        AuthenticationSessionModel authSession = tokenContext.getAuthenticationSession();
        EventBuilder event = tokenContext.getEvent();

        event.event(EventType.INVITE_ORG).detail(Details.USERNAME, user.getUsername());

        OrganizationModel organization = orgProvider.getById(token.getOrgId());

        if (organization == null) {
            return invalidOrganizationResponse(tokenContext, token);
        }

        if (organization.isMember(user)) {
            return alreadyMemberResponse(organization, user, tokenContext, token);
        }

        InvitationManager invitationManager = orgProvider.getInvitationManager();
        OrganizationInvitationModel invitation = invitationManager.getById(token.getId());

        if (invitation == null || invitation.isExpired()) {
            return invalidTokenResponse(tokenContext, token);
        }

        UriInfo uriInfo = tokenContext.getUriInfo();
        RealmModel realm = tokenContext.getRealm();

        if (tokenContext.isAuthenticationSessionFresh()) {
            return confirmMembershipResponse(organization, user, tokenContext, token);
        }

        // if we made it this far then go ahead and add the user to the organization
        orgProvider.addMember(orgProvider.getById(token.getOrgId()), user);

        // Delete the invitation since it has been used
        invitationManager.remove(token.getId());

        String redirectUri = token.getRedirectUri();

        if (redirectUri != null) {
            authSession.setAuthNote(AuthenticationManager.SET_REDIRECT_URI_AFTER_REQUIRED_ACTIONS, "true");
            authSession.setRedirectUri(redirectUri);
            authSession.setClientNote(OIDCLoginProtocol.REDIRECT_URI_PARAM, redirectUri);
        }

        event.success();

        tokenContext.setEvent(event.clone().removeDetail(Details.EMAIL).event(EventType.LOGIN));

        String nextAction = AuthenticationManager.nextRequiredAction(session, authSession, tokenContext.getRequest(), event);

        if (nextAction == null) {
            // do not show account updated page
            authSession.removeAuthNote(AuthenticationManager.END_AFTER_REQUIRED_ACTIONS);

            if (redirectUri != null) {
                // always redirect to the expected URI if provided
                return Response.status(Status.FOUND).location(URI.create(redirectUri)).build();
            }
        }

        return AuthenticationManager.redirectToRequiredActions(session, realm, authSession, uriInfo, nextAction);
    }

    private Response invalidTokenResponse(ActionTokenContext<InviteOrgActionToken> tokenContext, InviteOrgActionToken token) {
        EventBuilder event = tokenContext.getEvent();
        PassportSession session = tokenContext.getSession();
        AuthenticationSessionModel authSession = tokenContext.getAuthenticationSession();

        event.detail(Details.TOKEN_ID, token.getId())
                .detail(Details.EMAIL, token.getEmail())
                .detail(Details.ORG_ID, token.getOrgId())
                .error(Errors.INVALID_TOKEN);
        return session.getProvider(LoginFormsProvider.class)
                .setAuthenticationSession(authSession)
                .setAttribute("messageHeader", Messages.EXPIRED_ACTION)
                .setInfo(Messages.STALE_INVITE_ORG_LINK)
                .createInfoPage();
    }

    private Response invalidOrganizationResponse(ActionTokenContext<InviteOrgActionToken> tokenContext, InviteOrgActionToken token) {
        EventBuilder event = tokenContext.getEvent();
        PassportSession session = tokenContext.getSession();
        AuthenticationSessionModel authSession = tokenContext.getAuthenticationSession();

        event.detail(Details.TOKEN_ID, token.getId())
                .detail(Details.EMAIL, token.getEmail())
                .detail(Details.ORG_ID, token.getOrgId())
                .error(Errors.ORG_NOT_FOUND);
        return session.getProvider(LoginFormsProvider.class)
                .setAuthenticationSession(authSession)
                .setAttribute("messageHeader", Messages.EXPIRED_ACTION)
                .setInfo(Messages.ORG_NOT_FOUND, token.getOrgId())
                .createInfoPage();
    }

    private Response alreadyMemberResponse(OrganizationModel organization, UserModel user, ActionTokenContext<InviteOrgActionToken> tokenContext, InviteOrgActionToken token) {
        EventBuilder event = tokenContext.getEvent();
        PassportSession session = tokenContext.getSession();
        AuthenticationSessionModel authSession = tokenContext.getAuthenticationSession();

        event.detail(Details.TOKEN_ID, token.getId())
                .detail(Details.EMAIL, token.getEmail())
                .detail(Details.ORG_ID, token.getOrgId())
                .error(Errors.USER_ORG_MEMBER_ALREADY);
        return session.getProvider(LoginFormsProvider.class)
                .setAuthenticationSession(authSession)
                .setAttribute("messageHeader", Messages.EXPIRED_ACTION)
                .setInfo(Messages.ORG_MEMBER_ALREADY, user.getUsername(), organization.getName())
                .setAttribute("pageRedirectUri", organization.getRedirectUrl())
                .createInfoPage();
    }

    private Response confirmMembershipResponse(OrganizationModel organization, UserModel user, ActionTokenContext<InviteOrgActionToken> tokenContext, InviteOrgActionToken token) {
        PassportSession session = tokenContext.getSession();
        AuthenticationSessionModel authSession = tokenContext.getAuthenticationSession();
        UriInfo uriInfo = tokenContext.getUriInfo();
        String authSessionEncodedId = AuthenticationSessionCompoundId.fromAuthSession(authSession).getEncodedId();
        token.setCompoundAuthenticationSessionId(authSessionEncodedId);
        RealmModel realm = tokenContext.getRealm();
        UriBuilder builder = Urls.actionTokenBuilder(uriInfo.getBaseUri(), token.serialize(session, realm, uriInfo),
                authSession.getClient().getClientId(), authSession.getTabId(), AuthenticationProcessor.getClientData(session, authSession));
        String confirmUri = builder.build(realm.getName()).toString();

        return session.getProvider(LoginFormsProvider.class)
                .setAuthenticationSession(authSession)
                .setSuccess(Messages.CONFIRM_ORGANIZATION_MEMBERSHIP, organization.getName())
                .setAttribute("messageHeader", Messages.CONFIRM_ORGANIZATION_MEMBERSHIP_TITLE)
                .setAttribute(Constants.TEMPLATE_ATTR_ACTION_URI, confirmUri)
                .setAttribute(OrganizationModel.ORGANIZATION_NAME_ATTRIBUTE, organization.getName())
                .createInfoPage();
    }
}
