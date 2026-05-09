package com.aetherpro.passport.agency.crypto;

import com.aetherpro.passport.agency.jpa.AgencyKeypairEntity;
import org.jboss.logging.Logger;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

/**
 * Service for signing and verifying the signature chain.
 */
public class AgencySignatureService {

    private static final Logger logger = Logger.getLogger(AgencySignatureService.class);
    private static final String SIGNATURE_ALGORITHM = "Ed25519";

    public String sign(PrivateKey privateKey, String data) {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(data.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign data", e);
        }
    }

    public boolean verify(PublicKey publicKey, String data, String signatureBase64) {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(data.getBytes());
            return signature.verify(Base64.getUrlDecoder().decode(signatureBase64));
        } catch (Exception e) {
            logger.error("Signature verification failed", e);
            return false;
        }
    }
}
