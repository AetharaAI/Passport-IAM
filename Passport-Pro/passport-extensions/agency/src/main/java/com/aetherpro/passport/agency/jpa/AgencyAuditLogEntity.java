package com.aetherpro.passport.agency.jpa;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA Entity for Agency Audit Log
 */
@Entity
@Table(name = "AGENCY_AUDIT_LOG")
@NamedQueries({
    @NamedQuery(
        name = "AgencyAuditLogEntity.findByRealm",
        query = "SELECT a FROM AgencyAuditLogEntity a WHERE a.realmId = :realmId ORDER BY a.createdAt DESC"
    ),
    @NamedQuery(
        name = "AgencyAuditLogEntity.findByDelegate",
        query = "SELECT a FROM AgencyAuditLogEntity a WHERE a.delegateKid = :delegateKid"
    )
})
public class AgencyAuditLogEntity {

    @Id
    @Column(name = "ID", length = 36)
    private String id;

    @Column(name = "SIGNED_ACTION", nullable = false)
    private String signedAction;

    @Column(name = "DELEGATE_KID", nullable = false, length = 64)
    private String delegateKid;

    @Column(name = "MANDATE_ID", length = 36)
    private String mandateId;

    @Column(name = "PASSPORT_ID", length = 36)
    private String passportId;

    @Column(name = "ACTION_TYPE", nullable = false, length = 128)
    private String actionType;

    @Column(name = "CHAIN_VALID", nullable = false)
    private boolean chainValid;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

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

    public String getSignedAction() {
        return signedAction;
    }

    public void setSignedAction(String signedAction) {
        this.signedAction = signedAction;
    }

    public String getDelegateKid() {
        return delegateKid;
    }

    public void setDelegateKid(String delegateKid) {
        this.delegateKid = delegateKid;
    }

    public String getMandateId() {
        return mandateId;
    }

    public void setMandateId(String mandateId) {
        this.mandateId = mandateId;
    }

    public String getPassportId() {
        return passportId;
    }

    public void setPassportId(String passportId) {
        this.passportId = passportId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public boolean isChainValid() {
        return chainValid;
    }

    public void setChainValid(boolean chainValid) {
        this.chainValid = chainValid;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }
}
