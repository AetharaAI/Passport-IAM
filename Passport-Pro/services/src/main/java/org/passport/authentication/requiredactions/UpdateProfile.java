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

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.passport.Config;
import org.passport.authentication.InitiatedActionSupport;
import org.passport.authentication.RequiredActionContext;
import org.passport.authentication.RequiredActionFactory;
import org.passport.authentication.RequiredActionProvider;
import org.passport.events.Details;
import org.passport.events.EventBuilder;
import org.passport.events.EventType;
import org.passport.forms.login.LoginFormsProvider;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.utils.FormMessage;
import org.passport.services.validation.Validation;
import org.passport.userprofile.EventAuditingAttributeChangeListener;
import org.passport.userprofile.UserProfile;
import org.passport.userprofile.UserProfileContext;
import org.passport.userprofile.UserProfileProvider;
import org.passport.userprofile.ValidationException;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UpdateProfile implements RequiredActionProvider, RequiredActionFactory {
    @Override
    public InitiatedActionSupport initiatedActionSupport() {
        return InitiatedActionSupport.SUPPORTED;
    }

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        context.challenge(createResponse(context, null, null));
    }

    @Override
    public void processAction(RequiredActionContext context) {
        EventBuilder event = context.getEvent();
        event.event(EventType.UPDATE_PROFILE).detail(Details.CONTEXT, UserProfileContext.UPDATE_PROFILE.name());
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>(context.getHttpRequest().getDecodedFormParameters());
        UserModel user = context.getUser();
        String newEmail = formData.getFirst(UserModel.EMAIL);
        boolean isEmailUpdated = newEmail != null && !ofNullable(user.getEmail()).orElse("").equals(newEmail);
        RealmModel realm = context.getRealm();
        boolean isForceEmailVerification = isEmailUpdated && UpdateEmail.isVerifyEmailEnabled(realm);

        try {
            UserProfileProvider provider = context.getSession().getProvider(UserProfileProvider.class);
            UserProfile profile = provider.create(UserProfileContext.UPDATE_PROFILE, formData, user);

            if (isForceEmailVerification) {
                user.addRequiredAction(UserModel.RequiredAction.UPDATE_EMAIL);
            }

            profile.update(false, new EventAuditingAttributeChangeListener(profile, event));

            context.success();
        } catch (ValidationException pve) {
            List<FormMessage> errors = Validation.getFormErrorsFromValidation(pve.getErrors());

            context.challenge(createResponse(context, formData, errors));
        }
    }

    protected UserModel.RequiredAction getResponseAction(){
        return UserModel.RequiredAction.UPDATE_PROFILE;
    }

    protected Response createResponse(RequiredActionContext context, MultivaluedMap<String, String> formData, List<FormMessage> errors) {
        LoginFormsProvider form = context.form();

        if (errors != null && !errors.isEmpty()) {
            form.setErrors(errors);
        }

        if(formData != null) {
            form = form.setFormData(formData);
        }

        form.setUser(context.getUser());

        return form.createResponse(getResponseAction());
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
        return "Update Profile";
    }


    @Override
    public String getId() {
        return UserModel.RequiredAction.UPDATE_PROFILE.name();
    }
}
