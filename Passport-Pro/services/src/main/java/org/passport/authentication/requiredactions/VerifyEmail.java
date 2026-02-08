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

package org.passport.authentication.requiredactions;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriBuilderException;
import jakarta.ws.rs.core.UriInfo;

import org.passport.Config;
import org.passport.authentication.AuthenticationProcessor;
import org.passport.authentication.InitiatedActionSupport;
import org.passport.authentication.RequiredActionContext;
import org.passport.authentication.RequiredActionFactory;
import org.passport.authentication.RequiredActionProvider;
import org.passport.authentication.actiontoken.verifyemail.VerifyEmailActionToken;
import org.passport.authentication.requiredactions.util.EmailCooldownManager;
import org.passport.common.util.Time;
import org.passport.email.EmailException;
import org.passport.email.EmailTemplateProvider;
import org.passport.events.Details;
import org.passport.events.Errors;
import org.passport.events.EventBuilder;
import org.passport.events.EventType;
import org.passport.forms.login.LoginFormsProvider;
import org.passport.models.Constants;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.policy.MaxAuthAgePasswordPolicyProviderFactory;
import org.passport.protocol.AuthorizationEndpointBase;
import org.passport.provider.ProviderConfigProperty;
import org.passport.services.Urls;
import org.passport.services.messages.Messages;
import org.passport.services.validation.Validation;
import org.passport.sessions.AuthenticationSessionCompoundId;
import org.passport.sessions.AuthenticationSessionModel;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class VerifyEmail implements RequiredActionProvider, RequiredActionFactory {
    public static final String EMAIL_RESEND_COOLDOWN_KEY_PREFIX = "verify-email-cooldown-";
    private static final Logger logger = Logger.getLogger(VerifyEmail.class);

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        if (context.getRealm().isVerifyEmail() && !context.getUser().isEmailVerified()) {
            // Don't add VERIFY_EMAIL if UPDATE_EMAIL is already present (UPDATE_EMAIL takes precedence)
            if (context.getUser().getRequiredActionsStream().noneMatch(action -> UserModel.RequiredAction.UPDATE_EMAIL.name().equals(action))) {
                context.getUser().addRequiredAction(UserModel.RequiredAction.VERIFY_EMAIL);
                logger.debug("User is required to verify email");
            } else {
                logger.debug("Skipping VERIFY_EMAIL because UPDATE_EMAIL is already present");
            }
        }
    }

    @Override
    public InitiatedActionSupport initiatedActionSupport() {
        return InitiatedActionSupport.SUPPORTED;
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        process(context, true);
    }

    private void process(RequiredActionContext context, boolean isChallenge) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        if (context.getUser().isEmailVerified()) {
            context.success();
            authSession.removeAuthNote(Constants.VERIFY_EMAIL_KEY);
            return;
        }

        String email = context.getUser().getEmail();
        if (Validation.isBlank(email)) {
            context.ignore();
            return;
        }

        LoginFormsProvider loginFormsProvider = context.form();
        loginFormsProvider.setAuthenticationSession(context.getAuthenticationSession());
        Response challenge;
        authSession.setClientNote(AuthorizationEndpointBase.APP_INITIATED_FLOW, null);

        // Do not allow resending e-mail by simple page refresh, i.e. when e-mail sent, it should be resent properly via email-verification endpoint
        if (!Objects.equals(authSession.getAuthNote(Constants.VERIFY_EMAIL_KEY), email) && !(isCurrentActionTriggeredFromAIA(context) && isChallenge)) {
            // Adding the cooldown entry first to prevent concurrent operations
            EmailCooldownManager.addCooldownEntry(context, EMAIL_RESEND_COOLDOWN_KEY_PREFIX);
            authSession.setAuthNote(Constants.VERIFY_EMAIL_KEY, email);
            EventBuilder event = context.getEvent().clone().event(EventType.SEND_VERIFY_EMAIL).detail(Details.EMAIL, email);
            challenge = sendVerifyEmail(context, event);
        } else {
            challenge = loginFormsProvider.createResponse(UserModel.RequiredAction.VERIFY_EMAIL);
        }

        context.challenge(challenge);
    }

    private boolean isCurrentActionTriggeredFromAIA(RequiredActionContext context) {
        return Objects.equals(context.getAuthenticationSession().getClientNote(Constants.KC_ACTION), getId());
    }

    @Override
    public void processAction(RequiredActionContext context) {
        logger.debugf("Re-sending email requested for user: %s", context.getUser().getUsername());

        Long remaining = EmailCooldownManager.retrieveCooldownEntry(context, EMAIL_RESEND_COOLDOWN_KEY_PREFIX);
        if (remaining != null) {
            Response retryPage = context.form()
                    .setError(Messages.COOLDOWN_VERIFICATION_EMAIL, remaining)
                    .createResponse(UserModel.RequiredAction.VERIFY_EMAIL); // re-render same verify email page

            context.challenge(retryPage);
            return;
        }

        // This will allow user to re-send email again
        context.getAuthenticationSession().removeAuthNote(Constants.VERIFY_EMAIL_KEY);

        process(context, false);

    }


    @Override
    public void close() {

    }

    @Override
    public RequiredActionProvider create(PassportSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(PassportSessionFactory factory) {

    }

    @Override
    public String getDisplayText() {
        return "Verify Email";
    }


    @Override
    public String getId() {
        return UserModel.RequiredAction.VERIFY_EMAIL.name();
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {

        ProviderConfigProperty maxAge = new ProviderConfigProperty();
        maxAge.setName(Constants.MAX_AUTH_AGE_KEY);
        maxAge.setLabel("Maximum Age of Authentication");
        maxAge.setHelpText("Configures the duration in seconds this action can be used after the last authentication before the user is required to re-authenticate. " +
                "This parameter is used just in the context of AIA when the kc_action parameter is available in the request, which is for instance when user " +
                "himself updates his password in the account console.");
        maxAge.setType(ProviderConfigProperty.STRING_TYPE);
        maxAge.setDefaultValue(MaxAuthAgePasswordPolicyProviderFactory.DEFAULT_MAX_AUTH_AGE);

        return List.of(maxAge, EmailCooldownManager.createCooldownConfigProperty());
    }


    private Response sendVerifyEmail(RequiredActionContext context, EventBuilder event) throws UriBuilderException, IllegalArgumentException {
        RealmModel realm = context.getRealm();
        UriInfo uriInfo = context.getUriInfo();
        UserModel user = context.getUser();
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        PassportSession session = context.getSession();

        int validityInSecs = realm.getActionTokenGeneratedByUserLifespan(VerifyEmailActionToken.TOKEN_TYPE);
        int absoluteExpirationInSecs = Time.currentTime() + validityInSecs;

        String authSessionEncodedId = AuthenticationSessionCompoundId.fromAuthSession(authSession).getEncodedId();
        VerifyEmailActionToken token = new VerifyEmailActionToken(user.getId(), absoluteExpirationInSecs, authSessionEncodedId, user.getEmail(), authSession.getClient().getClientId());
        UriBuilder builder = Urls.actionTokenBuilder(uriInfo.getBaseUri(), token.serialize(session, realm, uriInfo),
                authSession.getClient().getClientId(), authSession.getTabId(), AuthenticationProcessor.getClientData(session, authSession));
        String link = builder.build(realm.getName()).toString();
        long expirationInMinutes = TimeUnit.SECONDS.toMinutes(validityInSecs);

        try {
            session
              .getProvider(EmailTemplateProvider.class)
              .setAuthenticationSession(authSession)
              .setRealm(realm)
              .setUser(user)
              .sendVerifyEmail(link, expirationInMinutes);
            event.success();

            return context.form().createResponse(UserModel.RequiredAction.VERIFY_EMAIL);
        } catch (EmailException e) {
            event.clone().event(EventType.SEND_VERIFY_EMAIL)
                    .detail(Details.REASON, e.getMessage())
                    .user(user)
                    .error(Errors.EMAIL_SEND_FAILED);
            logger.error("Failed to send verification email", e);
            context.failure(Messages.EMAIL_SENT_ERROR);
            return context.form()
                    .setError(Messages.EMAIL_SENT_ERROR)
                    .createErrorPage(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
