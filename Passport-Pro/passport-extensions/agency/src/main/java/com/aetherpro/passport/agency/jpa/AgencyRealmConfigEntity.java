/*
 * Copyright 2024 AetherPro Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aetherpro.passport.agency.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA Entity for Agency realm configuration.
 */
@Entity
@Table(name = "PASSPORT_AGENCY_CONFIG")
public class AgencyRealmConfigEntity {

    @Id
    @Column(name = "REALM_ID", length = 36)
    private String realmId;

    @Column(name = "ENABLED")
    private boolean enabled = false;

    @Column(name = "DEFAULT_JURISDICTION", length = 10)
    private String defaultJurisdiction;

    @Column(name = "COMPLIANCE_MODE", length = 20)
    private String complianceMode;

    @Column(name = "MANDATES_REQUIRED")
    private boolean mandatesRequired = true;

    @Column(name = "DEFAULT_MANDATE_VALIDITY_DAYS")
    private Integer defaultMandateValidityDays = 365;

    @Column(name = "QUALIFICATIONS_ENFORCED")
    private boolean qualificationsEnforced = false;

    @Column(name = "AUDIT_LEVEL", length = 20)
    private String auditLevel;

    @Column(name = "AGENT_PASSPORTS_ENABLED")
    private boolean agentPassportsEnabled = false;

    @Column(name = "MAX_PASSPORTS_PER_PRINCIPAL")
    private Integer maxPassportsPerPrincipal = 10;

    // Constructors
    public AgencyRealmConfigEntity() {}

    public AgencyRealmConfigEntity(String realmId) {
        this.realmId = realmId;
    }

    // Getters and Setters
    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDefaultJurisdiction() {
        return defaultJurisdiction;
    }

    public void setDefaultJurisdiction(String defaultJurisdiction) {
        this.defaultJurisdiction = defaultJurisdiction;
    }

    public String getComplianceMode() {
        return complianceMode;
    }

    public void setComplianceMode(String complianceMode) {
        this.complianceMode = complianceMode;
    }

    public boolean isMandatesRequired() {
        return mandatesRequired;
    }

    public void setMandatesRequired(boolean mandatesRequired) {
        this.mandatesRequired = mandatesRequired;
    }

    public Integer getDefaultMandateValidityDays() {
        return defaultMandateValidityDays;
    }

    public void setDefaultMandateValidityDays(Integer defaultMandateValidityDays) {
        this.defaultMandateValidityDays = defaultMandateValidityDays;
    }

    public boolean isQualificationsEnforced() {
        return qualificationsEnforced;
    }

    public void setQualificationsEnforced(boolean qualificationsEnforced) {
        this.qualificationsEnforced = qualificationsEnforced;
    }

    public String getAuditLevel() {
        return auditLevel;
    }

    public void setAuditLevel(String auditLevel) {
        this.auditLevel = auditLevel;
    }

    public boolean isAgentPassportsEnabled() {
        return agentPassportsEnabled;
    }

    public void setAgentPassportsEnabled(boolean agentPassportsEnabled) {
        this.agentPassportsEnabled = agentPassportsEnabled;
    }

    public Integer getMaxPassportsPerPrincipal() {
        return maxPassportsPerPrincipal;
    }

    public void setMaxPassportsPerPrincipal(Integer maxPassportsPerPrincipal) {
        this.maxPassportsPerPrincipal = maxPassportsPerPrincipal;
    }
}
