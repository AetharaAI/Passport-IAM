/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.passport.forms.login.freemarker.model;

import java.util.List;
import java.util.stream.Collectors;

import org.passport.authentication.authenticators.browser.OTPFormAuthenticator;
import org.passport.credential.CredentialModel;
import org.passport.credential.CredentialProvider;
import org.passport.credential.OTPCredentialProvider;
import org.passport.credential.OTPCredentialProviderFactory;
import org.passport.models.PassportSession;
import org.passport.models.OTPPolicy;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.credential.OTPCredentialModel;

/**
 * Used for TOTP login
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class TotpLoginBean {

    private final String selectedCredentialId;
    private final List<OTPCredential> userOtpCredentials;
    private OTPPolicy policy;

    public TotpLoginBean(PassportSession session, RealmModel realm, UserModel user, String selectedCredentialId) {

        this.userOtpCredentials = user.credentialManager().getStoredCredentialsByTypeStream(OTPCredentialModel.TYPE)
                .map(OTPCredential::new)
                .collect(Collectors.toList());

        // This means user did not yet manually selected any OTP credential through the UI. So just go with the default one with biggest priority
        if (selectedCredentialId == null || selectedCredentialId.isEmpty()) {
            OTPCredentialProvider otpCredentialProvider = (OTPCredentialProvider)session.getProvider(CredentialProvider.class, OTPCredentialProviderFactory.PROVIDER_ID);
            OTPCredentialModel otpCredential = otpCredentialProvider
                    .getDefaultCredential(session, realm, user);

            selectedCredentialId = otpCredential==null ? null : otpCredential.getId();
        }

        this.selectedCredentialId = selectedCredentialId;
        this.policy = realm.getOTPPolicy();
    }


    public List<OTPCredential> getUserOtpCredentials() {
        return userOtpCredentials;
    }

    public String getSelectedCredentialId() {
        return selectedCredentialId;
    }

    public OTPPolicy getPolicy() {
        return policy;
    }

    public static class OTPCredential {

        private final String id;
        private final String userLabel;

        public OTPCredential(CredentialModel credentialModel) {
            this.id = credentialModel.getId();
            // TODO: "Unnamed" OTP credentials should be displayed in the UI in gray
            this.userLabel = credentialModel.getUserLabel() == null || credentialModel.getUserLabel().isEmpty() ? OTPFormAuthenticator.UNNAMED : credentialModel.getUserLabel();
        }

        public String getId() {
            return id;
        }

        public String getUserLabel() {
            return userLabel;
        }
    }
}
