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

package org.passport.utils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.passport.authentication.Authenticator;
import org.passport.authentication.AuthenticatorFactory;
import org.passport.authentication.ClientAuthenticator;
import org.passport.authentication.ClientAuthenticatorFactory;
import org.passport.authentication.ConfigurableAuthenticatorFactory;
import org.passport.authentication.FormAction;
import org.passport.authentication.FormActionFactory;
import org.passport.credential.CredentialModel;
import org.passport.credential.CredentialProvider;
import org.passport.models.AuthenticationExecutionModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserCredentialModel;
import org.passport.models.UserModel;
import org.passport.models.credential.OTPCredentialModel;
import org.passport.models.credential.RecoveryAuthnCodesCredentialModel;
import org.passport.representations.idm.CredentialRepresentation;
import org.passport.util.JsonSerialization;

import org.jboss.logging.Logger;

/**
 * used to set an execution a state based on type.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class CredentialHelper {

    private static final Logger logger = Logger.getLogger(CredentialHelper.class);

    public static void setOrReplaceAuthenticationRequirement(PassportSession session, RealmModel realm, String type, AuthenticationExecutionModel.Requirement requirement, AuthenticationExecutionModel.Requirement currentRequirement) {
        realm.getAuthenticationFlowsStream().forEach(flow -> realm.getAuthenticationExecutionsStream(flow.getId())
                .filter(exe -> {
                    ConfigurableAuthenticatorFactory factory = getConfigurableAuthenticatorFactory(session, exe.getAuthenticator());
                    return Objects.nonNull(factory) && Objects.equals(type, factory.getReferenceCategory());
                })
                .filter(exe -> {
                    if (Objects.isNull(currentRequirement) || Objects.equals(exe.getRequirement(), currentRequirement))
                        return true;
                    else {
                        logger.debugf("Skip switch authenticator execution '%s' to '%s' as it's in state %s",
                                exe.getAuthenticator(), requirement.toString(), exe.getRequirement());
                        return false;
                    }
                })
                .forEachOrdered(exe -> {
                    exe.setRequirement(requirement);
                    realm.updateAuthenticatorExecution(exe);
                    logger.debugf("Authenticator execution '%s' switched to '%s'", exe.getAuthenticator(), requirement.toString());
                }));
    }

    public static ConfigurableAuthenticatorFactory getConfigurableAuthenticatorFactory(PassportSession session, String providerId) {
        ConfigurableAuthenticatorFactory factory = (AuthenticatorFactory)session.getPassportSessionFactory().getProviderFactory(Authenticator.class, providerId);
        if (factory == null) {
            factory = (FormActionFactory)session.getPassportSessionFactory().getProviderFactory(FormAction.class, providerId);
        }
        if (factory == null) {
            factory = (ClientAuthenticatorFactory)session.getPassportSessionFactory().getProviderFactory(ClientAuthenticator.class, providerId);
        }
        return factory;
    }

    /**
     * Create OTP credential either in userStorage or local storage (Passport DB)
     *
     * @return true if credential was successfully created either in the user storage or Passport DB. False if error happened (EG. during HOTP validation)
     */
    public static boolean createOTPCredential(PassportSession session, RealmModel realm, UserModel user, String totpCode, OTPCredentialModel credentialModel) {
        CredentialProvider otpCredentialProvider = session.getProvider(CredentialProvider.class, "passport-otp");
        String totpSecret = credentialModel.getOTPSecretData().getValue();

        UserCredentialModel otpUserCredential = new UserCredentialModel("", realm.getOTPPolicy().getType(), totpSecret);
        boolean userStorageCreated = user.credentialManager().updateCredential(otpUserCredential);

        String credentialId = null;
        if (userStorageCreated) {
            logger.debugf("Created OTP credential for user '%s' in the user storage", user.getUsername());
        } else {
            CredentialModel createdCredential = otpCredentialProvider.createCredential(realm, user, credentialModel);
            credentialId = createdCredential.getId();
        }

        //If the type is HOTP, call verify once to consume the OTP used for registration and increase the counter.
        UserCredentialModel credential = new UserCredentialModel(credentialId, otpCredentialProvider.getType(), totpCode);
        return user.credentialManager().isValid(credential);
    }

    /**
     * Create RecoveryCodes credential either in userStorage or local storage (Passport DB)
     */
    public static void createRecoveryCodesCredential(PassportSession session, RealmModel realm, UserModel user, RecoveryAuthnCodesCredentialModel credentialModel, List<String> generatedCodes) {
        var recoveryCodeCredentialProvider = session.getProvider(CredentialProvider.class, "passport-recovery-authn-codes");
        String recoveryCodesJson;
        try {
            recoveryCodesJson =  JsonSerialization.writeValueAsString(generatedCodes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UserCredentialModel recoveryCodesCredential = new UserCredentialModel("", credentialModel.getType(), recoveryCodesJson);

        boolean userStorageCreated = user.credentialManager().updateCredential(recoveryCodesCredential);
        if (userStorageCreated) {
            logger.debugf("Created RecoveryCodes credential for user '%s' in the user storage", user.getUsername());
        } else {
            recoveryCodeCredentialProvider.createCredential(realm, user, credentialModel);
        }
    }

    /**
     * Create "dummy" representation of the credential. Typically used when credential is provided by userStorage and we don't know further
     * details about the credential besides the type
     *
     * @param credentialProviderType
     * @return dummy credential
     */
    public static CredentialRepresentation createUserStorageCredentialRepresentation(String credentialProviderType) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setId(credentialProviderType + "-id");
        credential.setType(credentialProviderType);
        credential.setCreatedDate(-1L);
        credential.setPriority(0);
        return credential;
    }
}
