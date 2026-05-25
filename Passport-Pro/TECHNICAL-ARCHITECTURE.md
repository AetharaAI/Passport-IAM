# Passport IAM + Agency Extension
## Technical Architecture Document

**Version:** 1.0  
**Date:** February 2026  
**Author:** AetherPro Technologies  
**Classification:** For Grant Applications &amp; Technical Review

---

## Executive Summary

Passport is a sovereign identity and access management (IAM) platform built on the Keycloak foundation, extended with the **Agency** module for legally-compliant autonomous agent identity management. The system provides cryptographically-backed identity credentials for both humans and AI agents, with built-in authorization delegation, mandate scoping, and comprehensive audit capabilities.

**Core Innovation:** Agent Passports—DID-based persistent identities for autonomous AI agents with verifiable authority chains, enabling legally-accountable AI operations.

---

## 1. Technology Stack

### 1.1 Foundation Layer

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Core IAM** | Keycloak 26.2 (Fork) | OAuth 2.0, OIDC, SAML 2.0, User Federation |
| **Runtime** | Quarkus 3.x | High-performance Java runtime |
| **Database** | PostgreSQL 16 | Primary data store |
| **Cache** | Redis Stack | Session management, rate limiting |
| **Language** | Java 21 (LTS) | Core implementation |
| **Build** | Maven 3.9+ | Dependency management |
| **Container** | Docker/OCI | Deployment packaging |

### 1.2 Security Standards Compliance

| Standard | Status | Notes |
|----------|--------|-------|
| **OAuth 2.0** (RFC 6749) | ✅ Full | Authorization framework |
| **OpenID Connect 1.0** | ✅ Full | Authentication layer |
| **SAML 2.0** | ✅ Full | Enterprise SSO |
| **FIPS 140-2** | ✅ Provider | Crypto module available |
| **PKCE** (RFC 7636) | ✅ Full | Public client security |
| **JWT** (RFC 7519) | ✅ Full | Token format |
| **DID** (W3C) | 🔄 Partial | Agent Passport identifiers |

---

## 2. Agency Extension Architecture

### 2.1 Domain Model

```
┌─────────────────────────────────────────────────────────────────────┐
│                            REALM                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────┐         ┌──────────────┐        ┌──────────────┐ │
│  │  PRINCIPAL   │◄────────│   DELEGATE   │────────►│    USER      │ │
│  │  (Legal      │  owns   │  (Authority  │  grants │  (Human or   │ │
│  │   Entity)    │         │   Binding)   │         │   Agent)     │ │
│  └──────┬───────┘         └──────┬───────┘        └──────────────┘ │
│         │                        │                                  │
│         │ issues                 │ authorizes                       │
│         ▼                        ▼                                  │
│  ┌──────────────┐         ┌──────────────┐                         │
│  │    AGENT     │         │   MANDATE    │                         │
│  │   PASSPORT   │         │  (Scoped     │                         │
│  │  (DID-based) │         │   Action)    │                         │
│  └──────────────┘         └──────────────┘                         │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2 Core Entities

#### 2.2.1 Principal
A **legal entity** that can hold rights and grant delegations.

```java
public interface PrincipalModel {
    String getId();                    // Unique identifier
    String getRealmId();               // Tenant isolation
    String getName();                  // Display name
    PrincipalType getType();           // INDIVIDUAL | ORGANIZATION | SYSTEM | AI_AGENT | SMART_CONTRACT
    String getJurisdiction();          // ISO 3166-2 legal jurisdiction
    String getMetadata();              // JSON extensible metadata
    boolean isActive();                // Active/suspended status
    Instant getSuspendedAt();          // Suspension timestamp
    String getSuspensionReason();      // Audit trail
}
```

**Principal Types:**
- `INDIVIDUAL` — Natural person
- `ORGANIZATION` — Corporation, NGO, government agency
- `SYSTEM` — Service account
- `AI_AGENT` — Autonomous AI with persistent identity
- `SMART_CONTRACT` — Blockchain-based automated entity

#### 2.2.2 Delegate
An **authority binding** between a user/agent and a principal.

```java
public interface DelegateModel {
    String getId();
    String getAgentId();               // User who receives authority
    String getPrincipalId();           // Principal granting authority
    DelegationType getType();          // FULL | LIMITED | CONDITIONAL | EMERGENCY | TEMPORARY
    String getConstraints();           // JSON constraint rules
    Instant getValidFrom();            // Time-bounded validity
    Instant getValidUntil();
    Instant getRevokedAt();            // Revocation with audit
    String getRevocationReason();
    
    boolean isCurrentlyValid();        // Runtime validity check
}
```

**Delegation Types:**
- `FULL` — Complete authority to act for principal
- `LIMITED` — Restricted to specific scopes
- `CONDITIONAL` — Requires additional conditions
- `EMERGENCY` — Break-glass access with enhanced audit
- `TEMPORARY` — Auto-expiring delegation

#### 2.2.3 Mandate
A **scoped authorization** attached to a delegate.

```java
public interface MandateModel {
    String getId();
    String getDelegateId();            // Parent delegate
    String getScope();                 // e.g., "contracts.sign", "payments.approve:limit:10000"
    String getConstraints();           // JSON constraint rules
    Double getMaxAmount();             // Financial limits
    boolean requiresSecondFactor();    // 2FA enforcement
    Integer getUsageCount();           // Usage tracking
    Instant getLastUsedAt();
    Instant getValidFrom();
    Instant getValidUntil();
    Instant getSuspendedAt();
    String getSuspensionReason();
    
    boolean isCurrentlyValid();
    boolean isWithinAmountLimit(Double requestedAmount);
}
```

**Mandate Capabilities:**
- Scope-based permission (e.g., `contracts.sign`, `payments.transfer`)
- Financial authorization limits
- Usage counting and rate limiting
- Time-bounded validity
- 2FA enforcement option
- Suspension with reason tracking

#### 2.2.4 Agent Passport
A **DID-based persistent identity** for AI agents.

```java
public interface AgentPassport {
    String getId();                    // Internal UUID
    String getPassportDid();           // did:passport:<uuid>
    String getPrincipalId();           // Owning principal
    String getAgentType();             // ai-assistant | autonomous-agent | human-proxy
    String getCapabilities();          // JSON capability array
    String getRateLimits();            // JSON rate limit config
    Instant getMintedAt();             // Issuance timestamp
    Instant getExpiresAt();            // Expiration
    Instant getRevokedAt();            // Revocation
    String getRevocationReason();
    Integer getUsageCount();           // Lifetime usage
    Instant getLastUsedAt();
    
    boolean isCurrentlyValid();
}
```

**Agent Passport Features:**
- **DID Format:** `did:passport:<uuid>` — W3C DID-compatible identifier
- **Capability Declaration:** JSON array of permitted actions
- **Rate Limiting:** Configurable per-passport limits
- **Usage Tracking:** Count and timestamp of all uses
- **Revocation:** Immediate invalidation with audit trail
- **Expiration:** Time-bounded validity

#### 2.2.5 Qualification
A **credential or certification** that may be required for certain actions.

```java
public interface QualificationModel {
    String getId();
    String getName();                  // e.g., "FINRA Series 7"
    String getType();                  // certification | license | clearance | training
    String getIssuer();                // Issuing authority
    String getScope();                 // Actions enabled by this qualification
    Integer getValidityMonths();       // Default validity period
    String getDescription();
}
```

---

## 3. OIDC Token Integration

### 3.1 Agency Claims Protocol Mapper

The `AgencyClaimProtocolMapper` injects agency context into OIDC tokens:

```json
{
  "sub": "user-uuid",
  "agency": {
    "principals": [
      {
        "id": "principal-uuid",
        "name": "AetherPro Technologies",
        "type": "organization",
        "delegate_id": "delegate-uuid",
        "delegate_type": "limited"
      }
    ],
    "mandates": [
      {
        "id": "mandate-uuid",
        "scope": "contracts.sign",
        "principal_id": "principal-uuid",
        "valid_until": 1739500800000
      }
    ],
    "passports": [
      {
        "id": "passport-uuid",
        "principal_id": "principal-uuid",
        "capabilities": "[\"chat\", \"file_read\"]",
        "valid_until": 1739500800000
      }
    ]
  }
}
```

### 3.2 Claim Configuration

| Claim | Description | Configurable |
|-------|-------------|--------------|
| `principals` | Entities user can act for | ✅ Toggle |
| `mandates` | Active authorization scopes | ✅ Toggle |
| `passports` | Agent Passport IDs | ✅ Toggle |
| `claim.prefix` | Claim namespace | ✅ Custom |

---

## 4. Admin REST API

### 4.1 Endpoints Overview

| Resource | Endpoint | Operations |
|----------|----------|------------|
| **Principals** | `/admin/realms/{realm}/agency/principals` | CRUD, suspend, activate |
| **Delegates** | `/admin/realms/{realm}/agency/delegates` | Create, revoke, list by user/principal |
| **Mandates** | `/admin/realms/{realm}/agency/mandates` | CRUD, validate, suspend |
| **Passports** | `/admin/realms/{realm}/agency/passports` | Mint, revoke, list by principal |
| **Config** | `/admin/realms/{realm}/agency/config` | Realm-level settings |

### 4.2 Key Operations

#### Principal Management
- `GET /principals` — List with filters (type, jurisdiction, search)
- `POST /principals` — Create new principal
- `PUT /principals/{id}` — Update principal
- `POST /principals/{id}/suspend` — Suspend with reason
- `POST /principals/{id}/activate` — Reactivate

#### Delegate Management
- `GET /users/{userId}/delegates` — Get delegations for user
- `POST /users/{userId}/delegates` — Create delegation
- `DELETE /delegates/{delegateId}` — Revoke delegation

#### Mandate Management
- `GET /delegates/{delegateId}/mandates` — List mandates
- `POST /delegates/{delegateId}/mandates` — Create mandate
- `POST /mandates/validate` — Runtime validation check
- `POST /mandates/{mandateId}/suspend` — Suspend mandate

#### Agent Passport Management
- `GET /principals/{principalId}/passports` — List passports
- `POST /principals/{principalId}/passports` — **Mint** new passport
- `GET /passports/{passportId}` — Get passport details
- `DELETE /passports/{passportId}` — **Revoke** passport

---

## 5. Data Persistence

### 5.1 JPA Entity Mapping

| Model | Entity | Table |
|-------|--------|-------|
| PrincipalModel | PrincipalEntity | `agency_principal` |
| DelegateModel | DelegateEntity | `agency_delegate` |
| MandateModel | MandateEntity | `agency_mandate` |
| AgentPassport | AgentPassportEntity | `agency_passport` |
| QualificationModel | QualificationEntity | `agency_qualification` |
| AgencyRealmConfig | AgencyRealmConfigEntity | `agency_realm_config` |

### 5.2 Schema Isolation

All Agency tables are prefixed with `agency_` and isolated per-realm via `realm_id` foreign key, ensuring multi-tenant data separation.

---

## 6. Legal &amp; Compliance Alignment

### 6.1 Current Implementation Strengths

| Requirement | Implementation | Status |
|-------------|----------------|--------|
| **Authority Chain** | Principal → Delegate → Mandate | ✅ Implemented |
| **Time-Bounded Delegation** | `validFrom`, `validUntil` on all entities | ✅ Implemented |
| **Revocation** | `revokedAt`, `revocationReason` fields | ✅ Implemented |
| **Suspension** | Distinct from revocation, reversible | ✅ Implemented |
| **Jurisdiction** | ISO 3166-2 on Principal | ✅ Implemented |
| **Financial Limits** | `maxAmount` on Mandate | ✅ Implemented |
| **2FA Enforcement** | `requiresSecondFactor` on Mandate | ✅ Implemented |
| **Usage Tracking** | `usageCount`, `lastUsedAt` | ✅ Implemented |
| **DID Identifier** | `did:passport:<uuid>` format | ✅ Implemented |

### 6.2 Identified Gaps

| Gap | Description | Recommendation | Priority |
|-----|-------------|----------------|----------|
| **Cryptographic Binding** | Agent Passports lack keypair binding | Add public key field to AgentPassport, require signature for critical operations | **HIGH** |
| **Audit Event Log** | No dedicated audit trail table | Implement `agency_audit_log` with immutable event records | **HIGH** |
| **Consent Records** | No explicit consent tracking | Add consent entity for GDPR/privacy compliance | **MEDIUM** |
| **Qualification Verification** | Qualifications exist but aren't enforced | Integrate with mandate validation | **MEDIUM** |
| **Agent Provenance** | No model/version tracking for AI agents | Add `modelId`, `modelVersion` to AgentPassport | **MEDIUM** |
| **Rate Limit Enforcement** | Config exists but no runtime enforcement | Implement Redis-based rate limiting | **LOW** |
| **Cross-Realm Delegation** | Delegates are realm-scoped | Design cross-realm trust model if needed | **LOW** |

### 6.3 Privacy Considerations

**Current State:**
- Agent identification via DID (`did:passport:<uuid>`)
- UUID is pseudonymous but can be linked to principal

**Recommendations:**
1. **Hashed Keys for Tracking:** Use `SHA-256(public_key)` as tracking identifier rather than raw DID
2. **Selective Disclosure:** Implement JWT-SD (Selective Disclosure) for Agent Passport claims
3. **Data Retention Policy:** Add `retentionDays` to realm config for automatic purging
4. **Anonymization:** Provide API to anonymize revoked passports after retention period

---

## 7. Security Model

### 7.1 Authentication Flows

| Actor | Flow | Token Type |
|-------|------|------------|
| Human User | Authorization Code + PKCE | Access Token + ID Token |
| AI Agent | Client Credentials | Access Token |
| Service | Client Credentials | Access Token |

### 7.2 Authorization Model

```
Token Validation
       │
       ▼
┌──────────────┐
│ Verify JWT   │
│ (Passport)   │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ Check Agency │
│   Claims     │
└──────┬───────┘
       │
       ▼
┌──────────────┐     ┌──────────────┐
│ Validate     │────►│ Check        │
│ Mandate      │     │ Constraints  │
└──────┬───────┘     └──────────────┘
       │
       ▼
┌──────────────┐
│ Authorize    │
│ Action       │
└──────────────┘
```

---

## 8. Deployment Architecture

```
                    ┌─────────────────┐
                    │   Cloudflare    │
                    │   / Nginx       │
                    └────────┬────────┘
                             │ HTTPS
                             ▼
┌────────────────────────────────────────────────────┐
│                    Docker Host                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────┐  │
│  │   Passport   │  │  PostgreSQL  │  │  Redis   │  │
│  │   Server     │◄─│      16      │  │  Stack   │  │
│  │   (8080)     │  │   (5432)     │  │  (6379)  │  │
│  └──────────────┘  └──────────────┘  └──────────┘  │
│         │                                           │
│         └───────── passport-network ────────────────┤
└────────────────────────────────────────────────────┘
```

---

## 9. Appendix

### 9.1 Build Commands

```bash
# Build distribution (skip tests)
./mvnw clean install -pl quarkus/dist -am -DskipTests -DskipProtoLock=true

# Distribution output
quarkus/dist/target/passport-999.0.0-SNAPSHOT.tar.gz
```

### 9.2 Configuration

```properties
# Agency Realm Config
agency.enabled=true
agency.default-passport-validity-days=365
agency.require-2fa-for-financial-mandates=true
agency.max-delegates-per-principal=100
```

### 9.3 References

- [OAuth 2.0 RFC 6749](https://tools.ietf.org/html/rfc6749)
- [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)
- [W3C Decentralized Identifiers (DIDs)](https://www.w3.org/TR/did-core/)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [NIST SP 800-63B Digital Identity Guidelines](https://pages.nist.gov/800-63-3/)

---

## Document Control

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-02-08 | AetherPro Technologies | Initial release |
