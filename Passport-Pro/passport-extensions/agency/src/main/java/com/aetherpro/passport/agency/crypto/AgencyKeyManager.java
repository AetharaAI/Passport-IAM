package com.aetherpro.passport.agency.crypto;

import com.aetherpro.passport.agency.jpa.AgencyKeypairEntity;
import jakarta.persistence.EntityManager;
import org.jboss.logging.Logger;
import org.passport.models.utils.PassportModelUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * Manages Ed25519 keypairs for Issuers, Principals, and Delegates.
 */
public class AgencyKeyManager {

    private static final Logger logger = Logger.getLogger(AgencyKeyManager.class);
    private static final String ALGORITHM = "Ed25519";
    private static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final String MASTER_SECRET_ENV = "AGENCY_KEY_ENCRYPTION_SECRET";

    private final EntityManager em;

    public AgencyKeyManager(EntityManager em) {
        this.em = em;
    }

    public AgencyKeypairEntity generateAndSaveKeypair(String realmId, String entityType, String entityId) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITHM);
            KeyPair kp = kpg.generateKeyPair();

            byte[] publicKeyBytes = kp.getPublic().getEncoded();
            byte[] privateKeyBytes = kp.getPrivate().getEncoded();

            // Generate KID (SHA-256 of public key)
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(publicKeyBytes);
            String kid = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

            // Encrypt private key
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            SecretKeySpec keySpec = new SecretKeySpec(getMasterSecret(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
            byte[] encryptedPrivateKey = cipher.doFinal(privateKeyBytes);

            AgencyKeypairEntity entity = new AgencyKeypairEntity();
            entity.setId(PassportModelUtils.generateId());
            entity.setRealmId(realmId);
            entity.setEntityType(entityType);
            entity.setEntityId(entityId);
            entity.setKid(kid);
            entity.setPublicKeyBytes(publicKeyBytes);
            entity.setEncryptedPrivateKeyBytes(encryptedPrivateKey);
            entity.setIv(iv);
            entity.setAlgorithm(ALGORITHM);
            entity.setStatus("ACTIVE");
            entity.setCreatedAt(Instant.now());

            em.persist(entity);
            em.flush();

            logger.infof("Generated %s keypair for %s (%s). KID: %s", entityType, entityId, realmId, kid);

            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate agency keypair", e);
        }
    }

    public Optional<PrivateKey> getPrivateKey(AgencyKeypairEntity entity) {
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, entity.getIv());
            SecretKeySpec keySpec = new SecretKeySpec(getMasterSecret(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);

            byte[] privateKeyBytes = cipher.doFinal(entity.getEncryptedPrivateKeyBytes());

            KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
            return Optional.of(kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes)));
        } catch (Exception e) {
            logger.error("Failed to decrypt agency private key", e);
            return Optional.empty();
        }
    }

    public Optional<PublicKey> getPublicKey(AgencyKeypairEntity entity) {
        try {
            KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
            return Optional.of(kf.generatePublic(new X509EncodedKeySpec(entity.getPublicKeyBytes())));
        } catch (Exception e) {
            logger.error("Failed to decode agency public key", e);
            return Optional.empty();
        }
    }

    private byte[] getMasterSecret() {
        String secret = System.getenv(MASTER_SECRET_ENV);
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(MASTER_SECRET_ENV + " must be set to encrypt Agency private keys");
        }

        byte[] key = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new IllegalStateException(MASTER_SECRET_ENV + " must be 16, 24, or 32 bytes for AES");
        }

        return key;
    }
}
