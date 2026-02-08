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
package org.passport.forms.login.freemarker.model;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.UriBuilder;

import org.passport.authentication.otp.OTPApplicationProvider;
import org.passport.credential.CredentialModel;
import org.passport.models.PassportSession;
import org.passport.models.OTPPolicy;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.credential.OTPCredentialModel;
import org.passport.models.utils.HmacOTP;
import org.passport.utils.TotpUtils;

/**
 * Used for UpdateTotp required action
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class TotpBean {

    private PassportSession session;
    private final RealmModel realm;
    private final String totpSecret;
    private final String totpSecretEncoded;
    private final String totpSecretQrCode;
    private final boolean enabled;
    private UriBuilder uriBuilder;
    private final List<CredentialModel> otpCredentials;
    private final List<String> supportedApplications;
    private final UserModel user;

    public TotpBean(PassportSession session, RealmModel realm, UserModel user, UriBuilder uriBuilder) {
        this(session, realm, user, uriBuilder, null);
    }

    public TotpBean(PassportSession session, RealmModel realm, UserModel user, UriBuilder uriBuilder, String secret) {
        this.session = session;
        this.realm = realm;
        this.user = user;
        this.uriBuilder = uriBuilder;
        this.enabled = user.credentialManager().isConfiguredFor(OTPCredentialModel.TYPE);
        if (enabled) {
            otpCredentials = user.credentialManager().getStoredCredentialsByTypeStream(OTPCredentialModel.TYPE)
                    .collect(Collectors.toList());
        } else {
            otpCredentials = Collections.EMPTY_LIST;
        }
        if (secret == null) {
            this.totpSecret = HmacOTP.generateSecret(20);
        } else {
            this.totpSecret = secret;
        }
        this.totpSecretEncoded = TotpUtils.encode(totpSecret);
        this.totpSecretQrCode = TotpUtils.qrCode(totpSecret, realm, user);

        OTPPolicy otpPolicy = realm.getOTPPolicy();
        this.supportedApplications = session.getAllProviders(OTPApplicationProvider.class).stream()
                .filter(p -> p.supports(otpPolicy))
                .map(OTPApplicationProvider::getName)
                .collect(Collectors.toList());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getTotpSecret() {
        return totpSecret;
    }

    public String getTotpSecretEncoded() {
        return totpSecretEncoded;
    }

    public String getTotpSecretQrCode() {
        return totpSecretQrCode;
    }

    public String getManualUrl() {
        return uriBuilder.replaceQueryParam("session_code").replaceQueryParam("mode", "manual")
            .replaceQueryParam("execution", UserModel.RequiredAction.CONFIGURE_TOTP.name()).build().toString();
    }

    public String getQrUrl() {
        return uriBuilder.replaceQueryParam("session_code").replaceQueryParam("mode", "qr").build().toString();
    }

    public OTPPolicy getPolicy() {
        return realm.getOTPPolicy();
    }

    public List<String> getSupportedApplications() {
        return supportedApplications;
    }

    public List<CredentialModel> getOtpCredentials() {
        return otpCredentials;
    }

    public String getUsername() {
        return user.getUsername();
    }

}
