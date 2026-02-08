package org.passport.crypto;

import org.passport.common.VerificationException;
import org.passport.jose.jws.JWSInput;
import org.passport.models.ClientModel;
import org.passport.models.PassportSession;

public class ECDSAClientSignatureVerifierProvider implements ClientSignatureVerifierProvider {
    private final PassportSession session;
    private final String algorithm;

    public ECDSAClientSignatureVerifierProvider(PassportSession session, String algorithm) {
        this.session = session;
        this.algorithm = algorithm;
    }

    @Override
    public SignatureVerifierContext verifier(ClientModel client, JWSInput input) throws VerificationException {
        return new ClientECDSASignatureVerifierContext(session, client, input);
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
