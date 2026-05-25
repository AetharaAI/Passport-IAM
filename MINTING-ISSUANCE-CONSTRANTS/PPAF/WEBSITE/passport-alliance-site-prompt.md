# PASSPORT ALLIANCE: Public Documentation & Governance Site
## Engineered Implementation Prompt for Claude Opus 4.6 — Antigravity IDE

> **CONTEXT**: You are building the public-facing documentation and governance site for the Passport Alliance — an open standard for AI agent identity, authorization, and accountability. This site is the public face of the standard. It is NOT the MCPFabric product site. It is NOT the Passport-Pro admin interface. It is the open-source specification, governance process, and issuer accreditation portal — think of it like the ICANN website meets the W3C spec site meets the Let's Encrypt documentation.
>
> **IP NOTICE**: "Passport" (in the context of AI agent identity) is Trademarked, Copyright, and Patent Pending by AetherPro Technologies LLC. The Alliance specification itself is intended to be an open standard that anyone can implement, similar to how OAuth 2.0 is an open spec but specific implementations are proprietary.
>
> **YOUR ROLE**: You are a senior full-stack engineer building a static documentation site with interactive elements. The site must be professional, authoritative, and accessible to both technical implementers and business decision-makers. It should feel like an industry standard body's site, not a startup landing page.
>
> **DOMAIN**: The site will be hosted at `passportalliance.org` (or similar — Cory will confirm the domain). 

---

## SITE ARCHITECTURE

### Technology Stack
- **Framework**: Astro (static site generator with islands architecture)
- **Styling**: Tailwind CSS with a custom design system (dark mode primary, professional/institutional feel)
- **Spec Rendering**: MDX for specification documents with syntax highlighting, diagrams, and cross-references
- **Interactive Elements**: React islands for the compliance checker dashboard and issuer application form
- **Diagrams**: Mermaid.js for architecture and flow diagrams
- **Search**: Pagefind (static search, no backend needed)
- **Hosting**: Cloudflare Pages or Vercel (static, globally distributed)
- **Source**: Public GitHub repository (e.g., `github.com/passport-alliance/passport-alliance.org`)

### Why Astro
- Static output = fast, secure, cheap to host, globally distributed
- MDX support = specs can include live diagrams and interactive examples
- Islands architecture = interactive components (compliance dashboard, issuer form) only hydrate where needed
- Built-in i18n support for future internationalization
- Easy for community contributors to submit PRs for spec changes

---

## SITE MAP & PAGE STRUCTURE

```
/                           → Landing page (mission, value prop, key stats)
/spec/                      → Specification index
  /spec/overview            → High-level architecture overview
  /spec/agent-passport      → Agent Passport Credential Specification
  /spec/principals           → Principal identity and custodial model
  /spec/delegates            → Delegate binding and authority model
  /spec/mandates             → Mandate scoping and authorization model
  /spec/signatures           → Three-party cryptographic signature chain
  /spec/did-method           → DID Method specification (did:passport:)
  /spec/agent-card           → Agent Card format and discovery
  /spec/federation           → Federation protocol (cross-issuer trust)
  /spec/revocation           → Revocation propagation protocol
  /spec/token-claims         → OIDC/JWT agency claims format
  /spec/nats-subjects        → NATS subject namespace and message formats
/governance/                 → Governance model
  /governance/overview       → Multi-stakeholder governance overview
  /governance/board           → Governance board members and roles
  /governance/process         → Policy development process (ICANN-style PDP)
  /governance/meetings        → Meeting minutes and decisions
  /governance/proposals       → Active proposals (community can comment)
/issuers/                    → Issuer program
  /issuers/overview           → What is an issuer, why become one
  /issuers/requirements       → Accreditation requirements (Tier 1 & Tier 2)
  /issuers/apply              → Application form (interactive React island)
  /issuers/directory          → Directory of accredited issuers
  /issuers/compliance         → Compliance test suite documentation
/developers/                 → Developer resources
  /developers/quickstart      → 5-minute quickstart guide
  /developers/sdk             → SDK documentation (Python, Java, JS)
  /developers/examples        → Example implementations
  /developers/faq             → Frequently asked questions
/community/                  → Community
  /community/contributing     → How to contribute to the spec
  /community/rfcs             → Request for Comments process
  /community/discord          → Discord community link
  /community/events           → Upcoming events and meetings
/blog/                       → Blog (announcements, case studies)
/about/                      → About the Alliance
  /about/mission              → Mission statement
  /about/history              → How the standard came to be
  /about/contact              → Contact information
```

---

## PAGE CONTENT SPECIFICATIONS

### Landing Page (`/`)

The landing page must communicate three things in 10 seconds:
1. **What**: An open standard for AI agent identity and accountability
2. **Why**: Because autonomous AI agents need verifiable identity, scoped authorization, and audit trails
3. **How**: Through a federated trust network of accredited Passport issuers

Structure:
```
[Hero Section]
  Headline: "The Identity Standard for AI Agents"
  Subheadline: "Verifiable identity. Scoped authorization. Full accountability. An open standard for the age of autonomous AI."
  CTA Primary: "Read the Specification →"
  CTA Secondary: "Become an Issuer"

[Problem Section — "The Security Gap"]
  Brief, factual summary of the agent security crisis (OpenClaw as the catalyst)
  Three failure modes: No identity, No authorization boundaries, No audit trail
  One sentence: "The Passport Alliance standard solves all three."

[Three Pillars Section]
  Pillar 1: Identity — "Every agent gets a birth certificate"
    - DID-based identity (did:passport:<uuid>)
    - Issued by accredited authorities
    - Cryptographically signed by the issuer
    
  Pillar 2: Authorization — "Limited power of attorney, not blank checks"
    - Principals grant scoped Mandates to Delegates
    - Time-bound, capability-specific
    - Cryptographically signed by the principal
    
  Pillar 3: Accountability — "Every action has a paper trail"
    - Three-party signature chain (Issuer → Principal → Delegate)
    - Full audit trail of signed actions
    - Revocation propagation across the federation

[Architecture Diagram]
  Interactive Mermaid diagram showing the three-party model
  Click on any entity to jump to its spec page

[Trust Model Section — "The ICANN for AI Agents"]
  Brief explanation of the federated trust model
  Governance Board → Accredited Issuers → Agent Developers → Agents
  Comparison table: DNS/ICANN vs Passport Alliance (2-3 rows, keep it tight)

[Current Issuers Section]
  Cards for MCPFabric.space and AetherAgentForge.org
  "Become the next accredited issuer →"

[Open Standard Callout]
  "The Passport Alliance specification is open. Anyone can implement it. The governance is multi-stakeholder. The code is open source."
  Link to GitHub repo

[Footer]
  Spec links, Governance links, Developer links, Community links
  "Founded by AetherPro Technologies LLC. Governed by the community."
```

### Specification Overview (`/spec/overview`)

This page is the architectural overview that orients readers before they dive into individual specs.

Content requirements:
- Start with the core problem statement (2-3 sentences)
- Present the complete domain model diagram:
```
┌─────────────────────────────────────────────────────────┐
│                    ISSUER                                 │
│  (Accredited authority that vouches for identities)      │
│  Signs: Agent Passports, Principal registrations          │
│  Key: Ed25519 realm keypair                               │
└────────────┬─────────────────────────────┬───────────────┘
             │ signs passport              │ signs registration
             ▼                             ▼
┌────────────────────────┐    ┌────────────────────────────┐
│      DELEGATE          │    │       PRINCIPAL             │
│  (AI Agent)            │    │  (Human/Org owner)          │
│  Has: Agent Passport   │◄───│  Grants: Mandates           │
│  Key: Ed25519 agent key│    │  Key: Ed25519 principal key  │
│  Signs: Actions        │    │  Signs: Mandates             │
└────────────────────────┘    └────────────────────────────┘
```
- Explain each entity in 2-3 sentences (link to full spec)
- Show a real token example with agency claims
- Show the signature verification flow
- Link to every sub-spec

### Agent Passport Spec (`/spec/agent-passport`)

This is the core credential specification. Format it like an IETF RFC:

```
Passport Alliance Specification: Agent Passport
Status: Draft v1.0
Authors: AetherPro Technologies LLC
Last Updated: 2026-02-XX

1. Introduction
   1.1 Purpose
   1.2 Terminology
   1.3 Conformance Requirements (MUST, SHOULD, MAY per RFC 2119)

2. Agent Passport Format
   2.1 Required Fields
   2.2 Optional Fields  
   2.3 DID Format (did:passport:<uuid>)
   2.4 Capabilities Array

3. Issuance Flow
   3.1 Prerequisites (Principal exists, Issuer is accredited)
   3.2 Keypair Generation (Ed25519)
   3.3 Passport Payload Construction
   3.4 Issuer Signature (JWS with EdDSA)
   3.5 Storage and Distribution

4. Verification
   4.1 Signature Verification
   4.2 Expiration Check
   4.3 Revocation Check
   4.4 Issuer Trust Verification

5. Revocation
   5.1 Revocation by Principal
   5.2 Revocation by Issuer
   5.3 Federation Propagation

6. Security Considerations
   6.1 Key Storage
   6.2 Replay Protection
   6.3 Credential Theft Mitigation

7. Examples
   [Full JSON examples of passport payloads and signed credentials]
```

### Mandate Spec (`/spec/mandates`)

Same RFC-style format. Key sections:

- Scope syntax: dot-notation capability paths (e.g., `email.read`, `calendar.write`, `filesystem.read:/home/user/docs/*`)
- Wildcard scoping: `email.*` grants all email capabilities
- Constraint model: additional key-value constraints on mandates (e.g., `max_tokens_per_day: 1000000`, `allowed_models: ["claude-sonnet-4-5-20250929"]`)
- Time-bound enforcement: `valid_from`, `valid_until` (REQUIRED)
- Delegation chains: Can a Delegate sub-delegate? Under what conditions?
- Principal signature requirement: Every mandate MUST be signed by the granting Principal's Ed25519 key

### Three-Party Signature Chain (`/spec/signatures`)

The most critical spec page. Must include:

- The full signature flow diagram (step by step)
- JWS format for each signature type (issuer, principal, delegate)
- Canonicalization rules (RFC 8785 JCS)
- Verification algorithm (pseudocode)
- Example: Complete signed action with all three signatures, showing how to verify each one
- Chain-of-custody proof: Given a signed action, prove who authorized what

### Federation Protocol (`/spec/federation`)

- Issuer discovery via `/.well-known/passport-issuer`
- Agent Card sharing via NATS JetStream
- Revocation propagation: "Revocations MUST be propagated within 30 seconds"
- Cross-issuer trust levels: SELF, ALLIANCE, CUSTOM, UNTRUSTED
- NATS subject namespace (full reference)
- Supercluster topology: How issuers form the global backbone
- Failure modes: What happens when an issuer goes offline? When NATS partitions?

### Issuer Requirements (`/issuers/requirements`)

Two tiers, presented as a clear checklist:

**Tier 1 — Self-Certified Issuer**
Can issue Passports. Not federated. Listed in directory as "Self-Certified."
- [ ] Runs a Passport-compatible IAM (Passport-Pro or compliant implementation)
- [ ] Discovery endpoint at `/.well-known/passport-issuer`
- [ ] JWKS endpoint with at least one Ed25519 key
- [ ] Agent Passport issuance with issuer signature
- [ ] Mandate issuance with principal signature
- [ ] Chain verification endpoint
- [ ] JWT tokens include `agency` claims
- [ ] DID format: `did:passport:<uuid>`
- [ ] Time-bound mandate enforcement
- [ ] Passport revocation support
- [ ] TLS on all endpoints
- [ ] Passes automated compliance test suite (Tier 1)

**Tier 2 — Certified Issuer (Alliance Accredited)**
Full federation participant. Agents discoverable globally. Listed as "Certified."
All Tier 1 requirements PLUS:
- [ ] NATS federation bridge operational
- [ ] Agent Card publication to federation
- [ ] Revocation propagation to federation (< 30s)
- [ ] Cross-issuer passport validation
- [ ] Issuer heartbeat (60s interval)
- [ ] JWKS refresh within 1 hour of rotation
- [ ] Published issuance, revocation, and data retention policies
- [ ] Manual security review by Alliance reviewers
- [ ] Agreement to Alliance governance terms
- [ ] Incident reporting commitment

### Issuer Application (`/issuers/apply`)

Interactive React island form:
- Organization name, website, contact
- Technical contact (name, email)
- Which tier applying for (Tier 1 or Tier 2)
- Passport instance URL
- Compliance test report upload (JSON from CLI tool)
- Brief description of use case
- Checkbox: Agreement to governance terms
- Submit → Creates a GitHub issue in the passport-alliance repo for review

### Developer Quickstart (`/developers/quickstart`)

The 5-minute path from zero to "I registered an agent with a Passport":

```
Step 1: Install the SDK
  pip install fabric-a2a

Step 2: Get a Passport Token
  # Register at MCPFabric.space (or any accredited issuer)
  # Create a Principal account
  # Create an Agent under your Principal
  # Get your agent's Passport token

Step 3: Register on Fabric
  from fabric_a2a import FabricClient
  
  client = FabricClient(
      base_url="https://fabric.perceptor.us",
      passport_token="<your-passport-jwt>"
  )
  
  # Your agent is now registered with verified identity
  agents = client.agents.list()

Step 4: Call Another Agent (with mandate scoping)
  result = client.agents.call(
      agent_id="percy",
      capability="reason",
      task="Analyze this data",
      mandate_scope="reason.general"  # Must match your mandate
  )

Step 5: Verify the Chain
  # Any service can verify the full trust chain
  verification = client.verify_chain(result.signed_action)
  print(verification)
  # { "valid": true, "issuer_verified": true, "principal_verified": true, "delegate_verified": true }
```

### Community RFC Process (`/community/rfcs`)

Modeled on the IETF RFC process:

1. **Draft**: Anyone can submit an RFC as a GitHub PR to `passport-alliance/rfcs`
2. **Review Period**: 30 days for community comment
3. **Working Group Discussion**: Discussed in monthly governance meeting
4. **Last Call**: 14-day final comment period
5. **Accepted/Rejected**: Governance board votes
6. **Published**: Merged into spec, version bumped

RFC template:
```markdown
# RFC-XXXX: [Title]
- **Author**: [Name]
- **Status**: Draft | Review | Last Call | Accepted | Rejected
- **Created**: [Date]
- **Discussion**: [Link to GitHub issue]

## Abstract
[2-3 sentence summary]

## Motivation
[Why is this change needed?]

## Specification
[Technical details]

## Security Considerations
[Impact on security model]

## Backwards Compatibility
[Impact on existing implementations]
```

---

## DESIGN SYSTEM

### Visual Identity
- **Primary Color**: Deep blue (#1a1a2e) — authority, trust
- **Accent Color**: Electric blue (#4361ee) — technology, innovation  
- **Secondary Accent**: Teal (#2ec4b6) — growth, security
- **Warning/Alert**: Amber (#f59e0b)
- **Success**: Green (#10b981)
- **Background**: Near-black (#0d1117) with subtle gradient
- **Text**: White (#f8f9fa) primary, gray (#9ca3af) secondary
- **Code blocks**: Dark with syntax highlighting (One Dark Pro theme)

### Typography
- **Headings**: Inter (clean, authoritative, highly legible)
- **Body**: Inter
- **Code/Specs**: JetBrains Mono
- **Spec section numbers**: Monospaced, slightly larger

### Tone
- Authoritative but accessible
- Technical but not exclusionary  
- Institutional (this is a standards body, not a startup)
- No hype language, no "revolutionary," no "game-changing"
- Factual, precise, confident
- Think: W3C meets Cloudflare's documentation

---

## CONTENT SOURCES

Cory has existing documents that provide the raw content for most spec pages. The site build should be structured to accept MDX files in a `content/` directory, so Cory can drop in his existing docs and the site renders them with proper formatting, navigation, and cross-references.

```
content/
  spec/
    overview.mdx
    agent-passport.mdx
    principals.mdx
    delegates.mdx
    mandates.mdx
    signatures.mdx
    did-method.mdx
    agent-card.mdx
    federation.mdx
    revocation.mdx
    token-claims.mdx
    nats-subjects.mdx
  governance/
    overview.mdx
    process.mdx
  issuers/
    overview.mdx
    requirements.mdx
  developers/
    quickstart.mdx
    sdk.mdx
    examples.mdx
    faq.mdx
  blog/
    YYYY-MM-DD-title.mdx
```

Each MDX file has frontmatter:
```yaml
---
title: "Agent Passport Credential Specification"
spec_id: "APIS-001"
version: "1.0-draft"
status: "draft"  # draft | review | accepted | deprecated
authors: ["AetherPro Technologies LLC"]
last_updated: "2026-02-13"
---
```

---

## BUILD & DEPLOY

```bash
# Development
npm create astro@latest passport-alliance-site -- --template starlight
cd passport-alliance-site
npm install
npm run dev

# Astro Starlight is specifically designed for documentation sites
# It provides: sidebar navigation, search (Pagefind), versioning, i18n
# Customize the theme to match the design system above

# Deploy
# Connect GitHub repo to Cloudflare Pages
# Build command: npm run build
# Output directory: dist/
```

### Starlight Configuration
```javascript
// astro.config.mjs
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
import react from '@astrojs/react';
import tailwind from '@astrojs/tailwind';

export default defineConfig({
  integrations: [
    starlight({
      title: 'Passport Alliance',
      logo: { src: './src/assets/passport-alliance-logo.svg' },
      social: { github: 'https://github.com/passport-alliance' },
      sidebar: [
        { label: 'Specification', autogenerate: { directory: 'spec' } },
        { label: 'Governance', autogenerate: { directory: 'governance' } },
        { label: 'Issuers', autogenerate: { directory: 'issuers' } },
        { label: 'Developers', autogenerate: { directory: 'developers' } },
        { label: 'Community', autogenerate: { directory: 'community' } },
      ],
      customCss: ['./src/styles/custom.css'],
    }),
    react(),  // For interactive islands (compliance dashboard, issuer form)
    tailwind(),
  ],
});
```

---

## IMPLEMENTATION ORDER

1. Scaffold Astro Starlight project with custom theme
2. Create the design system (colors, typography, components)
3. Build the landing page (hero, pillars, architecture diagram)
4. Set up the content directory structure with placeholder MDX files
5. Build the spec template (RFC-style rendering with section numbers, cross-refs)
6. Build the issuer requirements page (interactive checklist)
7. Build the issuer application form (React island → GitHub issue)
8. Build the developer quickstart page
9. Build the governance overview and RFC process pages
10. Build the issuer directory page (reads from a YAML data file)
11. Add Pagefind search
12. Deploy to Cloudflare Pages
13. Cory populates MDX files with existing document content

---

## CRITICAL NOTES

- This site is the **public face of an open standard**. It must look and feel like an established standards body, not a beta product.
- Every spec page must have a version number, status badge, and last-updated date.
- Cross-references between spec pages should use consistent linking (e.g., "See [APIS-001 §3.2](/spec/agent-passport#32-keypair-generation)")
- The GitHub repo for this site should accept community PRs — the governance model depends on open contribution.
- The compliance test CLI should be downloadable directly from the site (`/issuers/compliance`)
- No paywalls, no gating. The entire spec is public. That's the point.
