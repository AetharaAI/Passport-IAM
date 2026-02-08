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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.ws.rs.ForbiddenException;

import org.passport.Config;
import org.passport.authentication.AuthenticationProcessor;
import org.passport.authentication.InitiatedActionSupport;
import org.passport.authentication.RequiredActionContext;
import org.passport.authentication.RequiredActionFactory;
import org.passport.authentication.RequiredActionProvider;
import org.passport.events.Details;
import org.passport.events.Errors;
import org.passport.events.EventBuilder;
import org.passport.events.EventType;
import org.passport.models.AccountRoles;
import org.passport.models.Constants;
import org.passport.models.PassportContext;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RealmModel;
import org.passport.models.RoleModel;
import org.passport.models.UserManager;
import org.passport.models.UserModel;
import org.passport.provider.ProviderConfigProperty;
import org.passport.services.managers.AuthenticationManager;
import org.passport.services.managers.AuthenticationSessionManager;
import org.passport.services.messages.Messages;
import org.passport.sessions.AuthenticationSessionModel;

import org.jboss.logging.Logger;
public class DeleteAccount implements RequiredActionProvider, RequiredActionFactory {

  public static final String PROVIDER_ID = "delete_account";

  private static final String TRIGGERED_FROM_AIA = "triggered_from_aia";

  private static final Logger logger = Logger.getLogger(DeleteAccount.class);

    @Override
  public String getDisplayText() {
    return "Delete Account";
  }

  @Override
  public void evaluateTriggers(RequiredActionContext context) {

  }

  @Override
  public void requiredActionChallenge(RequiredActionContext context) {
      if (!clientHasDeleteAccountRole(context)) {
        context.challenge(context.form().setError(Messages.DELETE_ACCOUNT_LACK_PRIVILEDGES).createForm("error.ftl"));
        return;
      }

      context.challenge(context.form().setAttribute(TRIGGERED_FROM_AIA, isCurrentActionTriggeredFromAIA(context)).createForm("delete-account-confirm.ftl"));
  }


  @Override
  public void processAction(RequiredActionContext context) {
    PassportSession session = context.getSession();
    EventBuilder eventBuilder = context.getEvent();
    PassportContext passportContext = session.getContext();
    RealmModel realm = passportContext.getRealm();
    UserModel user = passportContext.getAuthenticationSession().getAuthenticatedUser();

    try {
      if(!clientHasDeleteAccountRole(context)) {
        throw new ForbiddenException();
      }
      boolean removed = new UserManager(session).removeUser(realm, user);

      if (removed) {
        eventBuilder.event(EventType.DELETE_ACCOUNT)
            .client(passportContext.getClient())
            .user(user)
            .detail(Details.USERNAME, user.getUsername())
            .success();

        removeAuthenticationSession(context, session);

        context.challenge(context.form()
            .setAttribute("messageHeader", "")
            .setInfo("userDeletedSuccessfully")
            .createForm("info.ftl"));
      } else {
        eventBuilder.event(EventType.DELETE_ACCOUNT)
            .client(passportContext.getClient())
            .user(user)
            .detail(Details.USERNAME, user.getUsername())
            .error("User could not be deleted");

        cleanSession(context, RequiredActionContext.KcActionStatus.ERROR);
        context.failure();
      }

    } catch (ForbiddenException forbidden) {
      logger.error("account client does not have the required roles for user deletion");
      eventBuilder.event(EventType.DELETE_ACCOUNT_ERROR)
          .client(passportContext.getClient())
          .user(passportContext.getAuthenticationSession().getAuthenticatedUser())
          .detail(Details.REASON, "does not have the required roles for user deletion")
          .error(Errors.USER_DELETE_ERROR);
      //deletingAccountForbidden
      context.challenge(context.form().setAttribute(TRIGGERED_FROM_AIA, isCurrentActionTriggeredFromAIA(context)).setError(Messages.DELETE_ACCOUNT_LACK_PRIVILEDGES).createForm("delete-account-confirm.ftl"));
    } catch (Exception exception) {
      logger.error("unexpected error happened during account deletion", exception);
      eventBuilder.event(EventType.DELETE_ACCOUNT_ERROR)
          .client(passportContext.getClient())
          .user(passportContext.getAuthenticationSession().getAuthenticatedUser())
          .detail(Details.REASON, exception.getMessage())
          .error(Errors.USER_DELETE_ERROR);
      context.challenge(context.form().setError(Messages.DELETE_ACCOUNT_ERROR).createForm("delete-account-confirm.ftl"));
    }
  }

  private void cleanSession(RequiredActionContext context, RequiredActionContext.KcActionStatus status) {
    context.getAuthenticationSession().removeRequiredAction(PROVIDER_ID);
    context.getAuthenticationSession().removeAuthNote(AuthenticationProcessor.CURRENT_AUTHENTICATION_EXECUTION);
    AuthenticationManager.setKcActionStatus(PROVIDER_ID, status, context.getAuthenticationSession());
  }

  private boolean clientHasDeleteAccountRole(RequiredActionContext context) {
    RoleModel deleteAccountRole = context.getRealm().getClientByClientId(Constants.ACCOUNT_MANAGEMENT_CLIENT_ID).getRole(AccountRoles.DELETE_ACCOUNT);
    return deleteAccountRole != null && context.getUser().hasRole(deleteAccountRole);
  }

  private boolean isCurrentActionTriggeredFromAIA(RequiredActionContext context) {
    return Objects.equals(context.getAuthenticationSession().getClientNote(Constants.KC_ACTION), PROVIDER_ID);
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
  public void close() {

  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public InitiatedActionSupport initiatedActionSupport() {
    return InitiatedActionSupport.SUPPORTED;
  }

  @Override
  public boolean isOneTimeAction() {
    return true;
  }

  @Override
  public int getMaxAuthAge(PassportSession session) {
    return 0;
  }

  @Override
  public List<ProviderConfigProperty> getConfigMetadata() {
      return Collections.emptyList();
  }

  private void removeAuthenticationSession(RequiredActionContext context, PassportSession session) {
    AuthenticationSessionModel authSession = context.getAuthenticationSession();
    new AuthenticationSessionManager(session).removeAuthenticationSession(authSession.getRealm(), authSession, true);
  }
}
