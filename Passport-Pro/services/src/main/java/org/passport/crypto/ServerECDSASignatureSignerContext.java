package org.passport.crypto;

import org.passport.models.PassportSession;

public class ServerECDSASignatureSignerContext extends ECDSASignatureSignerContext {

    public ServerECDSASignatureSignerContext(PassportSession session, String algorithm) throws SignatureException {
        super(ServerAsymmetricSignatureSignerContext.getKey(session, algorithm));
    }

    public ServerECDSASignatureSignerContext(KeyWrapper key) {
        super(key);
    }
}
