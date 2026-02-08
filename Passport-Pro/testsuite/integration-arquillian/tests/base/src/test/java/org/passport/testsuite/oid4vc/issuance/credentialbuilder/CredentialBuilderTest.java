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

package org.passport.testsuite.oid4vc.issuance.credentialbuilder;

import org.passport.crypto.AsymmetricSignatureSignerContext;
import org.passport.crypto.AsymmetricSignatureVerifierContext;
import org.passport.crypto.KeyWrapper;
import org.passport.crypto.SignatureSignerContext;
import org.passport.crypto.SignatureVerifierContext;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.testsuite.oid4vc.issuance.signing.OID4VCTest;

/**
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public abstract class CredentialBuilderTest extends OID4VCTest {

    private static final KeyWrapper keyWrapper;

    static {
        keyWrapper = getRsaKey();
    }

    protected static SignatureSignerContext exampleSigner() {
        return new AsymmetricSignatureSignerContext(keyWrapper);
    }

    protected static SignatureVerifierContext exampleVerifier() {
        return new AsymmetricSignatureVerifierContext(keyWrapper);
    }

    @Override
    public void configureTestRealm(RealmRepresentation testRealm) {
        testRealm.setVerifiableCredentialsEnabled(true);
    }
}
