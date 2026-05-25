# Three-Party Cryptographic Signature Chain - Explained

## 🔐 The Core Concept

Every AI agent action is cryptographically verifiable through a chain of trust:

```
ISSUER (Passport Realm)
   │ Signs the Principal's public key
   ▼
PRINCIPAL (Legal Entity)
   │ Signs the Delegate's public key
   ▼
DELEGATE (AI Agent)
   │ Signs the Action
   ▼
ACTION (email.read, contracts.sign, etc.)
```

This creates an **immutable proof** that:
1. The Issuer certified the Principal exists
2. The Principal authorized the Delegate
3. The Delegate performed the Action

---

## 🔑 Keypair Generation

### When Keypairs are Created

| Entity | Trigger | Key Type | Purpose |
|--------|---------|----------|---------|
| **Issuer** | Realm initialization | Ed25519 | Root of trust - signs all Principals in this realm |
| **Principal** | `createPrincipal()` | Ed25519 | Entity key - signs all Delegates/Mandates |
| **Delegate** | `createAgentPassport()` | Ed25519 | Agent key - signs all actions |

### Key Storage

```
AGENCY_KEYPAIRS table:
┌─────────────┬──────────────┬─────────────┬────────────────────────┬─────────────────────────┐
│ entity_type │ entity_id    │ kid         │ public_key_bytes       │ encrypted_private_key   │
├─────────────┼──────────────┼─────────────┼────────────────────────┼─────────────────────────┤
│ ISSUER      │ realm-uuid   │ sha256(pub) │ Ed25519 public key     │ AES-GCM(Ed25519 priv)  │
│ PRINCIPAL   │ principal-id │ sha256(pub) │ Ed25519 public key     │ AES-GCM(Ed25519 priv)  │
│ DELEGATE    │ passport-id  │ sha256(pub) │ Ed25519 public key     │ AES-GCM(Ed25519 priv)  │
└─────────────┴──────────────┴─────────────┴────────────────────────┴─────────────────────────┘
```

**Security:**
- Private keys encrypted at rest with AES-256-GCM
- Key ID (KID) = SHA-256 hash of public key (content-addressed)
- Master encryption key from realm config (TODO: move to HSM/Vault)

---

## 📝 Signature Process

### 1. Agent Passport Issuance

**When:** `createAgentPassport()` is called

**Steps:**
1. Generate Ed25519 keypair for the agent (Delegate)
2. Build passport payload:
   ```json
   {
     "passport_did": "did:passport:abc123",
     "principal_id": "principal-uuid",
     "capabilities": ["reason", "code"],
     "issued_at": 1739500800000,
     "expires_at": 1771036800000,
     "delegate_public_key": "base64-encoded-public-key"
   }
   ```
3. Sign payload with Issuer's private key → `issuer_signature`
4. Store in database:
   ```sql
   UPDATE PASSPORT_AGENT_IDENTITY
   SET issuer_signature = '<JWS signature>',
       issuer_kid = 'realm-key-001',
       delegate_kid = 'agent-key-001'
   WHERE id = 'passport-uuid';
   ```

**Verification:**
Anyone can verify: `verify(issuer_public_key, passport_payload, issuer_signature)`

---

### 2. Mandate Issuance

**When:** `createMandate()` is called

**Steps:**
1. Build mandate payload:
   ```json
   {
     "id": "mandate-uuid",
     "scope": "email.read",
     "principal_id": "principal-uuid",
     "delegate_id": "delegate-uuid",
     "valid_from": 1739500800000,
     "valid_until": 1771036800000,
     "constraints": "{\"max_requests\": 1000}"
   }
   ```
2. Sign with Principal's private key → `principal_signature`
3. Store in database:
   ```sql
   UPDATE PASSPORT_MANDATE
   SET principal_signature = '<JWS signature>',
       principal_kid = 'principal-key-001'
   WHERE id = 'mandate-uuid';
   ```

**Verification:**
Anyone can verify: `verify(principal_public_key, mandate_payload, principal_signature)`

---

### 3. Action Execution

**When:** AI agent wants to perform an action

**Steps:**
1. Agent builds action payload:
   ```json
   {
     "action": "email.read",
     "target": "inbox",
     "timestamp": 1739500800123,
     "mandate_id": "mandate-uuid"
   }
   ```
2. Agent signs with its private key:
   ```javascript
   POST /realms/master/agency/sign-action
   {
     "delegate_kid": "agent-key-001",
     "action_data": "{\"action\":\"email.read\", ...}"
   }
   ```
3. Response includes full chain:
   ```json
   {
     "signed_action": {
       "action_data": "{...}",
       "delegate_kid": "agent-key-001",
       "delegate_signature": "...",
       "principal_kid": "principal-key-001",
       "principal_signature": "...",
       "issuer_kid": "realm-key-001",
       "issuer_signature": "...",
       "timestamp": 1739500800123
     }
   }
   ```
4. Service receives signed action and verifies ALL THREE signatures

---

## ✅ Chain Verification

**Endpoint:** `POST /realms/{realm}/agency/verify-chain`

**Process:**
```javascript
function verifyChain(signedAction) {
  // 1. Verify Delegate signature on action
  const delegateKey = getPublicKey(signedAction.delegate_kid);
  if (!verify(delegateKey, signedAction.action_data, signedAction.delegate_signature)) {
    return { valid: false, reason: "Invalid delegate signature" };
  }

  // 2. Verify Principal signature on Delegate's KID
  const principalKey = getPublicKey(signedAction.principal_kid);
  if (!verify(principalKey, signedAction.delegate_kid, signedAction.principal_signature)) {
    return { valid: false, reason: "Invalid principal signature on delegate key" };
  }

  // 3. Verify Issuer signature on Principal's KID
  const issuerKey = getPublicKey(signedAction.issuer_kid);
  if (!verify(issuerKey, signedAction.principal_kid, signedAction.issuer_signature)) {
    return { valid: false, reason: "Invalid issuer signature on principal key" };
  }

  // All three signatures valid!
  return {
    valid: true,
    chain: [
      { level: "issuer", kid: signedAction.issuer_kid },
      { level: "principal", kid: signedAction.principal_kid },
      { level: "delegate", kid: signedAction.delegate_kid }
    ]
  };
}
```

---

## 🌐 OIDC Token Integration

### Current Token (without signatures)
```json
{
  "sub": "user-uuid",
  "agency": {
    "principals": [{ "id": "...", "name": "AetherPro" }],
    "mandates": [{ "scope": "email.read", "valid_until": 123456 }],
    "passports": [{ "id": "did:passport:abc", "capabilities": [...] }]
  }
}
```

### Enhanced Token (with signatures) - TODO
```json
{
  "sub": "user-uuid",
  "agency": {
    "issuer_kid": "realm-key-001",
    "principals": [
      {
        "id": "principal-uuid",
        "name": "AetherPro",
        "principal_kid": "principal-key-001"
      }
    ],
    "mandates": [
      {
        "id": "mandate-uuid",
        "scope": "email.read",
        "principal_signature": "<JWS>",
        "principal_kid": "principal-key-001",
        "valid_until": 1739500800000
      }
    ],
    "passports": [
      {
        "id": "did:passport:abc123",
        "issuer_signature": "<JWS>",
        "issuer_kid": "realm-key-001",
        "delegate_kid": "agent-key-001",
        "capabilities": ["reason", "code"]
      }
    ]
  }
}
```

**Benefit:** Services can verify the signature chain **without calling back to Passport**.

---

## 📊 Example: Full Lifecycle

### 1. Setup (One-time)
```bash
# Admin creates a Principal for "AetherPro Technologies"
POST /admin/realms/master/agency/principals
{
  "name": "AetherPro Technologies",
  "type": "organization",
  "jurisdiction": "US-DE"
}

# System automatically:
# - Generates Ed25519 keypair for Principal
# - Issuer signs Principal's public key
# - Returns: principal_id = "principal-123"
```

### 2. Agent Passport Creation
```bash
# Admin mints an Agent Passport for Claude
POST /admin/realms/master/agency/principals/principal-123/passports
{
  "agent_type": "ai-assistant",
  "capabilities": ["reason", "code", "read_files"]
}

# System automatically:
# - Generates Ed25519 keypair for Agent
# - Builds passport payload
# - Issuer signs the passport
# - Returns: passport_id = "did:passport:claude-001"
```

### 3. Mandate Creation
```bash
# Admin grants Claude permission to read emails
POST /admin/realms/master/agency/delegates/{delegate-id}/mandates
{
  "scope": "email.read",
  "valid_until": "2027-01-01T00:00:00Z"
}

# System automatically:
# - Builds mandate payload
# - Principal signs the mandate
# - Returns: mandate_id = "mandate-456"
```

### 4. Action Execution
```bash
# Claude wants to read an email
# Step 1: Claude signs the action
POST /realms/master/agency/sign-action
{
  "delegate_kid": "agent-claude-key",
  "action_data": "{\"action\":\"email.read\",\"mailbox\":\"inbox\",\"message_id\":\"msg-789\"}"
}

# Response: Full signed action with 3-party chain

# Step 2: Email service verifies the chain
POST /realms/master/agency/verify-chain
{
  "signed_action": "<the signed action from step 1>"
}

# Response:
{
  "valid": true,
  "issuer": { "kid": "realm-key-001", "verified": true },
  "principal": { "kid": "principal-key-001", "verified": true },
  "delegate": { "kid": "agent-claude-key", "verified": true }
}

# ✅ Email service allows Claude to read the email
```

### 5. Audit Trail
```bash
# Every signed action is logged
SELECT * FROM AGENCY_AUDIT_LOG WHERE delegate_kid = 'agent-claude-key';

# Returns:
# - All actions Claude has performed
# - Whether each chain was valid
# - Timestamps of when actions occurred
# - Which mandates were used
```

---

## 🚀 Why This Matters

### For AI Agents
- **Persistent Identity:** Agent has a DID that persists across sessions
- **Cryptographic Proof:** Every action is signed and verifiable
- **Limited Authority:** Mandates restrict what agents can do

### For Principals (Humans/Orgs)
- **Accountability:** Clear trail of who authorized what
- **Revocation:** Can instantly revoke an agent's passport
- **Audit:** Every action is logged and verifiable

### For Services
- **Trust:** Can verify agent authority without calling Passport
- **Compliance:** Cryptographic proof for regulatory requirements
- **Security:** Can't forge signatures without private keys

### For the Ecosystem
- **Federation:** Different issuers can trust each other's signatures
- **Interoperability:** Standard format for agent identity
- **Scalability:** Offline verification via public key infrastructure

---

## 🔒 Security Properties

1. **Non-repudiation:** Agent can't deny performing a signed action
2. **Integrity:** Tampering with action data invalidates signature
3. **Authentication:** Only the real agent has the private key
4. **Authorization:** Mandate proves principal granted permission
5. **Accountability:** Full chain from action → agent → principal → issuer

---

**This is the foundation for the first persistent, cryptographically-verified identity system for AI agents.** 🌟
