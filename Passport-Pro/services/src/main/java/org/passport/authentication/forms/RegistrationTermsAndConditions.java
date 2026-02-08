/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.passport.authentication.forms;

import java.util.Collections;
import java.util.List;

import jakarta.ws.rs.core.MultivaluedMap;

import org.passport.Config;
import org.passport.authentication.FormAction;
import org.passport.authentication.FormActionFactory;
import org.passport.authentication.FormContext;
import org.passport.authentication.ValidationContext;
import org.passport.events.Errors;
import org.passport.forms.login.LoginFormsProvider;
import org.passport.models.AuthenticationExecutionModel;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.utils.FormMessage;
import org.passport.provider.ConfiguredProvider;
import org.passport.provider.ProviderConfigProperty;

public class RegistrationTermsAndConditions implements FormAction, FormActionFactory, ConfiguredProvider {

	public static final String PROVIDER_ID = "registration-terms-and-conditions";

	protected static final String FIELD = "termsAccepted";

	@Override
	public String getDisplayType() {
		return "Terms and conditions";
	}

	@Override
	public String getReferenceCategory() {
		return "terms-and-conditions";
	}

	@Override
	public boolean isConfigurable() {
		return false;
	}

	private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
			AuthenticationExecutionModel.Requirement.REQUIRED,
			AuthenticationExecutionModel.Requirement.DISABLED
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
	public void buildPage(FormContext context, LoginFormsProvider form) {
		form.setAttribute("termsAcceptanceRequired", true);
	}

	@Override
	public void validate(ValidationContext context) {
		MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
		if (formData.containsKey(FIELD)) {
			context.success();
			return;
		}

		context.error(Errors.INVALID_REGISTRATION);
		context.validationError(formData, Collections.singletonList(new FormMessage(FIELD, "termsAcceptanceRequired")));
	}

	@Override
	public void success(FormContext context) {

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
	public String getHelpText() {
		return "Asks the user to accept terms and conditions before submitting its registration form.";
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return Collections.emptyList();
	}

	@Override
	public FormAction create(PassportSession session) {
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
}
