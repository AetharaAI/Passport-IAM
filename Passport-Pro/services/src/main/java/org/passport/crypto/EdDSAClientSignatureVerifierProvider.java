/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.passport.crypto;

import org.passport.common.VerificationException;
import org.passport.jose.jws.JWSInput;
import org.passport.models.ClientModel;
import org.passport.models.PassportSession;

/**
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class EdDSAClientSignatureVerifierProvider implements ClientSignatureVerifierProvider {
    private final PassportSession session;
    private final String algorithm;

    public EdDSAClientSignatureVerifierProvider(PassportSession session, String algorithm) {
        this.session = session;
        this.algorithm = algorithm;
    }

    @Override
    public SignatureVerifierContext verifier(ClientModel client, JWSInput input) throws VerificationException {
        return new ClientEdDSASignatureVerifierContext(session, client, input);
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public boolean isAsymmetricAlgorithm() {
        return true;
    }
}
