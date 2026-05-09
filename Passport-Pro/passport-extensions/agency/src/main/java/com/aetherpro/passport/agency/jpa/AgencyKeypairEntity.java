package com.aetherpro.passport.agency.jpa;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA Entity for Agency Keypairs
 */
@Entity
@Table(name = "AGENCY_KEYPAIRS")
@NamedQueries({
    @NamedQuery(
        name = "AgencyKeypairEntity.findByKid",
        query = "SELECT k FROM AgencyKeypairEntity k WHERE k.kid = :kid"
    ),
    @NamedQuery(
        name = "AgencyKeypairEntity.findByEntity",
        query = "SELECT k FROM AgencyKeypairEntity k WHERE k.entityType = :entityType AND k.entityId = :entityId AND k.realmId = :realmId ORDER BY k.createdAt DESC"
    ),
    @NamedQuery(
        name = "AgencyKeypairEntity.findActiveByEntity",
        query = "SELECT k FROM AgencyKeypairEntity k WHERE k.entityType = :entityType AND k.entityId = :entityId AND k.realmId = :realmId AND k.status = 'ACTIVE'"
    )
})
public class AgencyKeypairEntity {

    @Id
    @Column(name = "ID", length = 36)
    private String id;

    @Column(name = "ENTITY_TYPE", nullable = false, length = 20)
    private String entityType; // ISSUER, PRINCIPAL, DELEGATE

    @Column(name = "ENTITY_ID", nullable = false, length = 36)
    private String entityId;

    @Column(name = "KID", nullable = false, unique = true, length = 64)
    private String kid;

    @Column(name = "PUBLIC_KEY_BYTES", nullable = false)
    private byte[] publicKeyBytes;

    @Column(name = "ENCRYPTED_PRIVATE_KEY_BYTES", nullable = false)
    private byte[] encryptedPrivateKeyBytes;

    @Column(name = "IV", nullable = false)
    private byte[] iv;

    @Column(name = "ALGORITHM", nullable = false, length = 20)
    private String algorithm = "Ed25519";

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @Column(name = "EXPIRES_AT")
    private Instant expiresAt;

    @Column(name = "REALM_ID", nullable = false, length = 36)
    private String realmId;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    public byte[] getPublicKeyBytes() {
        return publicKeyBytes;
    }

    public void setPublicKeyBytes(byte[] publicKeyBytes) {
        this.publicKeyBytes = publicKeyBytes;
    }

    public byte[] getEncryptedPrivateKeyBytes() {
        return encryptedPrivateKeyBytes;
    }

    public void setEncryptedPrivateKeyBytes(byte[] encryptedPrivateKeyBytes) {
        this.encryptedPrivateKeyBytes = encryptedPrivateKeyBytes;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }
}
