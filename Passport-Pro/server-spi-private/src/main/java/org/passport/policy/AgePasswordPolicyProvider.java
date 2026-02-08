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

package org.passport.policy;

import java.time.Duration;

import org.passport.common.util.Time;
import org.passport.credential.hash.PasswordHashProvider;
import org.passport.models.PassportSession;
import org.passport.models.PasswordPolicy;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.credential.PasswordCredentialModel;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:dev.maciej.mierzwa@gmail.com">Maciej Mierzwa</a>
 */
public class AgePasswordPolicyProvider implements PasswordPolicyProvider {
    private static final String ERROR_MESSAGE = "invalidPasswordGenericMessage";
    public static final Logger logger = Logger.getLogger(AgePasswordPolicyProvider.class);
    private final PassportSession session;

    public AgePasswordPolicyProvider(PassportSession session) {
        this.session = session;
    }

    @Override
    public PolicyError validate(String user, String password) {
        return null;
    }

    @Override
    public PolicyError validate(RealmModel realm, UserModel user, String password) {
        PasswordPolicy policy = session.getContext().getRealm().getPasswordPolicy();
        int passwordAgePolicyValue = policy.getPolicyConfig(PasswordPolicy.PASSWORD_AGE);

        if (passwordAgePolicyValue != -1) {
            //current password check
            if (user.credentialManager().getStoredCredentialsByTypeStream(PasswordCredentialModel.TYPE)
                    .map(PasswordCredentialModel::createFromCredentialModel)
                    .anyMatch(passwordCredential -> {
                        PasswordHashProvider hash = session.getProvider(PasswordHashProvider.class,
                                passwordCredential.getPasswordCredentialData().getAlgorithm());
                        return hash != null && hash.verify(password, passwordCredential);
                    })) {
                return new PolicyError(ERROR_MESSAGE, passwordAgePolicyValue);
            }

            final long passwordMaxAgeMillis = Time.currentTimeMillis() - Duration.ofDays(passwordAgePolicyValue).toMillis();
            if (passwordAgePolicyValue > 0) {
                if (user.credentialManager().getStoredCredentialsByTypeStream(PasswordCredentialModel.PASSWORD_HISTORY)
                        .filter(credentialModel -> credentialModel.getCreatedDate() > passwordMaxAgeMillis)
                        .map(PasswordCredentialModel::createFromCredentialModel)
                        .anyMatch(passwordCredential -> {
                            PasswordHashProvider hash = session.getProvider(PasswordHashProvider.class,
                                    passwordCredential.getPasswordCredentialData().getAlgorithm());
                            return hash.verify(password, passwordCredential);
                        })) {
                    return new PolicyError(ERROR_MESSAGE, passwordAgePolicyValue);
                }
            }
        }
        return null;
    }

    @Override
    public Object parseConfig(String value) {
        return parseInteger(value, AgePasswordPolicyProviderFactory.DEFAULT_AGE_DAYS);
    }

    @Override
    public void close() {
    }
}
