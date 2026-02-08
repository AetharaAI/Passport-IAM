package org.passport.testsuite.util;

import java.security.PrivateKey;

import org.passport.crypto.Algorithm;
import org.passport.crypto.AsymmetricSignatureSignerContext;
import org.passport.crypto.KeyWrapper;
import org.passport.crypto.ServerECDSASignatureSignerContext;
import org.passport.crypto.SignatureSignerContext;

public class SignatureSignerUtil {

    public static SignatureSignerContext createSigner(PrivateKey privateKey, String kid, String algorithm) {
        return createSigner(privateKey, kid, algorithm, null);
    }

    public static SignatureSignerContext createSigner(PrivateKey privateKey, String kid, String algorithm, String curve) {
        KeyWrapper keyWrapper = new KeyWrapper();
        keyWrapper.setAlgorithm(algorithm);
        keyWrapper.setKid(kid);
        keyWrapper.setPrivateKey(privateKey);
        keyWrapper.setCurve(curve);
        SignatureSignerContext signer;
        switch (algorithm) {
            case Algorithm.ES256:
            case Algorithm.ES384:
            case Algorithm.ES512:
                signer = new ServerECDSASignatureSignerContext(keyWrapper);
                break;
            default:
                signer = new AsymmetricSignatureSignerContext(keyWrapper);
        }
        return signer;
    }
}
