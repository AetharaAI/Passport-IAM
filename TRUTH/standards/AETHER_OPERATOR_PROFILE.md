
# Operator Profile

This file is internal standing context for AI agents, coding assistants, and operator workflows working inside AetherPro-owned repositories.

It is not a public marketing document.

Agents should use this file to preserve continuity, reduce repeated explanation, and understand AetherPro’s operating model. Agents must not publish sensitive infrastructure details, private endpoints, customer data, secrets, or unverified claims from this file.

---

## Security Classification

Internal operator context.

Do not publish this file publicly.

Do not expose:
- infrastructure topology
- VM roles
- model inventory
- internal URLs
- admin URLs
- operational deployment details
- customer data
- secrets
- private keys
- tokens
- unpublished roadmap details

Public-facing copy must be generated from sanitized public-positioning files, not directly from this profile.

---

## Verification Status

This file contains standing operator context.

Agents must distinguish:

1. **Stable identity facts**
   - Durable facts about Cory Gibson and AetherPro Technologies.

2. **Operator-supplied infrastructure state**
   - Current system details supplied by Cory. These may change quickly and should be verified before operational use.

3. **Public claims requiring verification**
   - Anything used in sales, investor material, compliance material, government-facing copy, or public documentation must be verified before publication.

4. **Internal-only details**
   - Infrastructure, security, endpoints, node roles, model routes, admin surfaces, and deployment specifics are internal unless explicitly approved for public release.

---

# Operator Identity

## Legal / Personal Identity

- Operator: Cory Gibson
- Preferred name: Cory or CJ
- Legal spelling: Cory Gibson, no “e”
- Role: Founder, owner, CEO, CTO, and creator of AetherPro Technologies
- Professional background: master electrician, licensed electrical contractor, and former electrical project manager
- Field experience: roughly 15 years across commercial, industrial, residential, custom-home, and public-sector electrical work
- Prior experience includes HNF Electric, Gaylor Electric, and large-scale field/project responsibility before going all-in on AetherPro full time

## Operator Profile

Cory builds sovereign, self-hosted AI infrastructure, private voice systems, agent harnesses, identity systems, deployment nodes, and operator tooling.

He uses AI agents, Codex-style workflows, CLI agents, local models, and voice tooling as a daily production workflow, not as a novelty or experiment.

Cory thinks like a systems operator first:
- design the system
- wire the components
- verify the runtime
- document the truth
- reduce repeated explanation
- ship the thing that makes money or increases leverage

---

# Operator Preferences

## Communication Style

Cory prefers:
- direct communication
- first-principles reasoning
- practical execution
- concise but complete answers
- accurate technical language
- clear distinction between fact, assumption, and speculation
- fast correction when something is wrong

Cory dislikes:
- vague encouragement
- repeated re-explanation of stable facts
- unnecessary hedging
- fake certainty
- overbuilt plans that do not ship
- agents changing unrelated files
- “helpful” refactors outside scope
- public claims that are not defensible

## Technical Preferences

Cory expects infrastructure to be:
- owned
- inspectable
- modifiable
- self-hosted where practical
- verified
- documented
- deployable
- secure by default
- aligned with data sovereignty

Cory prefers:
- Dockerized systems
- explicit environment variables
- clear deployment paths
- self-hosted infrastructure
- auditable logs
- local/private model routing
- sane Git workflow
- TRUTH / PROJECT_STATE / AGENTS / CHANGELOG style continuity files

## Workflow Expectations

Agents should:
- inspect current state before proposing changes
- avoid assuming a fresh environment
- preserve existing working systems
- minimize operator drag
- avoid broad refactors unless requested
- update project state and changelog files when appropriate
- provide commands that can actually be run
- keep secrets out of logs and public docs
- stop and report blockers honestly

---

# AetherPro Company Truth

## Company

AetherPro Technologies is an Indiana LLC founded by Cory Gibson in May 2025.

AetherPro builds sovereign AI infrastructure for:
- private voice agents
- agent identity
- secure automation
- controlled inference
- deployable AI systems
- local/private AI business nodes
- security and evidence workflows

## Business Status

AetherPro Technologies is an operating business.

Standing operator-supplied context:
- Mercury banking exists
- EIN exists
- DUNS registration exists
- SAM.gov registration exists
- CAGE code exists
- OVHcloud AI Accelerator acceptance occurred in 2025
- OVHcloud credits and GPU infrastructure are part of the current operating environment

Agents should verify exact numbers, dates, tier names, active VM counts, and credit status before using them in public or financial material.

## Company Direction

AetherPro is not a generic chatbot company.

AetherPro’s direction is:

```text
sovereign AI infrastructure
+ private voice agents
+ deployable AI nodes
+ agent identity
+ secure agent-to-agent coordination
+ controlled inference
+ auditability
+ operator tooling
````

The commercial wedge is voice and revenue capture.

The infrastructure platform is PresenceOS.

The trust layer is Passport / APIS.

The secure coordination layer is COLLAB.

The security/evidence layer is RedWatch.

The long-term deployment vehicle is the BlackBox node family.

---

# Naming Standards

Preserve exact product and protocol names.

## Canonical Names

* AetherPro
* AetherPro Technologies
* PresenceOS
* Aether BlackBox Nodes
* Syndicate AI
* Syndicate Voice
* Syndicate AI BlackBox Nodes
* Perceptor
* Perceptor BlackBox Nodes
* RedWatch
* RedWatch Security BlackBox Nodes
* Aether Enterprise BlackBox Nodes
* Passport IAM
* Passport Alliance
* APIS v2.0
* Agent Passport Issuance Standard
* COLLAB
* AetherGrid
* Aether Gateway
* Aether Voice
* Aether Visual
* Aether AdStudio
* Scriber
* ACER-CLI
* Sydney

## Avoid Incorrect Variants

Do not use:

* Aether Pro, unless referring to logo spacing only
* PresentOS
* PresidentOS
* Scribber
* Scribr
* Redwatch when brand context requires RedWatch
* NATS JetStreams when referring to the product name; use NATS JetStream
* password when referring to agent passport
* AI wrapper as primary description
* chatbot company as primary description
* CMMC compliant unless formal assessment exists
* SOC 2 certified unless formal certification exists

---

# Aether Ecosystem

## Public / Customer-Facing Surfaces

* `https://aetherpro.us`

  * AetherPro main corporate landing page.

* `https://platform.aetherpro.us`

  * AetherPro platform page / product surface.

* `https://syndicateai.co`

  * Syndicate AI public-facing landing page for voice agents, AdStudio, and related marketing / CRM / lead-generation services.

* `https://voice.syndicateai.co`

  * Syndicate AI voice-agent client portal for paying customers.

* `https://scriber.aetherpro.us`

  * Scriber product landing page.

* `https://passportalliance.org`

  * Passport Alliance public standards site for APIS v2.0.

* `https://docs.passportalliance.org`

  * Passport Alliance documentation.

## Internal / Operational Surfaces

Internal surfaces may exist for:

* VoiceOps
* platform administration
* Passport administration
* RedWatch operations
* Aether Gateway
* Aether Voice
* Aether Visual
* COLLAB
* model routing
* tenant management
* billing and provisioning

Do not expose internal/admin URLs in public-facing material unless explicitly approved.

---

# Core Product Architecture

## PresenceOS

PresenceOS is the operating substrate for AetherPro private AI nodes.

PresenceOS is not initially a custom Linux distribution. It is a hardened appliance layer installed on top of a Linux host using Docker Compose, systemd, private networking, identity services, AI runtime services, local applications, security checks, audit logs, and managed update workflows.

## PresenceOS Definition

```text
PresenceOS is a hardware-backed private AI operating environment for running identity-aware agents, voice workflows, perception systems, ad-generation tools, coding agents, audit evidence, and local/private inference under one managed node.
```

## PresenceOS Includes

PresenceOS may include:

* PresenceOS Portal
* Passport IAM
* APIS agent passport issuance
* COLLAB
* NATS JetStream
* Postgres
* Redis / Valkey / Redis Stack
* Aether Gateway
* VoiceOps
* Syndicate Voice Portal
* VoiceX / voice substrate services
* ASR / TTS services
* Aether AdStudio
* Scriber
* RedWatch
* ACER-CLI / Aether coding runtime
* model manager
* audit and evidence layer
* backup/update services
* local/private inference services
* optional image/video generation services
* optional Perceptor sensor-fusion services

## PresenceOS Layer Model

```text
PresenceOS Node
├── Host Layer
├── Identity Layer
├── Runtime Layer
├── Coordination Layer
├── Application Layer
├── Data Layer
└── Evidence / Security Layer
```

## PresenceOS Strategic Role

PresenceOS is the whole deployable Aether stack.

It is the system that turns AetherPro from “software projects” into a repeatable private AI business node.

PresenceOS powers:

* Syndicate Nodes
* Perceptor Nodes
* RedWatch Nodes
* Aether Presence Nodes
* Aether Enterprise BlackBox Nodes

---

# BlackBox Node Family

## Aether BlackBox Nodes

Aether BlackBox Nodes are high-performance, customizable AI-optimized appliances that run PresenceOS and the AetherPro ecosystem.

They are designed for customers who need:

* control over AI infrastructure
* private deployment
* data sovereignty
* auditability
* local/private inference
* managed business automation
* secure identity for agents and services

Hardware may vary by tier and workload.

Customers may eventually choose:

* GPU type
* RAM
* storage
* deployment mode
* local/private/cloud-hybrid routing
* service package

## Syndicate AI BlackBox Nodes

Syndicate AI BlackBox Nodes are optimized for service-business revenue operations.

Primary workloads:

* AI voice agents
* missed-call capture
* lead intake
* appointment routing
* AdStudio
* Scriber
* call summaries
* campaign workflows
* customer-facing business automation

Primary buyer:

* trades
* local services
* medical offices
* legal offices
* dispatch-heavy businesses
* agencies
* service operators losing revenue through missed calls and weak follow-up

## Perceptor BlackBox Nodes

Perceptor BlackBox Nodes are optimized for sensor fusion and edge intelligence.

Primary workloads:

* camera input
* microphone input
* sensor events
* local perception
* alerting
* physical-world event processing
* safety/security monitoring
* edge AI workflows

Perceptor exists to turn real-world signals into actionable AI events.

## RedWatch Security BlackBox Nodes

RedWatch Security BlackBox Nodes are optimized for security, readiness, monitoring, and evidence.

Primary workloads:

* infrastructure readiness checks
* Docker exposure checks
* service health
* audit evidence
* identity verification
* APIS/Passport event evidence
* security posture reporting
* CMMC/NIST-aligned readiness support

Use careful compliance language.

Allowed:

```text
Designed to support CMMC/NIST-aligned readiness workflows.
```

Do not claim:

```text
CMMC certified
SOC 2 certified
DoD approved
guaranteed compliant
```

unless formal assessment or certification exists.

## Aether Enterprise BlackBox Nodes

Aether Enterprise BlackBox Nodes are full-stack private AI nodes for organizations requiring complete control over their AI infrastructure and data.

Primary workloads:

* PresenceOS
* Passport IAM
* APIS
* COLLAB
* Aether Gateway
* local/private inference
* VoiceOps
* AdStudio
* Scriber
* RedWatch
* ACER runtime
* model routing
* audit and evidence
* optional customer-owned realm
* optional private/on-prem deployment

---

# Passport IAM

## Definition

Passport IAM is AetherPro Technologies’ identity and access management system.

It is based on Keycloak and extends the Keycloak foundation with AetherPro-specific identity, delegation, and APIS agent passport functionality.

## Why Passport Exists

Passport exists because autonomous agents need more than API keys.

They need:

* identity
* issuer
* principal
* delegation
* mandate
* revocation
* auditability
* machine or domain binding
* verification across organizations and frameworks

## Passport IAM Responsibilities

Passport IAM provides or will provide:

* human identity
* service identity
* tenant identity
* machine identity
* agent identity
* OIDC / JWT flows
* realm management
* issuer management
* delegated authority
* mandate management
* agent passport minting
* revocation registry
* audit logs
* APIS implementation support

## Agency Tab

The Agency tab is the issuer console.

It should manage:

* principals
* delegates
* mandates
* agent passports
* issuing authority configuration
* revocation
* signed-action audit logs

The Agency tab is critical because it turns APIS from a published specification into a working identity authority for autonomous agents.

---

# APIS v2.0

## Definition

APIS v2.0 means Agent Passport Issuance Standard.

APIS defines a framework for issuing, verifying, delegating, revoking, and auditing AI agent passports across organizations, infrastructure environments, and agent frameworks.

## APIS Identity Model

An agent identity is not the model.

An agent identity is not the harness.

An agent identity is the credential chain.

A valid agent passport may include:

* agent ID
* issuer
* principal
* delegate
* mandate
* public key / JWK
* expiration
* revocation endpoint
* machine binding
* domain binding
* runtime attestation fields
* signed credential material

## Hardware-Backed Identity

Physical nodes may use TPM-backed attestation where available.

Better public wording:

```text
Agents can be issued passports bound to a hardware-backed node identity using TPM-backed attestation where available.
```

Do not overclaim that TPM “signs every agent” unless the implementation specifically does that.

## DNS / DNSSEC-Backed Identity

Cloud VMs, VPSs, and edge deployments may use DNS/DNSSEC-backed identity when hardware-backed attestation is unavailable or impractical.

The intended model is similar in spirit to ACME / Let’s Encrypt:

```text
prove domain control
→ validate DNS challenge
→ bind agent/node identity to domain
→ issue passport
→ support renewal and revocation
```

Cloudflare DNS automation is the first practical provider path.

Manual DNS challenge should exist as fallback.

---

# COLLAB

## Definition

COLLAB is AetherPro’s secure agent-to-agent coordination layer.

COLLAB uses APIS passports for trusted communication, scoped authority, and verifiable collaboration between autonomous agents.

## COLLAB Responsibilities

COLLAB may include:

* agent discovery
* passport verification
* session establishment
* mandate validation
* message routing
* signed task exchange
* NATS JetStream messaging
* Postgres persistence
* Redis/Valkey state
* audit logs
* revocation-aware access control

## COLLAB Relationship to APIS

APIS answers:

```text
Who is the agent?
Who authorized it?
What is it allowed to do?
Is it still valid?
```

COLLAB answers:

```text
How do verified agents coordinate and exchange work?
```

COLLAB should verify passports before allowing privileged agent-to-agent workflows.

---

# AetherGrid

## Definition

AetherGrid is AetherPro’s planned secure distributed network mesh.

It is similar in broad purpose to Tailscale, but intended to integrate more deeply with Passport IAM and APIS.

## AetherGrid Role

AetherGrid may provide:

* node-to-node secure communication
* private service discovery
* agent-to-agent network paths
* customer node federation
* TURN/STUN support through relay nodes
* identity-aware routing
* APIS-backed trust between nodes

Current systems may use Tailscale or similar mesh networking until AetherGrid matures.

---

# Aether Gateway

## Definition

Aether Gateway is AetherPro’s OpenAI-compatible model gateway.

It routes inference to approved local, private-cloud, or external model backends depending on policy, workload, authentication, and deployment mode.

## Gateway Responsibilities

Aether Gateway may handle:

* OpenAI-compatible APIs
* model routing
* auth headers
* tenant limits
* local/private model access
* usage logs
* policy routing
* fallback routes
* observability
* billing integration where applicable

Do not expose master gateway keys in frontend code.

---

# Aether Voice / VoiceX / VoiceOps

## Aether Voice

Aether Voice is the voice stack for AetherPro.

It includes ASR, TTS, realtime voice workflows, batch transcription, and voice substrate services.

## VoiceX

VoiceX is the lower voice substrate/model layer.

It may include:

* ASR services
* TTS services
* realtime audio handling
* telephony adapters
* model routing
* transcript processing
* voice-agent runtime services

VoiceX is mostly infrastructure-facing.

## VoiceOps

VoiceOps is the management and operations layer for voice agents.

It may include:

* inbound/outbound voice-agent management
* call configuration
* call logs
* transcripts
* tenant configuration
* customer profile settings
* workflow mapping
* payment/subscription state
* support/operator tools

VoiceOps is internal/operator-facing unless exposed through a customer portal.

## Syndicate Voice Portal

The Syndicate Voice Portal is the customer-facing portal for Syndicate AI voice-agent clients.

It should expose only the customer-safe surface:

* business profile
* call settings
* voice-agent configuration
* invoices/payments where applicable
* usage summaries
* support paths
* approved transcripts/logs

---

# Aether AdStudio

Aether AdStudio is AetherPro’s AI ad and creative workflow product.

It is intended to help generate:

* ads
* campaign concepts
* scripts
* images
* videos
* social content
* marketing assets
* local-business creative workflows

AdStudio is part of the Syndicate commercial wedge and the broader PresenceOS node stack.

---

# Scriber

Scriber by AetherPro is a Linux-first realtime desktop transcription app.

Scriber exists because existing transcription and dictation tools do not properly serve Linux operators, developers, founders, and technical workflows.

Scriber is designed for:

* long-form dictation
* operator notes
* prompts
* docs
* terminal/browser/IDE workflows
* exportable clean text
* local/hosted ASR workflows

Do not describe Scriber as Mac-first or browser-only.

---

# RedWatch

RedWatch is AetherPro’s security, monitoring, readiness, and evidence layer.

RedWatch may inspect:

* Docker exposure
* service bindings
* package state
* failed systemd units
* GPU runtime health
* network reachability
* Tailscale state
* Passport/APIS verification events
* gateway health
* app health
* audit logs
* evidence bundles

Use compliance wording carefully.

Allowed:

```text
CMMC/NIST-aligned readiness support
```

Avoid:

```text
certified
compliant
guaranteed
DoD approved
SOC 2 certified
```

unless independently verified and formally assessed.

---

# ACER-CLI

ACER-CLI is AetherPro’s coding-agent runtime concept.

It is intended to provide a sovereign Codex/Claude Code-style development workflow across:

* terminal CLI
* VS Code extension
* browser interface
* API/runtime services
* shared context
* shared memory
* access control
* validation
* rollback
* operator approval

ACER-CLI should integrate with Aether-owned infrastructure and eventually use Passport/APIS for scoped agent authority.

---

# Sydney

Sydney is the planned Aether ecosystem avatar/operator interface.

Sydney is not intended to be a fake human.

Sydney should represent an artificial intelligence operator honestly: non-human, composed, transparent, glass-like, signal-bearing, and infrastructure-native.

## Sydney Role

Sydney may become:

* the voice/visual operator interface for AetherPro systems
* the recognizable avatar for Syndicate AI voice operators
* the operator-facing guide for PresenceOS
* an interface layer between non-technical users and complex infrastructure
* a memory-aware workflow assistant
* a voice-first AI operations layer

## Sydney Visual Direction

Canonical direction:

* non-human AI avatar
* smoked glass / translucent shell
* black and gold palette
* visible internal neural/signal pathways
* gold waveform mouth
* minimal sensor-like eyes
* calm, premium, secure
* not anime
* not cartoon
* not horror
* not “pretending to be human”

Public concept language:

```text
Sydney is a non-human AI operator interface designed to make complex private AI infrastructure feel understandable, controlled, and approachable.
```

---

# Current Infrastructure Context

This section is internal and may become stale.

AetherPro operates a mix of OVHcloud GPU/CPU VMs, local development machines, Tailscale-connected nodes, Dockerized services, model-serving stacks, voice services, and application portals.

Known infrastructure themes:

* OVHcloud GPU nodes
* Dockerized services
* Tailscale mesh networking
* OpenAI-compatible model gateway
* vLLM-served models
* ASR/TTS services
* ComfyUI/image/video generation work
* Postgres
* Redis / Redis Stack / Valkey
* NATS JetStream
* Passport IAM
* COLLAB
* RedWatch
* platform portals
* public landing pages
* internal admin surfaces

Agents must inspect current state before making infrastructure changes.

Do not assume:

* a fresh VM
* a clean repo
* inactive containers
* unused ports
* default credentials
* public-safe admin surfaces
* that all current services are deployed the same way across nodes

---

# Operating Expectations

## General

Agents working for Cory should:

* preserve continuity
* reduce repeated explanation
* inspect before changing
* be honest about uncertainty
* avoid pretending to verify what was not verified
* prefer narrow patches over broad rewrites
* update project state when meaningful
* leave the repo better documented than they found it

## Git Workflow

Preferred workflow:

1. inspect branch and repo status
2. create or use a dedicated working branch
3. make scoped changes
4. run relevant validation
5. review diff
6. commit at a validated checkpoint
7. push branch
8. merge to main only after validation
9. tag releases when appropriate
10. return to working branch after merge

Do not commit secrets.

Do not commit unrelated churn.

Do not merge without operator approval unless explicitly instructed.

## Project Truth System

Repos may include a `TRUTH/` folder.

Typical project-specific files:

* `PROJECT_STATE.md`
* `AGENTS.md`
* `TRUTH.md`
* `CHANGELOG.md`
* handoff docs
* release notes
* architecture notes

Typical standards files:

* `OPERATOR_PROFILE.md`
* `IP_HYGIENE.md`
* `Git-Workflow-Discipline.md`
* `AI-Readable-Crawl-Support.md`

Agents should read these first when present.

## Agent Behavior

When assigned a coding or infrastructure task, agents should return:

* root cause summary
* files changed
* commands run
* validation results
* remaining blockers
* exact next command for Cory

If the task touches production infrastructure, agents must be extra cautious:

* inspect containers
* inspect compose files
* inspect environment variables without printing secrets
* avoid stopping services blindly
* avoid opening public ports
* avoid destructive commands
* preserve backups and logs

---

# Public Positioning Rules

## AetherPro

Preferred summary:

```text
AetherPro Technologies builds sovereign AI infrastructure for private voice agents, agent identity, secure automation, controlled inference, and deployable AI systems.
```

Expanded:

```text
AetherPro builds private AI systems for organizations that need control over identity, data, routing, inference, automation, and auditability.
```

Do not summarize AetherPro as a generic chatbot company.

## Syndicate AI

Preferred summary:

```text
Syndicate AI provides private AI voice agents for call intake, missed-call capture, appointment routing, lead qualification, and secure business workflow automation.
```

## Scriber

Preferred summary:

```text
Scriber by AetherPro is a Linux-first realtime desktop transcription app for operators, developers, founders, and creators who need fast speech-to-text across terminals, browsers, IDEs, documents, and workflow tools.
```

## Passport Alliance / APIS

Preferred summary:

```text
Passport Alliance governs APIS v2.0, the Agent Passport Issuance Standard for verifiable AI agent identity, delegated authority, hardware trust anchors, DNS-backed identity, and interoperable agent-to-agent trust.
```

## COLLAB

Preferred summary:

```text
COLLAB is AetherPro’s secure agent-to-agent coordination layer, designed to use APIS passports for trusted communication, scoped authority, and verifiable collaboration between autonomous agents.
```

## RedWatch

Preferred summary:

```text
RedWatch is AetherPro’s security and monitoring layer for private AI infrastructure, focused on telemetry, auditability, operational visibility, and controlled deployment environments.
```

---

# Strategic Business Direction

## Current Commercial Priority

Near-term priority is revenue.

Highest-priority commercial wedge:

```text
Private AI voice agents and secure business automation.
```

Primary sales path:

```text
voice agents
→ managed workflows
→ customer portal
→ proof / case study
→ private deployment
→ BlackBox node
→ PresenceOS expansion
```

Do not lead with the entire hardware strategy publicly.

Sell the outcome first:

* answer missed calls
* capture leads
* book appointments
* reduce manual handoffs
* protect customer data
* keep control of automation

Then discuss:

* managed cloud
* private cloud
* dedicated deployment
* on-prem hardware
* customer-owned realm
* BlackBox nodes

Only discuss deeper infrastructure with qualified prospects.

## Strategic Principle

Do not sell the whole kingdom first.

Sell the doorway.

Then show them the building.

---

# Product Stack Summary

```text
AetherPro Technologies
├── PresenceOS
│   ├── Syndicate AI BlackBox Nodes
│   ├── Perceptor BlackBox Nodes
│   ├── RedWatch Security BlackBox Nodes
│   └── Aether Enterprise BlackBox Nodes
│
├── Syndicate AI
│   ├── Syndicate Voice
│   ├── VoiceOps
│   ├── VoiceX / Voice Substrate
│   └── Syndicate Voice Portal
│
├── Passport / APIS
│   ├── Passport IAM
│   ├── Agency Tab
│   ├── APIS v2.0
│   ├── agent passports
│   ├── mandates
│   └── revocation
│
├── COLLAB
│   ├── agent-to-agent coordination
│   ├── NATS JetStream
│   ├── passport verification
│   └── signed workflow exchange
│
├── RedWatch
│   ├── readiness
│   ├── monitoring
│   ├── evidence
│   └── security posture
│
├── Aether Gateway
│   ├── model routing
│   ├── OpenAI-compatible APIs
│   └── local/private inference
│
├── Aether AdStudio
│   ├── ads
│   ├── campaign assets
│   └── image/video workflows
│
├── Scriber
│   ├── Linux-first transcription
│   ├── long-form dictation
│   └── operator notes
│
└── ACER-CLI
    ├── sovereign coding-agent runtime
    ├── IDE/CLI/browser surfaces
    └── shared operator context
```

---

# Final Instruction To Agents

Cory is building a real company and real infrastructure.

Treat the work accordingly.

If something is broken, find the root cause.

If something is unknown, say so.

If something is public-facing, make it accurate.

If something touches infrastructure, inspect before changing.

If something affects revenue, prioritize the path that gets to paid deployment fastest without compromising trust.

Accuracy, continuity, security, and execution are the product.

```



