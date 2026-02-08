/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.passport.authentication.requiredactions;

import org.passport.Config;
import org.passport.Config.Scope;
import org.passport.authentication.RequiredActionFactory;
import org.passport.authentication.RequiredActionProvider;
import org.passport.common.Profile;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.provider.EnvironmentDependentProviderFactory;
import org.passport.truststore.TruststoreProvider;

import com.webauthn4j.anchor.KeyStoreTrustAnchorRepository;
import com.webauthn4j.verifier.attestation.trustworthiness.certpath.CertPathTrustworthinessVerifier;
import com.webauthn4j.verifier.attestation.trustworthiness.certpath.DefaultCertPathTrustworthinessVerifier;
import com.webauthn4j.verifier.attestation.trustworthiness.certpath.NullCertPathTrustworthinessVerifier;

public class WebAuthnRegisterFactory implements RequiredActionFactory, EnvironmentDependentProviderFactory {

    public static final String PROVIDER_ID = "webauthn-register";

    @Override
    public RequiredActionProvider create(PassportSession session) {
        WebAuthnRegister webAuthnRegister = null;
        TruststoreProvider truststoreProvider = session.getProvider(TruststoreProvider.class);
        if (truststoreProvider == null || truststoreProvider.getTruststore() == null) {
            webAuthnRegister = createProvider(session, new NullCertPathTrustworthinessVerifier());
        } else {
            KeyStoreTrustAnchorRepository keyStoreTrustAnchorRepository = new KeyStoreTrustAnchorRepository(truststoreProvider.getTruststore());
            DefaultCertPathTrustworthinessVerifier trustVerifier = new DefaultCertPathTrustworthinessVerifier(keyStoreTrustAnchorRepository);
            webAuthnRegister = createProvider(session, trustVerifier);
        }
        return webAuthnRegister;
    }

    protected WebAuthnRegister createProvider(PassportSession session, CertPathTrustworthinessVerifier trustVerifier) {
         return new WebAuthnRegister(session, trustVerifier);
    }

    @Override
    public void init(Scope config) {
        // NOP
    }

    @Override
    public void postInit(PassportSessionFactory factory) {
        // NOP
    }

    @Override
    public void close() {
        // NOP
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayText() {
        return "Webauthn Register";
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.WEB_AUTHN);
    }
}
