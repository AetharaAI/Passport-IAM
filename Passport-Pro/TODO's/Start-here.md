I'll give you a comprehensive, highly detailed prompt for Kimi Code that covers everything we discussed, optimized for AI-to-AI implementation with clear technical specifications, file paths, and integration points for your entire ecosystem.

---

## Detailed Engineered Prompt for Kimi Code

```markdown
# Passport IAM Implementation Specification
## Version 1.0 - Production Deployment Ready
## Target Domain: passport.aetherpro.tech (primary), passport.aetherpro.us (fallback)

---

## EXECUTIVE SUMMARY

Implement a complete fork of Passport 22+ with Agency/LBAC (Legal-Based Access Control) extensions. 
This is NOT a theme - this is a full core extension with custom SPIs, REST APIs, React components, 
and database schema changes. The result must be deployable to passport.aetherpro.tech within 
hours of implementation completion.

---

## ARCHITECTURE CONTEXT

### Ecosystem Integration Map
┌─────────────────────────────────────────────────────────────────────────────┐
│                              PASSPORT IAM                                    │
│                    (passport.aetherpro.tech)                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │   Agency    │  │   OAuth2    │  │    SAML     │  │   Agent Identity    │ │
│  │   Engine    │  │   OIDC      │  │    SSO      │  │   Minting Service   │ │
│  │  (Custom)   │  │ (Passport)  │  │ (Passport)  │  │     (Custom)        │ │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────────┬──────────┘ │
│         └─────────────────┴─────────────────┴────────────────────┘            │
│                              │                                               │
│                              ▼                                               │
│  ┌─────────────────────────────────────────────────────────────────────────┐ │
│  │                     ATP (Analytics & Telemetry Plane)                    │ │
│  │              (Shared with MCPFabric.space, CMC, Policy Router)          │ │
│  └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
        ┌─────────────────────────────┼─────────────────────────────┐
        ▼                             ▼                             ▼
┌───────────────┐           ┌───────────────┐           ┌───────────────┐
│AetherAgentForge│           │  aetherpro.tech│          │ MCPFabric.space│
│    .org       │           │   (Chat UI)    │          │ (A2A/MCP Registry)
│               │           │                │           │                │
│ Agent Hosting │◄─────────►│   SSO Login    │◄─────────►│  Tool Registry │
│  (OpenClaw,   │           │  (Passport)    │           │  Agent Comms   │
│  Motlbot,     │           │                │           │  (Redis Streams)│
│  Agent Zero)  │           │                │           │                │
└───────────────┘           └───────────────┘           └────────────────┘
                                      │
                                      ▼
                           ┌──────────────────┐
                           │ blackboxaudio.tech│
                           │  (Qwen3 TTS/ASR)  │
                           │   Voice Stack     │
                           └──────────────────┘

### Revenue Model Context
- FREE: Standard IAM (OAuth/OIDC/SAML) - compete with Passport/Authentik
- PAID: Agent Identity Minting ("Passports for Agents") - persistent identity across systems
- PAID: Triad Intelligence CMC integration - context-aware access decisions
- PAID: Advanced Agency routing - mandate-based request delegation

---

## REPOSITORY SETUP

### Step 1: Fork Initialization
```bash
# Execute these commands exactly
git clone https://github.com/passport/passport.git passport-iam
cd passport-iam
git checkout 22.0.5  # Stable release
git checkout -b passport-main
git remote rename origin upstream
git remote add origin https://github.com/YOUR_ORG/passport-iam.git

# Initial commit structure
mkdir -p passport-extensions/{agency,identity-minting,triad-integration}
mkdir -p passport-themes/passport/admin
mkdir -p docs/architecture
```

---

## PHASE 1: AGENCY SPI IMPLEMENTATION (Priority: CRITICAL)

### 1.1 Core SPI Interface

**File: `passport-extensions/agency/src/main/java/com/aetherpro/passport/agency/AgencySpi.java`**
```java
package com.aetherpro.passport.agency;

import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;

public class AgencySpi implements Spi {
    public static final String SPI_NAME = "agency";
    
    @Override
    public String getName() { return SPI_NAME; }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return AgencyProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return AgencyProviderFactory.class;
    }

    @Override
    public boolean isInternal() { return false; }
}
```

**File: `passport-extensions/agency/src/main/java/com/aetherpro/passport/agency/AgencyProvider.java`**
```java
package com.aetherpro.passport.agency;

import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.provider.Provider;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.time.Instant;

/**
 * Legal/Agent-Based Access Control Provider
 * 
 * Core Concepts:
 * - PRINCIPAL: Legal entity (person, org, system) that can hold rights
 * - AGENT: User acting on behalf of a principal  
 * - DELEGATE: Specific agent-principal relationship with constraints
 * - MANDATE: Time-bound, scope-limited authorization for a delegate
 * - QUALIFICATION: Credential/certification required for certain actions
 */
public interface AgencyProvider extends Provider {
    
    // ========== PRINCIPAL MANAGEMENT ==========
    
    /**
     * Create a new legal principal
     * @param realm The Passport realm
     * @param name Display name (e.g., "Acme Corp")
     * @param type individual | organization | system | ai-agent
     * @param jurisdiction Legal jurisdiction code (ISO 3166-2)
     * @param metadata Additional JSON metadata
     */
    PrincipalModel createPrincipal(
        RealmModel realm, 
        String name, 
        PrincipalType type,
        String jurisdiction,
        String metadata
    );
    
    Optional<PrincipalModel> getPrincipal(RealmModel realm, String principalId);
    List<PrincipalModel> getRealmPrincipals(RealmModel realm);
    List<PrincipalModel> getPrincipalsByType(RealmModel realm, PrincipalType type);
    List<PrincipalModel> getPrincipalsByJurisdiction(RealmModel realm, String jurisdiction);
    
    void updatePrincipal(PrincipalModel principal);
    void suspendPrincipal(String principalId, String reason);
    void revokePrincipal(String principalId, String reason);
    
    // ========== DELEGATE MANAGEMENT ==========
    
    /**
     * Create a delegation relationship
     * @param agent The user acting as agent
     * @param principal The principal they represent
     * @param delegationType full | limited | conditional | emergency
     * @param constraints JSON constraint rules
     */
    DelegateModel createDelegate(
        UserModel agent,
        PrincipalModel principal,
        DelegationType delegationType,
        String constraints,
        Instant validFrom,
        Instant validUntil
    );
    
    List<DelegateModel> getDelegatesForPrincipal(PrincipalModel principal);
    List<DelegateModel> getDelegatesForAgent(UserModel agent);
    Optional<DelegateModel> getActiveDelegate(UserModel agent, PrincipalModel principal);
    
    boolean isValidDelegate(UserModel agent, PrincipalModel principal, String actionScope);
    void revokeDelegate(String delegateId, String reason);
    
    // ========== MANDATE MANAGEMENT ==========
    
    /**
     * Create a specific mandate (authorization instance)
     * @param delegate The delegation this mandate operates under
     * @param scope Action scope (e.g., "contracts.sign", "payments.approve")
     * @param constraints JSON constraint object
     * @param maxAmount For financial mandates, max authorized amount
     * @param requiresSecondFactor Whether 2FA is required
     */
    MandateModel createMandate(
        DelegateModel delegate,
        String scope,
        String constraints,
        Double maxAmount,
        boolean requiresSecondFactor,
        Instant validFrom,
        Instant validUntil
    );
    
    List<MandateModel> getMandatesForDelegate(DelegateModel delegate);
    List<MandateModel> getActiveMandatesForAgent(UserModel agent, String scope);
    
    /**
     * Validate if a mandate permits a specific action
     * This is the core access control check for Agency/LBAC
     */
    MandateValidationResult validateMandate(
        String mandateId,
        String action,
        String resource,
        Double amount,
        String context  // JSON context for dynamic evaluation
    );
    
    void suspendMandate(String mandateId, String reason);
    void revokeMandate(String mandateId, String reason);
    
    // ========== QUALIFICATION MANAGEMENT ==========
    
    QualificationModel createQualification(
        RealmModel realm,
        String name,
        String type,  // certification | license | clearance | training
        String issuer,
        String scope,
        Integer validityMonths
    );
    
    void assignQualification(UserModel user, QualificationModel qualification, 
                            String credentialId, Instant expiresAt);
    void revokeQualification(String assignmentId, String reason);
    
    Set<QualificationModel> getUserQualifications(UserModel user);
    boolean hasQualification(UserModel user, String qualificationName);
    
    // ========== AGENCY CONTEXT ==========
    
    /**
     * Get full agency context for authentication/authorization decisions
     * Called during token generation to embed claims
     */
    AgencyContext getAgencyContext(UserModel user);
    
    /**
     * Check if user can act on behalf of principal for specific action
     * Integrates with Policy Router for complex decisions
     */
    AgencyDecision evaluateAgencyAccess(
        UserModel user,
        String principalId,
        String action,
        String resource,
        String context
    );
    
    // ========== AGENT IDENTITY MINTING (REVENUE FEATURE) ==========
    
    /**
     * Mint a persistent Agent Passport
     * This is a PAID feature - agents get persistent DID-based identity
     */
    AgentPassport mintAgentPassport(
        PrincipalModel principal,
        String agentType,  // ai-assistant | autonomous-agent | human-proxy
        String capabilities,  // JSON array of capability strings
        String rateLimits  // JSON rate limiting configuration
    );
    
    Optional<AgentPassport> getAgentPassport(String passportId);
    void revokeAgentPassport(String passportId, String reason);
    
    @Override
    default void close() {}
}

// Supporting enums
enum PrincipalType {
    INDIVIDUAL, ORGANIZATION, SYSTEM, AI_AGENT, SMART_CONTRACT
}

enum DelegationType {
    FULL, LIMITED, CONDITIONAL, EMERGENCY, TEMPORARY
}

// Supporting models (interfaces)
interface PrincipalModel {
    String getId();
    String getName();
    PrincipalType getType();
    String getJurisdiction();
    String getMetadata();
    boolean isActive();
    Instant getCreatedAt();
    Instant getUpdatedAt();
}

interface DelegateModel {
    String getId();
    UserModel getAgent();
    PrincipalModel getPrincipal();
    DelegationType getType();
    String getConstraints();
    boolean isActive();
    Instant getValidFrom();
    Instant getValidUntil();
}

interface MandateModel {
    String getId();
    DelegateModel getDelegate();
    String getScope();
    String getConstraints();
    Double getMaxAmount();
    boolean requiresSecondFactor();
    boolean isActive();
    Instant getValidFrom();
    Instant getValidUntil();
}

interface QualificationModel {
    String getId();
    String getName();
    String getType();
    String getIssuer();
    String getScope();
    Integer getValidityMonths();
}

interface AgencyContext {
    String getUserId();
    List<PrincipalReference> getRepresenting();
    List<String> getActiveScopes();
    List<String> getQualifications();
    boolean isAgent();
    String getPrimaryPassportId();
}

interface PrincipalReference {
    String getPrincipalId();
    String getPrincipalName();
    List<String> getActiveMandateScopes();
}

interface MandateValidationResult {
    boolean isValid();
    String getMandateId();
    List<String> getPermittedScopes();
    List<String> getViolations();
    String getAuditToken();  // For ATP logging
}

interface AgencyDecision {
    boolean allowed();
    String getDecisionId();
    String getReason();
    List<String> getApplicableMandates();
    String getRoutingHint();  // For Triad CMC integration
}

interface AgentPassport {
    String getPassportId();  // DID format: did:passport:<uuid>
    String getPrincipalId();
    String getAgentType();
    List<String> getCapabilities();
    boolean isActive();
    Instant getMintedAt();
    Instant getExpiresAt();
}
```

### 1.2 JPA Entity Implementation

**File: `passport-extensions/agency/src/main/java/com/aetherpro/passport/agency/jpa/PrincipalEntity.java`**
```java
package com.aetherpro.passport.agency.jpa;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "PASSPORT_PRINCIPAL")
public class PrincipalEntity {
    
    @Id
    @Column(name = "ID", length = 36)
    private String id = UUID.randomUUID().toString();
    
    @Column(name = "REALM_ID", nullable = false, length = 36)
    private String realmId;
    
    @Column(name = "NAME", nullable = false, length = 255)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false, length = 20)
    private PrincipalType type;
    
    @Column(name = "JURISDICTION", length = 10)
    private String jurisdiction;  // ISO 3166-2
    
    @Column(name = "METADATA", length = 4000)
    private String metadata;  // JSON
    
    @Column(name = "ACTIVE", nullable = false)
    private boolean active = true;
    
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt = Instant.now();
    
    @Column(name = "UPDATED_AT")
    private Instant updatedAt;
    
    @Column(name = "SUSPENDED_AT")
    private Instant suspendedAt;
    
    @Column(name = "SUSPENSION_REASON", length = 1000)
    private String suspensionReason;
    
    // Getters/setters...
}
```

**File: `passport-extensions/agency/src/main/java/com/aetherpro/passport/agency/jpa/DelegateEntity.java`**
```java
package com.aetherpro.passport.agency.jpa;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "PASSPORT_DELEGATE")
public class DelegateEntity {
    
    @Id
    @Column(name = "ID", length = 36)
    private String id = UUID.randomUUID().toString();
    
    @Column(name = "REALM_ID", nullable = false, length = 36)
    private String realmId;
    
    @Column(name = "AGENT_ID", nullable = false, length = 36)
    private String agentId;  // User ID
    
    @Column(name = "PRINCIPAL_ID", nullable = false, length = 36)
    private String principalId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRINCIPAL_ID", insertable = false, updatable = false)
    private PrincipalEntity principal;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "DELEGATION_TYPE", nullable = false, length = 20)
    private DelegationType delegationType;
    
    @Column(name = "CONSTRAINTS", length = 4000)
    private String constraints;  // JSON
    
    @Column(name = "ACTIVE", nullable = false)
    private boolean active = true;
    
    @Column(name = "VALID_FROM", nullable = false)
    private Instant validFrom;
    
    @Column(name = "VALID_UNTIL")
    private Instant validUntil;
    
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt = Instant.now();
    
    @Column(name = "REVOKED_AT")
    private Instant revokedAt;
    
    @Column(name = "REVOCATION_REASON", length = 1000)
    private String revocationReason;
    
    // Getters/setters...
}
```

**File: `passport-extensions/agency/src/main/java/com/aetherpro/passport/agency/jpa/MandateEntity.java`**
```java
package com.aetherpro.passport.agency.jpa;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "PASSPORT_MANDATE")
public class MandateEntity {
    
    @Id
    @Column(name = "ID", length = 36)
    private String id = UUID.randomUUID().toString();
    
    @Column(name = "REALM_ID", nullable = false, length = 36)
    private String realmId;
    
    @Column(name = "DELEGATE_ID", nullable = false, length = 36)
    private String delegateId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DELEGATE_ID", insertable = false, updatable = false)
    private DelegateEntity delegate;
    
    @Column(name = "SCOPE", nullable = false, length = 500)
    private String scope;  // e.g., "contracts.sign", "payments.approve:limit:10000"
    
    @Column(name = "CONSTRAINTS", length = 4000)
    private String constraints;  // JSON with detailed rules
    
    @Column(name = "MAX_AMOUNT")
    private Double maxAmount;  // For financial mandates
    
    @Column(name = "REQUIRES_2FA", nullable = false)
    private boolean requiresSecondFactor = false;
    
    @Column(name = "ACTIVE", nullable = false)
    private boolean active = true;
    
    @Column(name = "VALID_FROM", nullable = false)
    private Instant validFrom;
    
    @Column(name = "VALID_UNTIL")
    private Instant validUntil;
    
    @Column(name = "USAGE_COUNT")
    private Integer usageCount = 0;
    
    @Column(name = "LAST_USED_AT")
    private Instant lastUsedAt;
    
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt = Instant.now();
    
    // Getters/setters...
}
```

**File: `passport-extensions/agency/src/main/java/com/aetherpro/passport/agency/jpa/AgentPassportEntity.java`**
```java
package com.aetherpro.passport.agency.jpa;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Persistent Agent Identity - Revenue Feature
 * Agents get a DID-based passport that persists across systems
 */
@Entity
@Table(name = "PASSPORT_AGENT_IDENTITY")
public class AgentPassportEntity {
    
    @Id
    @Column(name = "ID", length = 36)
    private String id = UUID.randomUUID().toString();
    
    @Column(name = "PASSPORT_DID", nullable = false, unique = true, length = 100)
    private String passportDid;  // did:passport:<uuid>
    
    @Column(name = "REALM_ID", nullable = false, length = 36)
    private String realmId;
    
    @Column(name = "PRINCIPAL_ID", nullable = false, length = 36)
    private String principalId;
    
    @Column(name = "AGENT_TYPE", nullable = false, length = 50)
    private String agentType;  // ai-assistant | autonomous-agent | human-proxy
    
    @Column(name = "CAPABILITIES", length = 4000)
    private String capabilities;  // JSON array
    
    @Column(name = "RATE_LIMITS", length = 2000)
    private String rateLimits;  // JSON
    
    @Column(name = "ACTIVE", nullable = false)
    private boolean active = true;
    
    @Column(name = "MINTED_AT", nullable = false)
    private Instant mintedAt = Instant.now();
    
    @Column(name = "EXPIRES_AT")
    private Instant expiresAt;
    
    @Column(name = "REVOKED_AT")
    private Instant revokedAt;
    
    @Column(name = "LAST_USED_AT")
    private Instant lastUsedAt;
    
    @Column(name = "USAGE_COUNT")
    private Integer usageCount = 0;
    
    // Getters/setters...
}
```

### 1.3 Liquibase Migration

**File: `passport-extensions/agency/src/main/resources/META-INF/changelog-passport-agency.xml`**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog 
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="passport-agency-1.0.0" author="aetherpro">
        <createTable tableName="PASSPORT_PRINCIPAL">
            <column name="ID" type="VARCHAR(36)">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="REALM_ID" type="VARCHAR(36)">
                <constraints nullable="false"/>
            </column>
            <column name="NAME" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="TYPE" type="VARCHAR(20)">
                <constraints nullable="false"/>
            </column>
            <column name="JURISDICTION" type="VARCHAR(10)"/>
            <column name="METADATA" type="VARCHAR(4000)"/>
            <column name="ACTIVE" type="BOOLEAN" defaultValue="true">
                <constraints nullable="false"/>
            </column>
            <column name="CREATED_AT" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="UPDATED_AT" type="TIMESTAMP"/>
            <column name="SUSPENDED_AT" type="TIMESTAMP"/>
            <column name="SUSPENSION_REASON" type="VARCHAR(1000)"/>
        </createTable>
        
        <createIndex indexName="IDX_PRINCIPAL_REALM" tableName="PASSPORT_PRINCIPAL">
            <column name="REALM_ID"/>
        </createIndex>
        
        <createIndex indexName="IDX_PRINCIPAL_TYPE" tableName="PASSPORT_PRINCIPAL">
            <column name="TYPE"/>
        </createIndex>
        
        <createTable tableName="PASSPORT_DELEGATE">
            <column name="ID" type="VARCHAR(36)">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="REALM_ID" type="VARCHAR(36)">
                <constraints nullable="false"/>
            </column>
            <column name="AGENT_ID" type="VARCHAR(36)">
                <constraints nullable="false"/>
            </column>
            <column name="PRINCIPAL_ID" type="VARCHAR(36)">
                <constraints nullable="false"/>
            </column>
            <column name="DELEGATION_TYPE" type="VARCHAR(20)">
                <constraints nullable="false"/>
            </column>
            <column name="CONSTRAINTS" type="VARCHAR(4000)"/>
            <column name="ACTIVE" type="BOOLEAN" defaultValue="true">
                <constraints nullable="false"/>
            </column>
            <column name="VALID_FROM" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="VALID_UNTIL" type="TIMESTAMP"/>
            <column name="CREATED_AT" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="REVOKED_AT" type="TIMESTAMP"/>
            <column name="REVOCATION_REASON" type="VARCHAR(1000)"/>
        </createTable>
        
        <addForeignKeyConstraint 
            baseTableName="PASSPORT_DELEGATE" 
            baseColumnNames="PRINCIPAL_ID"
            constraintName="FK_DELEGATE_PRINCIPAL"
            referencedTableName="PASSPORT_PRINCIPAL"
            referencedColumnNames="ID"/>
            
        <createIndex indexName="IDX_DELEGATE_AGENT" tableName="PASSPORT_DELEGATE">
            <column name="AGENT_ID"/>
        </createIndex>
        
        <createTable tableName="PASSPORT_MANDATE">
            <!-- Mandate table schema -->
        </createTable>
        
        <createTable tableName="PASSPORT_AGENT_IDENTITY">
            <!-- Agent passport table schema -->
        </createTable>
    </changeSet>
</databaseChangeLog>
```

---

## PHASE 2: REST API LAYER (Priority: CRITICAL)

### 2.1 Agency Admin Resource

**File: `passport-extensions/agency/src/main/java/com/aetherpro/passport/agency/admin/AgencyAdminResource.java`**
```java
package com.aetherpro.passport.agency.admin;

import com.aetherpro.passport.agency.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.services.ErrorResponse;
import org.passport.services.resources.admin.AdminEventBuilder;
import org.passport.services.resources.admin.permissions.AdminPermissionEvaluator;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin REST API for Agency/LBAC management
 * Base path: /admin/realms/{realm}/agency
 */
@Path("/admin/realms/{realm}/agency")
public class AgencyAdminResource {
    
    private final PassportSession session;
    private final RealmModel realm;
    private final AgencyProvider agency;
    private final AdminPermissionEvaluator auth;
    private final AdminEventBuilder adminEvent;
    
    public AgencyAdminResource(
        PassportSession session,
        RealmModel realm,
        AdminPermissionEvaluator auth,
        AdminEventBuilder adminEvent
    ) {
        this.session = session;
        this.realm = realm;
        this.auth = auth;
        this.adminEvent = adminEvent;
        this.agency = session.getProvider(AgencyProvider.class);
    }
    
    // ========== PRINCIPAL ENDPOINTS ==========
    
    @GET
    @Path("/principals")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PrincipalRepresentation> listPrincipals(
        @QueryParam("type") String type,
        @QueryParam("jurisdiction") String jurisdiction,
        @QueryParam("search") String search,
        @QueryParam("first") @DefaultValue("0") int first,
        @QueryParam("max") @DefaultValue("20") int max
    ) {
        auth.realm().requireViewRealm();
        
        List<PrincipalModel> principals = agency.getRealmPrincipals(realm);
        
        // Apply filters
        if (type != null) {
            principals = principals.stream()
                .filter(p -> p.getType().toString().equalsIgnoreCase(type))
                .collect(Collectors.toList());
        }
        
        if (jurisdiction != null) {
            principals = principals.stream()
                .filter(p -> jurisdiction.equalsIgnoreCase(p.getJurisdiction()))
                .collect(Collectors.toList());
        }
        
        if (search != null) {
            principals = principals.stream()
                .filter(p -> p.getName().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return principals.stream()
            .skip(first)
            .limit(max)
            .map(this::toPrincipalRep)
            .collect(Collectors.toList());
    }
    
    @POST
    @Path("/principals")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPrincipal(PrincipalRepresentation rep) {
        auth.realm().requireManageRealm();
        
        PrincipalModel principal = agency.createPrincipal(
            realm,
            rep.getName(),
            PrincipalType.valueOf(rep.getType().toUpperCase()),
            rep.getJurisdiction(),
            rep.getMetadata()
        );
        
        adminEvent.operation(OperationType.CREATE)
            .resourcePath(session.getContext().getUri(), principal.getId())
            .representation(rep)
            .success();
        
        return Response.created(
            session.getContext().getUri().getAbsolutePathBuilder()
                .path(principal.getId()).build()
        ).entity(toPrincipalRep(principal)).build();
    }
    
    @GET
    @Path("/principals/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public PrincipalRepresentation getPrincipal(@PathParam("id") String id) {
        auth.realm().requireViewRealm();
        
        PrincipalModel principal = agency.getPrincipal(realm, id)
            .orElseThrow(() -> new NotFoundException("Principal not found"));
        
        return toPrincipalRep(principal);
    }
    
    @PUT
    @Path("/principals/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePrincipal(
        @PathParam("id") String id,
        PrincipalRepresentation rep
    ) {
        auth.realm().requireManageRealm();
        
        PrincipalModel principal = agency.getPrincipal(realm, id)
            .orElseThrow(() -> new NotFoundException("Principal not found"));
        
        // Update fields
        agency.updatePrincipal(principal);
        
        adminEvent.operation(OperationType.UPDATE)
            .resourcePath(session.getContext().getUri())
            .representation(rep)
            .success();
        
        return Response.noContent().build();
    }
    
    @POST
    @Path("/principals/{id}/suspend")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response suspendPrincipal(
        @PathParam("id") String id,
        SuspensionRequest request
    ) {
        auth.realm().requireManageRealm();
        
        agency.suspendPrincipal(id, request.getReason());
        
        adminEvent.operation(OperationType.ACTION)
            .resourcePath(session.getContext().getUri())
            .representation(request)
            .success();
        
        return Response.noContent().build();
    }
    
    // ========== DELEGATE ENDPOINTS ==========
    
    @GET
    @Path("/users/{userId}/delegates")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DelegateRepresentation> getUserDelegates(
        @PathParam("userId") String userId
    ) {
        auth.users().requireViewUser(userId);
        
        UserModel user = session.users().getUserById(realm, userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        
        return agency.getDelegatesForAgent(user).stream()
            .map(this::toDelegateRep)
            .collect(Collectors.toList());
    }
    
    @POST
    @Path("/users/{userId}/delegates")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDelegate(
        @PathParam("userId") String userId,
        DelegateRequest request
    ) {
        auth.users().requireManageUser(userId);
        
        UserModel user = session.users().getUserById(realm, userId);
        PrincipalModel principal = agency.getPrincipal(realm, request.getPrincipalId())
            .orElseThrow(() -> new NotFoundException("Principal not found"));
        
        DelegateModel delegate = agency.createDelegate(
            user,
            principal,
            DelegationType.valueOf(request.getType()),
            request.getConstraints(),
            request.getValidFrom(),
            request.getValidUntil()
        );
        
        return Response.created(...)
            .entity(toDelegateRep(delegate))
            .build();
    }
    
    // ========== MANDATE ENDPOINTS ==========
    
    @POST
    @Path("/mandates/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public MandateValidationResponse validateMandate(MandateValidationRequest request) {
        // This endpoint is called by the Policy Router and ATP for real-time decisions
        auth.realm().requireViewRealm();
        
        MandateValidationResult result = agency.validateMandate(
            request.getMandateId(),
            request.getAction(),
            request.getResource(),
            request.getAmount(),
            request.getContext()
        );
        
        return new MandateValidationResponse(
            result.isValid(),
            result.getMandateId(),
            result.getPermittedScopes(),
            result.getViolations(),
            result.getAuditToken()
        );
    }
    
    // ========== AGENT PASSPORT ENDPOINTS (REVENUE) ==========
    
    @POST
    @Path("/principals/{principalId}/passports")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response mintAgentPassport(
        @PathParam("principalId") String principalId,
        AgentPassportRequest request
    ) {
        auth.realm().requireManageRealm();
        
        PrincipalModel principal = agency.getPrincipal(realm, principalId)
            .orElseThrow(() -> new NotFoundException("Principal not found"));
        
        AgentPassport passport = agency.mintAgentPassport(
            principal,
            request.getAgentType(),
            request.getCapabilities(),
            request.getRateLimits()
        );
        
        // Emit event to ATP for billing/auditing
        session.getProvider(EventStoreProvider.class).onEvent(
            new AgencyEventBuilder()
                .type(AgencyEventType.PASSPORT_MINTED)
                .realmId(realm.getId())
                .principalId(principalId)
                .passportId(passport.getPassportId())
                .build()
        );
        
        return Response.created(...)
            .entity(toPassportRep(passport))
            .build();
    }
    
    @GET
    @Path("/passports/{passportId}")
    @Produces(MediaType.APPLICATION_JSON)
    public AgentPassportRepresentation getAgentPassport(
        @PathParam("passportId") String passportId
    ) {
        auth.realm().requireViewRealm();
        
        AgentPassport passport = agency.getAgentPassport(passportId)
            .orElseThrow(() -> new NotFoundException("Passport not found"));
        
        return toPassportRep(passport);
    }
    
    // ========== REALM CONFIG ==========
    
    @GET
    @Path("/config")
    @Produces(MediaType.APPLICATION_JSON)
    public AgencyRealmConfig getRealmConfig() {
        auth.realm().requireViewRealm();
        
        AgencyRealmConfig config = new AgencyRealmConfig();
        config.setEnabled(Boolean.parseBoolean(
            realm.getAttribute("agency.enabled")));
        config.setDefaultJurisdiction(
            realm.getAttribute("agency.defaultJurisdiction"));
        config.setComplianceMode(
            realm.getAttribute("agency.complianceMode"));
        config.setRequireMandateForDelegation(Boolean.parseBoolean(
            realm.getAttribute("agency.requireMandate")));
        config.setMaxDelegationDepth(Integer.parseInt(
            realm.getAttribute("agency.maxDepth") != null ? 
                realm.getAttribute("agency.maxDepth") : "3"));
        
        return config;
    }
    
    @PUT
    @Path("/config")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRealmConfig(AgencyRealmConfig config) {
        auth.realm().requireManageRealm();
        
        realm.setAttribute("agency.enabled", String.valueOf(config.isEnabled()));
        realm.setAttribute("agency.defaultJurisdiction", config.getDefaultJurisdiction());
        realm.setAttribute("agency.complianceMode", config.getComplianceMode());
        realm.setAttribute("agency.requireMandate", 
            String.valueOf(config.isRequireMandateForDelegation()));
        realm.setAttribute("agency.maxDepth", 
            String.valueOf(config.getMaxDelegationDepth()));
        
        adminEvent.operation(OperationType.UPDATE)
            .resourcePath(session.getContext().getUri())
            .representation(config)
            .success();
        
        return Response.noContent().build();
    }
    
    // Helper methods...
    private PrincipalRepresentation toPrincipalRep(PrincipalModel model) {
        PrincipalRepresentation rep = new PrincipalRepresentation();
        rep.setId(model.getId());
        rep.setName(model.getName());
        rep.setType(model.getType().toString());
        rep.setJurisdiction(model.getJurisdiction());
        rep.setActive(model.isActive());
        rep.setCreatedAt(model.getCreatedAt().toString());
        return rep;
    }
}
```

### 2.2 Representation Classes (DTOs)

**File: `passport-extensions/agency/src/main/java/com/aetherpro/passport/agency/admin/PrincipalRepresentation.java`**
```java
package com.aetherpro.passport.agency.admin;

public class PrincipalRepresentation {
    private String id;
    private String name;
    private String type;  // individual | organization | system | ai-agent
    private String jurisdiction;
    private String metadata;  // JSON
    private boolean active;
    private String createdAt;
    private String updatedAt;
    
    // Getters/setters...
}
```

**File: `passport-extensions/agency/src/main/java/com/aetherpro/passport/agency/admin/AgencyRealmConfig.java`**
```java
package com.aetherpro.passport.agency.admin;

public class AgencyRealmConfig {
    private boolean enabled;
    private String defaultJurisdiction;
    private String complianceMode;  // gdpr | ccpa | hipaa | sox | custom
    private boolean requireMandateForDelegation;
    private int maxDelegationDepth;
    private String auditLevel;  // minimal | standard | comprehensive
    
    // Getters/setters...
}
```

---

## PHASE 3: FRONTEND IMPLEMENTATION (Priority: CRITICAL)

### 3.1 Agency Module Structure

```
js/apps/admin-ui/src/agency/
├── components/
│   ├── PrincipalForm.tsx
│   ├── DelegateForm.tsx
│   ├── MandateForm.tsx
│   └── AgencyStatusBadge.tsx
├── pages/
│   ├── PrincipalsList.tsx
│   ├── PrincipalDetails.tsx
│   ├── DelegatesList.tsx
│   ├── MandatesList.tsx
│   └── AgencyRealmSettings.tsx
├── hooks/
│   ├── useAgencyConfig.ts
│   ├── usePrincipals.ts
│   └── useDelegates.ts
├── types/
│   └── agency.ts
└── index.ts
```

### 3.2 Type Definitions

**File: `js/apps/admin-ui/src/agency/types/agency.ts`**
```typescript
export type PrincipalType = 'individual' | 'organization' | 'system' | 'ai-agent';

export type DelegationType = 'full' | 'limited' | 'conditional' | 'emergency';

export type ComplianceMode = 'gdpr' | 'ccpa' | 'hipaa' | 'sox' | 'custom';

export interface Principal {
  id: string;
  name: string;
  type: PrincipalType;
  jurisdiction: string;
  metadata?: Record<string, any>;
  active: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface Delegate {
  id: string;
  agentId: string;
  agentName?: string;
  principalId: string;
  principalName?: string;
  type: DelegationType;
  constraints?: Record<string, any>;
  active: boolean;
  validFrom: string;
  validUntil?: string;
}

export interface Mandate {
  id: string;
  delegateId: string;
  scope: string;
  constraints?: Record<string, any>;
  maxAmount?: number;
  requiresSecondFactor: boolean;
  active: boolean;
  validFrom: string;
  validUntil?: string;
  usageCount: number;
}

export interface AgencyConfig {
  enabled: boolean;
  defaultJurisdiction: string;
  complianceMode: ComplianceMode;
  requireMandateForDelegation: boolean;
  maxDelegationDepth: number;
  auditLevel: 'minimal' | 'standard' | 'comprehensive';
}

export interface AgentPassport {
  passportId: string;  // DID format
  principalId: string;
  agentType: 'ai-assistant' | 'autonomous-agent' | 'human-proxy';
  capabilities: string[];
  active: boolean;
  mintedAt: string;
  expiresAt?: string;
  usageCount: number;
}
```

### 3.3 Principals List Page

**File: `js/apps/admin-ui/src/agency/pages/PrincipalsList.tsx`**
```typescript
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { useRealm } from '../../context/RealmContext';
import { usePrincipals } from '../hooks/usePrincipals';
import {
  PageSection,
  PageSectionVariants,
  Title,
  Toolbar,
  ToolbarContent,
  ToolbarItem,
  Button,
  Table,
  Thead,
  Tbody,
  Tr,
  Th,
  Td,
  ActionsColumn,
  Label,
  Modal,
  Form,
  FormGroup,
  TextInput,
  Select,
  SelectOption,
  Alert,
  Pagination
} from '@patternfly/react-core';
import {
  PlusCircleIcon,
  BlueprintIcon,
  SearchIcon
} from '@patternfly/react-icons';
import { HelpItem } from '../../components/help-enabler/HelpItem';

export const PrincipalsList = () => {
  const { t } = useTranslation();
  const { realm } = useRealm();
  const [page, setPage] = useState(1);
  const [perPage, setPerPage] = useState(20);
  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState<string>('');
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  
  const { principals, total, loading, error, refresh } = usePrincipals({
    realm: realm.name,
    first: (page - 1) * perPage,
    max: perPage,
    search: search || undefined,
    type: typeFilter || undefined
  });
  
  const [newPrincipal, setNewPrincipal] = useState({
    name: '',
    type: 'organization' as PrincipalType,
    jurisdiction: '',
    metadata: {}
  });
  
  const columns = [
    { name: 'name', label: 'Name', width: 25 },
    { name: 'type', label: 'Type', width: 15 },
    { name: 'jurisdiction', label: 'Jurisdiction', width: 15 },
    { name: 'status', label: 'Status', width: 10 },
    { name: 'created', label: 'Created', width: 20 },
    { name: 'actions', label: '', width: 15 }
  ];
  
  const getStatusLabel = (active: boolean) => {
    if (active) return <Label color="green">Active</Label>;
    return <Label color="red">Inactive</Label>;
  };
  
  const getTypeIcon = (type: PrincipalType) => {
    switch (type) {
      case 'individual': return '👤';
      case 'organization': return '🏢';
      case 'system': return '⚙️';
      case 'ai-agent': return '🤖';
      default: return '❓';
    }
  };
  
  const rowActions = (principal: Principal) => [
    {
      title: 'View Details',
      onClick: () => navigate(`/realm/${realm.name}/agency/principals/${principal.id}`)
    },
    {
      title: 'Edit',
      onClick: () => editPrincipal(principal)
    },
    {
      title: 'Manage Delegates',
      onClick: () => navigate(`/realm/${realm.name}/agency/principals/${principal.id}/delegates`)
    },
    { isSeparator: true },
    {
      title: principal.active ? 'Suspend' : 'Activate',
      onClick: () => togglePrincipalStatus(principal)
    },
    {
      title: 'Revoke',
      onClick: () => revokePrincipal(principal.id),
      isDanger: true
    }
  ];
  
  const handleCreate = async () => {
    try {
      await createPrincipal(realm.name, newPrincipal);
      setIsCreateOpen(false);
      refresh();
      setNewPrincipal({ name: '', type: 'organization', jurisdiction: '', metadata: {} });
    } catch (err) {
      // Handle error
    }
  };
  
  return (
    <>
      <PageSection variant={PageSectionVariants.light}>
        <Title headingLevel="h1">
          <BlueprintIcon /> {t('agency.principals.title')}
        </Title>
        <p>{t('agency.principals.description')}</p>
      </PageSection>
      
      <PageSection>
        <Toolbar>
          <ToolbarContent>
            <ToolbarItem variant="search-filter">
              <TextInput
                iconVariant={SearchIcon}
                placeholder={t('agency.principals.search')}
                value={search}
                onChange={setSearch}
                onKeyPress={(e) => e.key === 'Enter' && refresh()}
              />
            </ToolbarItem>
            
            <ToolbarItem>
              <Select
                variant="single"
                placeholderText={t('agency.principals.filterByType')}
                selections={typeFilter}
                onSelect={(_, value) => setTypeFilter(value as string)}
              >
                <SelectOption value="">All Types</SelectOption>
                <SelectOption value="individual">Individual</SelectOption>
                <SelectOption value="organization">Organization</SelectOption>
                <SelectOption value="system">System</SelectOption>
                <SelectOption value="ai-agent">AI Agent</SelectOption>
              </Select>
            </ToolbarItem>
            
            <ToolbarItem>
              <Button variant="primary" icon={<PlusCircleIcon />} onClick={() => setIsCreateOpen(true)}>
                {t('agency.principals.create')}
              </Button>
            </ToolbarItem>
            
            <ToolbarItem variant="pagination" align={{ default: 'alignRight' }}>
              <Pagination
                itemCount={total}
                page={page}
                perPage={perPage}
                onSetPage={(_, newPage) => setPage(newPage)}
                onPerPageSelect={(_, newPerPage) => {
                  setPerPage(newPerPage);
                  setPage(1);
                }}
              />
            </ToolbarItem>
          </ToolbarContent>
        </Toolbar>
        
        {error && <Alert variant="danger" title={error} />}
        
        <Table aria-label="Principals" borders isStriped>
          <Thead>
            <Tr>
              {columns.map(col => (
                <Th key={col.name} width={col.width}>{col.label}</Th>
              ))}
            </Tr>
          </Thead>
          <Tbody>
            {principals.map(principal => (
              <Tr key={principal.id}>
                <Td>
                  <Link to={`/realm/${realm.name}/agency/principals/${principal.id}`}>
                    {getTypeIcon(principal.type)} {principal.name}
                  </Link>
                </Td>
                <Td>{principal.type}</Td>
                <Td>{principal.jurisdiction || '-'}</Td>
                <Td>{getStatusLabel(principal.active)}</Td>
                <Td>{new Date(principal.createdAt).toLocaleDateString()}</Td>
                <Td isActionCell>
                  <ActionsColumn items={rowActions(principal)} />
                </Td>
              </Tr>
            ))}
          </Tbody>
        </Table>
      </PageSection>
      
      {/* Create Principal Modal */}
      <Modal
        title={t('agency.principals.createTitle')}
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        actions={[
          <Button key="create" variant="primary" onClick={handleCreate}>
            {t('common.create')}
          </Button>,
          <Button key="cancel" variant="link" onClick={() => setIsCreateOpen(false)}>
            {t('common.cancel')}
          </Button>
        ]}
      >
        <Form>
          <FormGroup
            label={t('agency.principals.name')}
            isRequired
            fieldId="principal-name"
            helperText={t('agency.principals.nameHelp')}
          >
            <TextInput
              id="principal-name"
              value={newPrincipal.name}
              onChange={val => setNewPrincipal({...newPrincipal, name: val})}
              placeholder="e.g., Acme Corporation"
            />
          </FormGroup>
          
          <FormGroup label={t('agency.principals.type')} isRequired fieldId="principal-type">
            <Select
              id="principal-type"
              selections={newPrincipal.type}
              onSelect={(_, val) => setNewPrincipal({...newPrincipal, type: val as PrincipalType})}
            >
              <SelectOption value="individual">Individual</SelectOption>
              <SelectOption value="organization">Organization</SelectOption>
              <SelectOption value="system">System</SelectOption>
              <SelectOption value="ai-agent">AI Agent</SelectOption>
            </Select>
          </FormGroup>
          
          <FormGroup
            label={t('agency.principals.jurisdiction')}
            fieldId="principal-jurisdiction"
            labelIcon={
              <HelpItem 
                helpText={t('agency.principals.jurisdictionHelp')} 
                fieldLabelId="principal-jurisdiction"
              />
            }
          >
            <TextInput
              id="principal-jurisdiction"
              value={newPrincipal.jurisdiction}
              onChange={val => setNewPrincipal({...newPrincipal, jurisdiction: val})}
              placeholder="e.g., US-CA, EU-DE, UK"
            />
          </FormGroup>
        </Form>
      </Modal>
    </>
  );
};
```

### 3.4 Agency Realm Settings Tab

**File: `js/apps/admin-ui/src/agency/pages/AgencyRealmSettings.tsx`**
```typescript
import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useRealm } from '../../context/RealmContext';
import { useAgencyConfig } from '../hooks/useAgencyConfig';
import {
  PageSection,
  Form,
  FormGroup,
  Switch,
  TextInput,
  Select,
  SelectOption,
  NumberInput,
  ActionGroup,
  Button,
  Alert,
  AlertGroup,
  Title,
  Divider
} from '@patternfly/react-core';
import { SaveIcon, UndoIcon, BlueprintIcon } from '@patternfly/react-icons';
import { HelpItem } from '../../components/help-enabler/HelpItem';

export const AgencyRealmSettings = () => {
  const { t } = useTranslation();
  const { realm, updateRealm } = useRealm();
  const { config, loading, error, updateConfig } = useAgencyConfig(realm.name);
  
  const [localConfig, setLocalConfig] = useState<AgencyConfig>({
    enabled: false,
    defaultJurisdiction: '',
    complianceMode: 'gdpr',
    requireMandateForDelegation: true,
    maxDelegationDepth: 3,
    auditLevel: 'standard'
  });
  
  const [isSaving, setIsSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [saveSuccess, setSaveSuccess] = useState(false);
  
  useEffect(() => {
    if (config) {
      setLocalConfig(config);
    }
  }, [config]);
  
  const handleSave = async () => {
    setIsSaving(true);
    setSaveError(null);
    setSaveSuccess(false);
    
    try {
      // Update realm attributes
      await updateRealm({
        attributes: {
          ...realm.attributes,
          'agency.enabled': String(localConfig.enabled),
          'agency.defaultJurisdiction': localConfig.defaultJurisdiction,
          'agency.complianceMode': localConfig.complianceMode,
          'agency.requireMandate': String(localConfig.requireMandateForDelegation),
          'agency.maxDepth': String(localConfig.maxDelegationDepth),
          'agency.auditLevel': localConfig.auditLevel
        }
      });
      
      // Update agency-specific config
      await updateConfig(localConfig);
      
      setSaveSuccess(true);
      setTimeout(() => setSaveSuccess(false), 3000);
    } catch (err) {
      setSaveError(err.message || 'Failed to save configuration');
    } finally {
      setIsSaving(false);
    }
  };
  
  return (
    <PageSection>
      <Title headingLevel="h2">
        <BlueprintIcon /> {t('agency.settings.title')}
      </Title>
      <p>{t('agency.settings.description')}</p>
      
      <Divider style={{ margin: '1rem 0' }} />
      
      <Form>
        {saveError && (
          <AlertGroup>
            <Alert variant="danger" title={saveError} isInline />
          </AlertGroup>
        )}
        
        {saveSuccess && (
          <AlertGroup>
            <Alert variant="success" title={t('common.saveSuccess')} isInline />
          </AlertGroup>
        )}
        
        <FormGroup
          label={t('agency.settings.enable')}
          fieldId="agency-enabled"
          labelIcon={
            <HelpItem 
              helpText={t('agency.settings.enableHelp')} 
              fieldLabelId="agency-enabled"
            />
          }
        >
          <Switch
            id="agency-enabled"
            isChecked={localConfig.enabled}
            onChange={checked => setLocalConfig({...localConfig, enabled: checked})}
            label={t('agency.settings.enabled')}
            labelOff={t('agency.settings.disabled')}
          />
        </FormGroup>
        
        {localConfig.enabled && (
          <>
            <FormGroup
              label={t('agency.settings.defaultJurisdiction')}
              fieldId="default-jurisdiction"
              labelIcon={
                <HelpItem 
                  helpText={t('agency.settings.jurisdictionHelp')} 
                  fieldLabelId="default-jurisdiction"
                />
              }
            >
              <TextInput
                id="default-jurisdiction"
                value={localConfig.defaultJurisdiction}
                onChange={val => setLocalConfig({...localConfig, defaultJurisdiction: val})}
                placeholder="e.g., US-CA, EU-DE"
              />
            </FormGroup>
            
            <FormGroup
              label={t('agency.settings.complianceMode')}
              fieldId="compliance-mode"
              labelIcon={
                <HelpItem 
                  helpText={t('agency.settings.complianceHelp')} 
                  fieldLabelId="compliance-mode"
                />
              }
            >
              <Select
                id="compliance-mode"
                selections={localConfig.complianceMode}
                onSelect={(_, val) => setLocalConfig({...localConfig, complianceMode: val as ComplianceMode})}
              >
                <SelectOption value="gdpr">GDPR (European Union)</SelectOption>
                <SelectOption value="ccpa">CCPA (California, USA)</SelectOption>
                <SelectOption value="hipaa">HIPAA (Healthcare, USA)</SelectOption>
                <SelectOption value="sox">SOX (Financial, USA)</SelectOption>
                <SelectOption value="custom">Custom Policy</SelectOption>
              </Select>
            </FormGroup>
            
            <FormGroup
              label={t('agency.settings.requireMandate')}
              fieldId="require-mandate"
            >
              <Switch
                id="require-mandate"
                isChecked={localConfig.requireMandateForDelegation}
                onChange={checked => setLocalConfig({...localConfig, requireMandateForDelegation: checked})}
                label={t('agency.settings.mandateRequired')}
                labelOff={t('agency.settings.mandateOptional')}
              />
            </FormGroup>
            
            <FormGroup
              label={t('agency.settings.maxDepth')}
              fieldId="max-depth"
              labelIcon={
                <HelpItem 
                  helpText={t('agency.settings.maxDepthHelp')} 
                  fieldLabelId="max-depth"
                />
              }
            >
              <NumberInput
                id="max-depth"
                value={localConfig.maxDelegationDepth}
                min={1}
                max={10}
                onChange={val => setLocalConfig({...localConfig, maxDelegationDepth: val})}
              />
            </FormGroup>
            
            <FormGroup
              label={t('agency.settings.auditLevel')}
              fieldId="audit-level"
            >
              <Select
                id="audit-level"
                selections={localConfig.auditLevel}
                onSelect={(_, val) => setLocalConfig({...localConfig, auditLevel: val as any})}
              >
                <SelectOption value="minimal">Minimal - Errors only</SelectOption>
                <SelectOption value="standard">Standard - Access events</SelectOption>
                <SelectOption value="comprehensive">Comprehensive - All actions</SelectOption>
              </Select>
            </FormGroup>
          </>
        )}
        
        <ActionGroup>
          <Button
            variant="primary"
            icon={<SaveIcon />}
            onClick={handleSave}
            isLoading={isSaving}
          >
            {t('common.save')}
          </Button>
          <Button
            variant="link"
            icon={<UndoIcon />}
            onClick={() => setLocalConfig(config || localConfig)}
          >
            {t('common.revert')}
          </Button>
        </ActionGroup>
      </Form>
    </PageSection>
  );
};
```

### 3.5 Integrate Agency Tab into Realm Settings

**File: `js/apps/admin-ui/src/realm-settings/RealmSettings.tsx`**
```typescript
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useRealm } from '../context/RealmContext';
import {
  Tabs,
  Tab,
  TabTitleText,
  TabTitleIcon
} from '@patternfly/react-core';
import {
  CogIcon,
  SignInIcon,
  EnvelopeIcon,
  PaletteIcon,
  KeyIcon,
  CalendarIcon,
  GlobeIcon,
  ShieldAltIcon,
  ClockIcon,
  TokenIcon,
  FileContractIcon,
  UserIcon,
  BlueprintIcon  // Agency icon
} from '@patternfly/react-icons';

// Existing imports...
import { AgencyRealmSettings } from '../agency/pages/AgencyRealmSettings';

export const RealmSettings = () => {
  const { t } = useTranslation();
  const { realm } = useRealm();
  const [activeTab, setActiveTab] = useState(0);
  
  // Check if agency is enabled to show the tab
  const agencyEnabled = realm.attributes?.['agency.enabled'] === 'true';
  
  const tabs = [
    { key: 'general', title: t('realm-settings.general'), icon: <CogIcon />, component: <GeneralSettings /> },
    { key: 'login', title: t('realm-settings.login'), icon: <SignInIcon />, component: <LoginSettings /> },
    { key: 'email', title: t('realm-settings.email'), icon: <EnvelopeIcon />, component: <EmailSettings /> },
    { key: 'themes', title: t('realm-settings.themes'), icon: <PaletteIcon />, component: <ThemesSettings /> },
    { key: 'keys', title: t('realm-settings.keys'), icon: <KeyIcon />, component: <KeysSettings /> },
    { key: 'events', title: t('realm-settings.events'), icon: <CalendarIcon />, component: <EventsSettings /> },
    { key: 'localization', title: t('realm-settings.localization'), icon: <GlobeIcon />, component: <LocalizationSettings /> },
    { key: 'security', title: t('realm-settings.securityDefenses'), icon: <ShieldAltIcon />, component: <SecurityDefenses /> },
    { key: 'sessions', title: t('realm-settings.sessions'), icon: <ClockIcon />, component: <SessionsSettings /> },
    { key: 'tokens', title: t('realm-settings.tokens'), icon: <TokenIcon />, component: <TokenSettings /> },
    { key: 'client-policies', title: t('realm-settings.clientPolicies'), icon: <FileContractIcon />, component: <ClientPolicies /> },
    { key: 'user-profile', title: t('realm-settings.userProfile'), icon: <UserIcon />, component: <UserProfile /> },
    // PASSPORT AGENCY TAB - Always show, but indicate if disabled
    { key: 'agency', title: t('realm-settings.agency'), icon: <BlueprintIcon />, component: <AgencyRealmSettings /> },
  ];
  
  return (
    <Tabs
      activeKey={activeTab}
      onSelect={(_, eventKey) => setActiveTab(eventKey as number)}
      mountOnEnter
      unmountOnExit
    >
      {tabs.map((tab, index) => (
        <Tab
          key={tab.key}
          eventKey={index}
          title={
            <>
              <TabTitleIcon>{tab.icon}</TabTitleIcon>
              <TabTitleText>
                {tab.key === 'agency' && !agencyEnabled && (
                  <span style={{ opacity: 0.6 }}>{tab.title} (Disabled)</span>
                )}
                {tab.key === 'agency' && agencyEnabled && tab.title}
                {tab.key !== 'agency' && tab.title}
              </TabTitleText>
            </>
          }
        >
          {tab.component}
        </Tab>
      ))}
    </Tabs>
  );
};
```

### 3.6 Add Agency Navigation

**File: `js/apps/admin-ui/src/components/nav/LeftNav.tsx`**
```typescript
import { useTranslation } from 'react-i18next';
import { useRealm } from '../../context/RealmContext';
import {
  Nav,
  NavGroup,
  NavItem,
  NavList
} from '@patternfly/react-core';
import {
  HomeIcon,
  UsersIcon,
  UserIcon,
  UsersCogIcon,
  IdCardIcon,
  ClipboardListIcon,
  CogIcon,
  NetworkWiredIcon,
  KeyIcon,
  GlobeIcon,
  FileContractIcon,
  BlueprintIcon,      // Agency
  CertificateIcon,    // Qualifications
  ShareAltIcon,       // Delegates
  FileSignatureIcon   // Mandates
} from '@patternfly/react-icons';
import { Link, useLocation } from 'react-router-dom';

export const LeftNav = () => {
  const { t } = useTranslation();
  const { realm } = useRealm();
  const location = useLocation();
  
  const agencyEnabled = realm.attributes?.['agency.enabled'] === 'true';
  
  const isActive = (path: string) => location.pathname.includes(path);
  
  return (
    <Nav>
      <NavGroup title={t('nav.manage')}>
        <NavItem itemId={0} isActive={isActive('/dashboard')}>
          <Link to={`/${realm.name}/dashboard`}>
            <HomeIcon /> {t('nav.dashboard')}
          </Link>
        </NavItem>
        <NavItem itemId={1} isActive={isActive('/groups')}>
          <Link to={`/${realm.name}/groups`}>
            <UsersIcon /> {t('nav.groups')}
          </Link>
        </NavItem>
        <NavItem itemId={2} isActive={isActive('/users')}>
          <Link to={`/${realm.name}/users`}>
            <UserIcon /> {t('nav.users')}
          </Link>
        </NavItem>
        <NavItem itemId={3} isActive={isActive('/roles')}>
          <Link to={`/${realm.name}/roles`}>
            <UsersCogIcon /> {t('nav.roles')}
          </Link>
        </NavItem>
        <NavItem itemId={4} isActive={isActive('/clients')}>
          <Link to={`/${realm.name}/clients`}>
            <IdCardIcon /> {t('nav.clients')}
          </Link>
        </NavItem>
        <NavItem itemId={5} isActive={isActive('/client-scopes')}>
          <Link to={`/${realm.name}/client-scopes`}>
            <ClipboardListIcon /> {t('nav.clientScopes')}
          </Link>
        </NavItem>
        <NavItem itemId={6} isActive={isActive('/sessions')}>
          <Link to={`/${realm.name}/sessions`}>
            <NetworkWiredIcon /> {t('nav.sessions')}
          </Link>
        </NavItem>
      </NavGroup>
      
      {/* PASSPORT AGENCY SECTION */}
      {agencyEnabled && (
        <NavGroup title={t('nav.agency')}>
          <NavItem itemId={10} isActive={isActive('/agency/principals')}>
            <Link to={`/${realm.name}/agency/principals`}>
              <BlueprintIcon /> {t('nav.principals')}
            </Link>
          </NavItem>
          <NavItem itemId={11} isActive={isActive('/agency/qualifications')}>
            <Link to={`/${realm.name}/agency/qualifications`}>
              <CertificateIcon /> {t('nav.qualifications')}
            </Link>
          </NavItem>
          <NavItem itemId={12} isActive={isActive('/agency/delegates')}>
            <Link to={`/${realm.name}/agency/delegates`}>
              <ShareAltIcon /> {t('nav.delegates')}
            </Link>
          </NavItem>
          <NavItem itemId={13} isActive={isActive('/agency/mandates')}>
            <Link to={`/${realm.name}/agency/mandates`}>
              <FileSignatureIcon /> {t('nav.mandates')}
            </Link>
          </NavItem>
        </NavGroup>
      )}
      
      <NavGroup title={t('nav.configure')}>
        <NavItem itemId={20} isActive={isActive('/realm-settings')}>
          <Link to={`/${realm.name}/realm-settings`}>
            <CogIcon /> {t('nav.realmSettings')}
          </Link>
        </NavItem>
        <NavItem itemId={21} isActive={isActive('/authentication')}>
          <Link to={`/${realm.name}/authentication`}>
            <KeyIcon /> {t('nav.authentication')}
          </Link>
        </NavItem>
        <NavItem itemId={22} isActive={isActive('/identity-providers')}>
          <Link to={`/${realm.name}/identity-providers`}>
            <GlobeIcon /> {t('nav.identityProviders')}
          </Link>
        </NavItem>
        <NavItem itemId={23} isActive={isActive('/user-federation')}>
          <Link to={`/${realm.name}/user-federation`}>
            <FileContractIcon /> {t('nav.userFederation')}
          </Link>
        </NavItem>
      </NavGroup>
    </Nav>
  );
};
```

---

## PHASE 4: DEPLOYMENT CONFIGURATION (Priority: CRITICAL)

### 4.1 Production Docker Compose

**File: `docker-compose.production.yml`**
```yaml
version: '3.8'

services:
  passport-db:
    image: postgres:15-alpine
    container_name: passport-db
    environment:
      POSTGRES_DB: passport
      POSTGRES_USER: passport
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - passport-db-data:/var/lib/postgresql/data
    networks:
      - passport-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U passport"]
      interval: 10s
      timeout: 5s
      retries: 5

  passport-cache:
    image: redis:7-alpine
    container_name: passport-cache
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - passport-cache-data:/data
    networks:
      - passport-network

  passport:
    build:
      context: .
      dockerfile: Dockerfile.passport
    container_name: passport-iam
    environment:
      # Database
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://passport-db:5432/passport
      KC_DB_USERNAME: passport
      KC_DB_PASSWORD: ${DB_PASSWORD}
      
      # Cache (Infinispan with Redis for sessions)
      KC_CACHE: ispn
      KC_CACHE_STACK: kubernetes
      
      # Hostname configuration
      KC_HOSTNAME: passport.aetherpro.tech
      KC_HOSTNAME_ADMIN: passport.aetherpro.tech
      KC_HOSTNAME_STRICT: "false"
      KC_HOSTNAME_STRICT_HTTPS: "true"
      
      # Proxy configuration
      KC_PROXY: edge
      
      # HTTP/TLS
      KC_HTTP_ENABLED: "true"
      KC_HTTP_PORT: 8080
      KC_HTTPS_PORT: 8443
      
      # Admin credentials (change after first login)
      PASSPORT_ADMIN: ${ADMIN_USER:-admin}
      PASSPORT_ADMIN_PASSWORD: ${ADMIN_PASSWORD}
      
      # Agency extensions
      PASSPORT_AGENCY_ENABLED: "true"
      PASSPORT_ATP_ENDPOINT: ${ATP_ENDPOINT}
      PASSPORT_CMC_ENDPOINT: ${CMC_ENDPOINT}
      PASSPORT_POLICY_ROUTER: ${POLICY_ROUTER_ENDPOINT}
      
      # Feature flags
      KC_FEATURES: token-exchange,admin-fine-grained-authz,declarative-user-profile
      
    ports:
      - "8080:8080"
      - "8443:8443"
    depends_on:
      passport-db:
        condition: service_healthy
      passport-cache:
        condition: service_started
    networks:
      - passport-network
    volumes:
      - passport-data:/opt/passport/data
    command: start --optimized --spi-theme-default=passport

  # ATP Integration (if running alongside)
  atp-collector:
    image: aetherpro/atp-collector:latest
    container_name: atp-collector
    environment:
      REDIS_URL: redis://:${REDIS_PASSWORD}@passport-cache:6379
      POSTGRES_URL: postgresql://passport:${DB_PASSWORD}@passport-db:5432/passport
    networks:
      - passport-network
    profiles:
      - full-stack

volumes:
  passport-db-data:
  passport-cache-data:
  passport-data:

networks:
  passport-network:
    driver: bridge
```

### 4.2 Environment Template

**File: `.env.production.template`**
```bash
# Database
DB_PASSWORD=generate-strong-password-here

# Redis Cache
REDIS_PASSWORD=generate-different-strong-password

# Admin
ADMIN_USER=admin
ADMIN_PASSWORD=change-immediately-after-setup

# Domain Configuration
PRIMARY_DOMAIN=passport.aetherpro.tech
FALLBACK_DOMAIN=passport.aetherpro.us

# AetherPro Ecosystem Integration
ATP_ENDPOINT=https://atp.aetherpro.tech
CMC_ENDPOINT=https://cmc.aetherpro.tech
POLICY_ROUTER_ENDPOINT=https://router.aetherpro.tech

# Agency Configuration
DEFAULT_JURISDICTION=US-CA
COMPLIANCE_MODE=gdpr

# Email (SMTP for notifications)
SMTP_HOST=smtp.sendgrid.net
SMTP_PORT=587
SMTP_USER=apikey
SMTP_PASSWORD=your-sendgrid-api-key
SMTP_FROM=noreply@aetherpro.tech
SMTP_FROM_DISPLAY="Passport IAM"

# SSL/TLS Certificates (Let's Encrypt via Traefik or manual)
SSL_CERT_PATH=/etc/letsencrypt/live/passport.aetherpro.tech/fullchain.pem
SSL_KEY_PATH=/etc/letsencrypt/live/passport.aetherpro.tech/privkey.pem
```

### 4.3 Nginx Reverse Proxy Config

**File: `nginx/passport.conf`**
```nginx
server {
    listen 80;
    server_name passport.aetherpro.tech passport.aetherpro.us;
    
    # Redirect to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name passport.aetherpro.tech passport.aetherpro.us;
    
    # SSL Certificates (Let's Encrypt)
    ssl_certificate /etc/letsencrypt/live/passport.aetherpro.tech/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/passport.aetherpro.tech/privkey.pem;
    
    # SSL Configuration
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 1d;
    
    # Security headers
    add_header Strict-Transport-Security "max-age=63072000" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
    
    # Proxy to Passport
    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;
        
        # WebSocket support (for admin UI real-time features)
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # Health check endpoint
    location /health {
        proxy_pass http://localhost:8080/health;
        access_log off;
    }
}
```

---

## PHASE 5: ECOSYSTEM SSO INTEGRATION (Priority: HIGH)

### 5.1 Client Configuration for Each Service

**File: `scripts/setup-sso-clients.sh`**
```bash
#!/bin/bash

# Script to configure SSO clients for all AetherPro services
# Run after Passport is deployed

PASSPORT_URL="https://passport.aetherpro.tech"
ADMIN_TOKEN=$(curl -s -X POST "$PASSPORT_URL/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=$ADMIN_USER" \
  -d "password=$ADMIN_PASSWORD" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | jq -r '.access_token')

REALM="aetherpro"

# Function to create OIDC client
create_oidc_client() {
  local CLIENT_ID=$1
  local CLIENT_NAME=$2
  local REDIRECT_URIS=$3
  local WEB_ORIGINS=$4
  
  curl -X POST "$PASSPORT_URL/admin/realms/$REALM/clients" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"clientId\": \"$CLIENT_ID\",
      \"name\": \"$CLIENT_NAME\",
      \"enabled\": true,
      \"protocol\": \"openid-connect\",
      \"redirectUris\": [$REDIRECT_URIS],
      \"webOrigins\": [$WEB_ORIGINS],
      \"publicClient\": false,
      \"secret\": \"$(openssl rand -hex 32)\",
      \"standardFlowEnabled\": true,
      \"implicitFlowEnabled\": false,
      \"directAccessGrantsEnabled\": true,
      \"serviceAccountsEnabled\": true,
      \"authorizationServicesEnabled\": true
    }"
}

# 1. AetherAgentForge.org - Agent hosting platform
create_oidc_client \
  "agent-forge" \
  "Aether Agent Forge" \
  '"https://aetheragentforge.org/*", "https://agent-forge.aetherpro.tech/*"' \
  '"https://aetheragentforge.org", "https://agent-forge.aetherpro.tech"'

# 2. aetherpro.tech - Chat interface (previously built)
create_oidc_client \
  "aether-chat" \
  "AetherPro Chat" \
  '"https://aetherpro.tech/*", "https://chat.aetherpro.tech/*"' \
  '"https://aetherpro.tech", "https://chat.aetherpro.tech"'

# 3. MCPFabric.space - A2A/MCP Registry
create_oidc_client \
  "mcp-fabric" \
  "MCP Fabric Registry" \
  '"https://mcpfabric.space/*", "https://mcp.aetherpro.tech/*"' \
  '"https://mcpfabric.space", "https://mcp.aetherpro.tech"'

# 4. blackboxaudio.tech - Voice stack
create_oidc_client \
  "blackbox-audio" \
  "BlackBox Audio" \
  '"https://blackboxaudio.tech/*", "https://voice.aetherpro.tech/*"' \
  '"https://blackboxaudio.tech", "https://voice.aetherpro.tech"'

echo "SSO clients configured. Save the generated secrets securely."
```

### 5.2 Agency-Aware Authentication Flow

**File: `passport-extensions/agency/src/main/java/com/aetherpro/passport/agency/authentication/AgencyAuthenticator.java`**
```java
package com.aetherpro.passport.agency.authentication;

import com.aetherpro.passport.agency.AgencyProvider;
import com.aetherpro.passport.agency.AgencyContext;
import org.passport.authentication.AuthenticationFlowContext;
import org.passport.authentication.Authenticator;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;

/**
 * Custom authenticator that enforces Agency/LBAC rules during login
 * Integrates with Policy Router for complex decisions
 */
public class AgencyAuthenticator implements Authenticator {
    
    public static final String PROVIDER_ID = "agency-authenticator";
    
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel user = context.getUser();
        RealmModel realm = context.getRealm();
        PassportSession session = context.getSession();
        
        AgencyProvider agency = session.getProvider(AgencyProvider.class);
        
        // Check if user has active agency context
        AgencyContext agencyContext = agency.getAgencyContext(user);
        
        if (agencyContext != null && !agencyContext.getRepresenting().isEmpty()) {
            // User is acting as agent - verify mandates are valid
            boolean mandatesValid = agencyContext.getRepresenting().stream()
                .allMatch(ref -> {
                    // Check if delegation is still valid
                    return agency.isValidDelegate(user, ref.getPrincipalId(), "login");
                });
            
            if (!mandatesValid) {
                context.failure(AuthenticationFlowError.INVALID_USER);
                return;
            }
            
            // Store agency context in authentication session for token enrichment
            context.getAuthenticationSession().setAuthNote(
                "agency.context", 
                serializeAgencyContext(agencyContext)
            );
        }
        
        context.success();
    }
    
    @Override
    public void action(AuthenticationFlowContext context) {
        // Handle any required agency verification steps
        context.success();
    }
    
    @Override
    public boolean requiresUser() {
        return true;
    }
    
    @Override
    public boolean configuredFor(PassportSession session, RealmModel realm, UserModel user) {
        // Always configured - agency check is optional based on user attributes
        return true;
    }
    
    @Override
    public void setRequiredActions(PassportSession session, RealmModel realm, UserModel user) {
        // No required actions
    }
    
    @Override
    public void close() {}
    
    private String serializeAgencyContext(AgencyContext context) {
        // JSON serialization
        return "{}"; // Implement properly
    }
}
```

### 5.3 Agency Claims Protocol Mapper

**File: `passport-extensions/agency/src/main/java/com/aetherpro/passport/agency/protocol/AgencyProtocolMapper.java`**
```java
package com.aetherpro.passport.agency.protocol;

import com.aetherpro.passport.agency.AgencyProvider;
import com.aetherpro.passport.agency.AgencyContext;
import org.passport.models.PassportSession;
import org.passport.models.ProtocolMapperModel;
import org.passport.models.UserSessionModel;
import org.passport.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.passport.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.passport.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.passport.protocol.oidc.mappers.UserInfoTokenMapper;
import org.passport.provider.ProviderConfigProperty;
import org.passport.representations.IDToken;
import org.passport.representations.AccessToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Injects Agency/LBAC claims into JWT tokens
 * Enables downstream services to make authorization decisions
 */
public class AgencyProtocolMapper extends AbstractOIDCProtocolMapper 
    implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {
    
    public static final String PROVIDER_ID = "oidc-agency-mapper";
    public static final String DISPLAY_TYPE = "Agency Context";
    
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();
    
    static {
        // Configuration options
        ProviderConfigProperty includePrincipals = new ProviderConfigProperty();
        includePrincipals.setName("include.principals");
        includePrincipals.setLabel("Include Representing Principals");
        includePrincipals.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        includePrincipals.setDefaultValue("true");
        includePrincipals.setHelpText("Include list of principals the user represents");
        configProperties.add(includePrincipals);
        
        ProviderConfigProperty includeMandates = new ProviderConfigProperty();
        includeMandates.setName("include.mandates");
        includeMandates.setLabel("Include Active Mandates");
        includeMandates.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        includeMandates.setDefaultValue("true");
        includeMandates.setHelpText("Include active mandate scopes");
        configProperties.add(includeMandates);
        
        ProviderConfigProperty includePassport = new ProviderConfigProperty();
        includePassport.setName("include.passport");
        includePassport.setLabel("Include Agent Passport");
        includePassport.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        includePassport.setDefaultValue("true");
        includePassport.setHelpText("Include agent passport ID if applicable");
        configProperties.add(includePassport);
    }
    
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }
    
    @Override
    public String getDisplayType() {
        return DISPLAY_TYPE;
    }
    
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
    
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }
    
    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel,
                          UserSessionModel userSession, PassportSession passportSession) {
        
        AgencyProvider agency = passportSession.getProvider(AgencyProvider.class);
        AgencyContext context = agency.getAgencyContext(userSession.getUser());
        
        if (context == null) {
            return; // No agency context
        }
        
        Map<String, Object> agencyClaim = new HashMap<>();
        
        // Always include basic info
        agencyClaim.put("is_agent", context.isAgent());
        agencyClaim.put("primary_passport", context.getPrimaryPassportId());
        
        // Include principals if configured
        if (parseBoolean(mappingModel.getConfig().get("include.principals"))) {
            List<Map<String, Object>> principals = new ArrayList<>();
            for (var ref : context.getRepresenting()) {
                Map<String, Object> p = new HashMap<>();
                p.put("id", ref.getPrincipalId());
                p.put("name", ref.getPrincipalName());
                p.put("scopes", ref.getActiveMandateScopes());
                principals.add(p);
            }
            agencyClaim.put("representing", principals);
        }
        
        // Include active scopes
        if (parseBoolean(mappingModel.getConfig().get("include.mandates"))) {
            agencyClaim.put("active_scopes", context.getActiveScopes());
        }
        
        // Include qualifications
        agencyClaim.put("qualifications", context.getQualifications());
        
        // Set the claim
        token.getOtherClaims().put("agency", agencyClaim);
        
        // Also set individual claims for easier access
        if (context.isAgent()) {
            token.getOtherClaims().put("agent_passport_id", context.getPrimaryPassportId());
        }
    }
    
    @Override
    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel,
                                          PassportSession session, UserSessionModel userSession) {
        setClaim(token, mappingModel, userSession, session);
        return token;
    }
    
    @Override
    public IDToken transformIDToken(IDToken token, ProtocolMapperModel mappingModel,
                                   PassportSession session, UserSessionModel userSession) {
        setClaim(token, mappingModel, userSession, session);
        return token;
    }
    
    private boolean parseBoolean(String value) {
        return value == null || Boolean.parseBoolean(value);
    }
}
```

---

## DEPLOYMENT CHECKLIST

### Pre-Deployment
- [ ] Domain DNS configured (passport.aetherpro.tech → server IP)
- [ ] SSL certificates obtained (Let's Encrypt)
- [ ] Database passwords generated
- [ ] Docker and Docker Compose installed on server
- [ ] Ports 80, 443, 8080 open in firewall

### Deployment Steps
1. Build Passport image: `docker-compose -f docker-compose.production.yml build`
2. Start database and cache: `docker-compose up -d passport-db passport-cache`
3. Run migrations: `docker-compose run --rm passport migrate`
4. Start Passport: `docker-compose up -d passport`
5. Configure Nginx: `sudo cp nginx/passport.conf /etc/nginx/sites-available/ && sudo ln -s /etc/nginx/sites-available/passport.conf /etc/nginx/sites-enabled/`
6. Obtain SSL: `sudo certbot --nginx -d passport.aetherpro.tech -d passport.aetherpro.us`
7. Test: `curl https://passport.aetherpro.tech/health`

### Post-Deployment
- [ ] Create admin user
- [ ] Configure realm "aetherpro"
- [ ] Enable Agency in realm settings
- [ ] Run `setup-sso-clients.sh` to configure clients
- [ ] Test login from each service
- [ ] Configure ATP integration for audit logging
- [ ] Set up monitoring/alerting

---

## SUCCESS CRITERIA

1. **Visual Parity**: Passport UI matches Passport screenshots exactly (full width, proper tables)
2. **Feature Parity**: All Passport 22+ features work (OIDC, SAML, LDAP, etc.)
3. **Agency Extensions**: 
   - Can create Principals, Delegates, Mandates
   - Agency tab visible in realm settings
   - Agency claims appear in JWT tokens
4. **SSO Integration**: 
   - AetherAgentForge.org login redirects to Passport
   - aetherpro.tech chat interface uses Passport auth
   - MCPFabric.space authenticates via Passport
   - blackboxaudio.tech uses Passport for voice auth
5. **Revenue Features**:
   - Can mint Agent Passports
   - Usage tracked for billing
6. **Deployment**:
   - Live on passport.aetherpro.tech
   - SSL working
   - Health checks passing

---

Execute this implementation in the following order:
1. Fork and basic rebrand (Day 1)
2. Agency SPI and database (Day 1-2)
3. REST API layer (Day 2)
4. Frontend Agency UI (Day 3)
5. Integration and testing (Day 4)
6. Deployment (Day 5)

Total estimated time: 3-5 days for full implementation and deployment.
```

This prompt gives Kimi Code everything needed: exact file paths, complete code implementations, deployment configurations, and integration points for your entire ecosystem. The structure ensures you get a production-ready Passport IAM that can immediately serve as the identity backbone for all your services.
