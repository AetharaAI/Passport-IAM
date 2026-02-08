package org.passport.crypto;

import org.passport.common.VerificationException;
import org.passport.models.PassportSession;

public class ServerECDSASignatureVerifierContext extends  AsymmetricSignatureVerifierContext {
    public ServerECDSASignatureVerifierContext(PassportSession session, String kid, String algorithm) throws VerificationException {
        super(ServerAsymmetricSignatureVerifierContext.getKey(session, kid, algorithm));
    }

    public ServerECDSASignatureVerifierContext(KeyWrapper key) {
        super(key);
    }

    @Override
    public boolean verify(byte[] data, byte[] signature) throws VerificationException {
        try {
            int expectedSize = ECDSAAlgorithm.getSignatureLength(getAlgorithm());
            byte[] derSignature = ECDSAAlgorithm.concatenatedRSToASN1DER(signature, expectedSize);
            return super.verify(data, derSignature);
        } catch (Exception e) {
            throw new VerificationException("Signing failed", e);
        }
    }
}
