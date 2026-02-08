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
package org.passport.credential;

import java.util.List;

import org.passport.common.util.ObjectUtil;
import org.passport.common.util.Time;
import org.passport.models.PassportSession;
import org.passport.models.OTPPolicy;
import org.passport.models.RealmModel;
import org.passport.models.SingleUseObjectProvider;
import org.passport.models.UserCredentialModel;
import org.passport.models.UserModel;
import org.passport.models.credential.OTPCredentialModel;
import org.passport.models.credential.dto.OTPCredentialData;
import org.passport.models.credential.dto.OTPSecretData;
import org.passport.models.utils.HmacOTP;
import org.passport.models.utils.TimeBasedOTP;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class OTPCredentialProvider implements CredentialProvider<OTPCredentialModel>, CredentialInputValidator/*, OnUserCache*/ {
    private static final Logger logger = Logger.getLogger(OTPCredentialProvider.class);

    protected PassportSession session;

    public OTPCredentialProvider(PassportSession session) {
        this.session = session;
    }

    @Override
    public CredentialModel createCredential(RealmModel realm, UserModel user, OTPCredentialModel credentialModel) {
        if (credentialModel.getCreatedDate() == null) {
            credentialModel.setCreatedDate(Time.currentTimeMillis());
        }
        return user.credentialManager().createStoredCredential(credentialModel);
    }

    @Override
    public boolean deleteCredential(RealmModel realm, UserModel user, String credentialId) {
        return user.credentialManager().removeStoredCredentialById(credentialId);
    }

    @Override
    public OTPCredentialModel getCredentialFromModel(CredentialModel model) {
        return OTPCredentialModel.createFromCredentialModel(model);
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return List.of(OTPCredentialModel.TYPE, OTPCredentialModel.TOTP, OTPCredentialModel.HOTP).contains(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        if (!supportsCredentialType(credentialType)) return false;
        return user.credentialManager().getStoredCredentialsByTypeStream(credentialType).findAny().isPresent();
    }

    public boolean isConfiguredFor(RealmModel realm, UserModel user){
        return isConfiguredFor(realm, user, getType());
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        if (!(credentialInput instanceof UserCredentialModel)) {
            logger.debug("Expected instance of UserCredentialModel for CredentialInput");
            return false;

        }
        String challengeResponse = credentialInput.getChallengeResponse();
        if (challengeResponse == null) {
            return false;
        }
        if (ObjectUtil.isBlank(credentialInput.getCredentialId())) {
            logger.debugf("CredentialId is null when validating credential of user %s", user.getUsername());
            return false;
        }

        CredentialModel credential = user.credentialManager().getStoredCredentialById(credentialInput.getCredentialId());
        OTPCredentialModel otpCredentialModel = OTPCredentialModel.createFromCredentialModel(credential);
        OTPSecretData secretData = otpCredentialModel.getOTPSecretData();
        OTPCredentialData credentialData = otpCredentialModel.getOTPCredentialData();
        OTPPolicy policy = realm.getOTPPolicy();

        if (OTPCredentialModel.HOTP.equals(credentialData.getSubType())) {
            HmacOTP validator = new HmacOTP(credentialData.getDigits(), credentialData.getAlgorithm(), policy.getLookAheadWindow());
            int counter = validator.validateHOTP(challengeResponse, otpCredentialModel.getDecodedSecret(), credentialData.getCounter());
            if (counter < 0) {
                return false;
            }
            otpCredentialModel.updateCounter(counter);
            user.credentialManager().updateStoredCredential(otpCredentialModel);
            return true;
        } else if (OTPCredentialModel.TOTP.equals(credentialData.getSubType())) {
            TimeBasedOTP validator = new TimeBasedOTP(credentialData.getAlgorithm(), credentialData.getDigits(), credentialData.getPeriod(), policy.getLookAheadWindow());
            final boolean isValid = validator.validateTOTP(challengeResponse, otpCredentialModel.getDecodedSecret());

            if (isValid) {
                if (policy.isCodeReusable()) return true;

                SingleUseObjectProvider singleUseStore = session.singleUseObjects();
                final long validLifespan = (long) credentialData.getPeriod() * (2L * policy.getLookAheadWindow() + 1);
                final String searchKey = credential.getId() + "." + challengeResponse;

                return singleUseStore.putIfAbsent(searchKey, validLifespan);
            }
        }
        return false;
    }

    @Override
    public String getType() {
        return OTPCredentialModel.TYPE;
    }

    @Override
    public CredentialTypeMetadata getCredentialTypeMetadata(CredentialTypeMetadataContext metadataContext) {
        return CredentialTypeMetadata.builder()
                .type(getType())
                .category(CredentialTypeMetadata.Category.TWO_FACTOR)
                .displayName("otp-display-name")
                .helpText("otp-help-text")
                .iconCssClass("kcAuthenticatorOTPClass")
                .createAction(UserModel.RequiredAction.CONFIGURE_TOTP.toString())
                .removeable(true)
                .build(session);
    }
}
