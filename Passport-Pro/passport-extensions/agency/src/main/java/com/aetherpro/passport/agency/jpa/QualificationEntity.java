package com.aetherpro.passport.agency.jpa;

import com.aetherpro.passport.agency.QualificationModel;
import jakarta.persistence.*;

/**
 * JPA Entity for a qualification definition
 */
@Entity
@Table(name = "PASSPORT_QUALIFICATION")
@NamedQueries({
    @NamedQuery(
        name = "QualificationEntity.findByRealm",
        query = "SELECT q FROM QualificationEntity q WHERE q.realmId = :realmId AND q.active = true ORDER BY q.name"
    ),
    @NamedQuery(
        name = "QualificationEntity.findByRealmAndName",
        query = "SELECT q FROM QualificationEntity q WHERE q.realmId = :realmId AND q.name = :name"
    )
})
public class QualificationEntity implements QualificationModel {
    
    @Id
    @Column(name = "ID", length = 36)
    private String id;
    
    @Column(name = "REALM_ID", nullable = false, length = 36)
    private String realmId;
    
    @Column(name = "NAME", nullable = false, length = 255)
    private String name;
    
    @Column(name = "TYPE", nullable = false, length = 50)
    private String type;
    
    @Column(name = "ISSUER", length = 255)
    private String issuer;
    
    @Column(name = "SCOPE", length = 500)
    private String scope;
    
    @Column(name = "VALIDITY_MONTHS")
    private Integer validityMonths;
    
    @Column(name = "DESCRIPTION", length = 1000)
    private String description;
    
    @Column(name = "IS_ACTIVE", nullable = false)
    private boolean active = true;
    
    // Getters and Setters
    
    @Override
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public String getRealmId() {
        return realmId;
    }
    
    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String getType() {
        return type;
    }
    
    @Override
    public void setType(String type) {
        this.type = type;
    }
    
    @Override
    public String getIssuer() {
        return issuer;
    }
    
    @Override
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    
    @Override
    public String getScope() {
        return scope;
    }
    
    @Override
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    @Override
    public Integer getValidityMonths() {
        return validityMonths;
    }
    
    @Override
    public void setValidityMonths(Integer months) {
        this.validityMonths = months;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public boolean isActive() {
        return active;
    }
    
    @Override
    public void setActive(boolean active) {
        this.active = active;
    }
}
