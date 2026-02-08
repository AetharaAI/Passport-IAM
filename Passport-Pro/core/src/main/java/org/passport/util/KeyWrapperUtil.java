package org.passport.util;

import org.passport.crypto.AsymmetricSignatureSignerContext;
import org.passport.crypto.AsymmetricSignatureVerifierContext;
import org.passport.crypto.ECDSASignatureSignerContext;
import org.passport.crypto.ECDSASignatureVerifierContext;
import org.passport.crypto.KeyType;
import org.passport.crypto.KeyWrapper;
import org.passport.crypto.SignatureSignerContext;
import org.passport.crypto.SignatureVerifierContext;

public class KeyWrapperUtil {

    public static SignatureSignerContext createSignatureSignerContext(KeyWrapper keyWrapper) {
        switch (keyWrapper.getType()) {
            case KeyType.EC:
                return new ECDSASignatureSignerContext(keyWrapper);
            case KeyType.RSA:
            case KeyType.OKP:
                return new AsymmetricSignatureSignerContext(keyWrapper);
            default:
                throw new IllegalArgumentException("No signer provider for key algorithm type " + keyWrapper.getType());
        }
    }

    public static SignatureVerifierContext createSignatureVerifierContext(KeyWrapper keyWrapper) {
        switch (keyWrapper.getType()) {
            case KeyType.EC:
                return new ECDSASignatureVerifierContext(keyWrapper);
            case KeyType.RSA:
            case KeyType.OKP:
                return new AsymmetricSignatureVerifierContext(keyWrapper);
            default:
                throw new IllegalArgumentException("No signer provider for key algorithm type " + keyWrapper.getType());
        }
    }

    private KeyWrapperUtil() {
    }
}
