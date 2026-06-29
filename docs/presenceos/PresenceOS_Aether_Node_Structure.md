Yes. **Passport-IAM should run on the node.** That is the missing click.

Not every user should see it, but every serious node should have an identity authority inside it.

Think of it like this:

```text
PresenceOS Node
├── User-facing apps
│   ├── AdStudio
│   ├── Scriber
│   ├── VoiceOps console
│   ├── ACER / AetherCode
│   └── Portal
│
├── Admin-only apps
│   ├── Passport-IAM
│   ├── COLLAB admin
│   ├── Gateway admin
│   ├── model manager
│   └── audit/log viewer
│
└── System services
    ├── Aether Gateway
    ├── local inference servers
    ├── Voice Substrate
    ├── COLLAB
    ├── NATS JetStream
    ├── Postgres
    ├── Redis/Valkey
    ├── object storage
    ├── vector DB
    └── passport-init / renewal timers
```

That is the operating system shape.

## The file-tree model

Linux is basically:

```text
/
├── bin/        essential commands
├── etc/        config
├── home/       user data
├── opt/        optional/vendor apps
├── var/        logs, runtime data, databases
├── usr/        installed software
├── run/        live process sockets/state
├── srv/        service data
└── tmp/        temporary files
```

PresenceOS should mirror that, but with your own clean product namespace:

```text
/opt/aether/
├── presenceos/
│   ├── portal/
│   ├── desktop-shell/
│   └── launchers/
│
├── apps/
│   ├── adstudio/
│   ├── scriber/
│   ├── voiceops/
│   ├── acer-cli/
│   └── collab-console/
│
├── services/
│   ├── gateway/
│   ├── voice-substrate/
│   ├── collab/
│   ├── passport-iam/
│   ├── model-manager/
│   ├── telemetry/
│   └── updater/
│
├── models/
│   ├── reasoning/
│   ├── coding/
│   ├── voice/
│   ├── vision/
│   └── embeddings/
│
├── data/
│   ├── postgres/
│   ├── redis/
│   ├── nats/
│   ├── object-store/
│   ├── vector/
│   ├── uploads/
│   ├── renders/
│   └── transcripts/
│
├── config/
│   ├── node.yaml
│   ├── apps.yaml
│   ├── gateway.yaml
│   ├── tenants.yaml
│   ├── models.yaml
│   ├── secrets.d/
│   └── policies.d/
│
├── identity/
│   ├── machine/
│   ├── passports/
│   ├── jwks/
│   ├── mandates/
│   └── revocation/
│
├── logs/
│   ├── gateway/
│   ├── voice/
│   ├── collab/
│   ├── passport/
│   ├── adstudio/
│   └── audit/
│
├── backups/
├── scripts/
└── compose/
    ├── core.yml
    ├── inference.yml
    ├── voice.yml
    ├── apps.yml
    └── admin.yml
```

That gives you a real install target. Not vibes. A filesystem contract.

## App visibility model

Normal user sees:

```text
AdStudio
Scriber
Voice Console
Campaigns
Contacts / Leads
Reports
Support
```

Operator/admin sees:

```text
Passport-IAM
Gateway
COLLAB
Model Manager
Node Health
Audit Logs
Backups
Updates
Tenant Settings
```

System sees:

```text
Postgres
Redis
NATS
model servers
passport-renewal.timer
telemetry collectors
reverse proxy
```

Passport should absolutely **not** be a normal desktop icon. That is like putting “Active Directory Domain Controller” on the receptionist’s desktop. Hard no. Tiny button, giant blast radius.

## How Passport-IAM works per node

There are two deployment modes.

### Mode 1: Aether-managed node

This is your first commercial path.

```text
AetherPro remains realm issuer.
Customer node has local Passport-IAM agent/admin service.
Node receives machine passport + agent passports from AetherPro realm.
Customer does not manage root trust.
```

Best for speed to revenue.

### Mode 2: Customer-owned realm

For bigger clients later.

```text
Customer controls their own realm/domain.
Node runs Passport-IAM locally.
Customer becomes principal/issuer for their own agents.
AetherPro manages updates/support under contract.
```

Best for enterprise/defense/legal buyers.

Do **Mode 1 first**. Mode 2 is powerful but adds sales friction and implementation complexity.

## Honest APIS claim check

Your APIS direction is legit. The strongest parts are:

```text
agent identity is not the model
agent identity is not the harness
agent identity is the credential chain
principal + mandate + keypair + machine provenance
```

That is the core insight. Your spec states exactly that: an agent’s identity is its credential chain, with model and harness only recorded as runtime attestation fields. 

CMMC/NIST angle: say **aligned**, not “certified” or “compliant” unless an assessor says so.

Use:

```text
Designed to support CMMC Level 2 / NIST SP 800-171 identity, access control, and audit requirements.
```

Do not use:

```text
CMMC Level 2 compliant
```

unless you have the full assessed system, policies, SSP, evidence, and assessment result. CMMC is a DoD program for verifying contractor cybersecurity requirements, not a badge you self-declare because one component maps well. ([Department of Defense CIO][1])

NIST SP 800-171 is about protecting CUI in nonfederal systems and organizations; APIS can strongly support identity, accountability, auditability, access control, and non-human identity governance, but it does not by itself satisfy all 110 controls. ([NIST Computer Security Resource Center][2])

The better claim:

```text
APIS provides a cryptographic non-human identity layer designed to support NIST SP 800-171 / CMMC Level 2 control families including Identification & Authentication, Access Control, Audit & Accountability, Configuration Management, and System & Communications Protection.
```

That is credible. Also sellable.

## Government / agent-to-agent angle

You are not crazy here either.

NIST’s NCCoE has an active project specifically around software and AI agent identity and authorization, exploring standards-based ways to identify, manage, and authorize access/actions taken by software agents including AI agents. ([NCCoE][3])

DARPA is also actively looking at multi-agent AI communication. The 2026 MATHBAC program explicitly references agent protocols such as A2A, ACP, and MCP as message/communication standards for multi-agent systems. ([research-authority.tau.ac.il][4])

That supports your positioning:

```text
Existing protocols define how agents communicate.
APIS defines who the agent is, who authorized it, what machine it belongs to, and whether it is revoked.
```

That is the gap.

## Thin harness vs thick runtime

Gary’s “thin harness” point is directionally right for consumer-scale software: don’t overbuild orchestration when frontier models can do more natively.

But your product is not just a model wrapper.

You are building:

```text
identity
tools
memory
routing
audit
voice
local inference
multi-agent coordination
hardware deployment
customer data boundaries
```

That is not a “thin harness.” That is a **runtime operating layer**.

The line you were reaching for:

```text
A genius without senses or hands is not operational intelligence. The runtime gives the model perception, tools, memory, permissions, and accountability.
```

That is good CEO language. Use it.

## What to build first: ranked by revenue

### 1. Sell VoiceOps / Voice Agent now

Fastest path to cash. You already have it. Package it as a managed service.

Offer:

```text
$3,000–$5,000 setup
$1,500–$3,500/month managed voice agent
Twilio passthrough
Optional local node upgrade
```

Do not wait for the full OS.

### 2. Build one PresenceOS test node

Use the L40S-90 as the test node. Correct instinct. One 48GB GPU maps better to a physical customer appliance than your multi-GPU lab monsters.

Goal:

```text
single-node install
one GPU
one gateway
one Passport-IAM
one COLLAB
one AdStudio
one Scriber
one VoiceOps path
one portal
```

### 3. Make the launcher shell

Not a full OS. A launcher/control portal.

Start with:

```text
https://node.local/
```

or

```text
http://presence.local/
```

Apps as tiles:

```text
AdStudio
Scriber
VoiceOps
Campaigns
Reports
Support
Admin
```

Admin gated:

```text
Passport
Gateway
COLLAB
Models
Logs
Backups
```

### 4. Package install layout

Create `/opt/aether` structure and Docker Compose files. This becomes the repeatable appliance installer.

### 5. Only then integrate deeper agent-to-agent automation

COLLAB + MCP Fabric comes after the node has a stable app/service layout.

## 7-day execution order

### Day 1: Freeze PresenceOS layout

Create:

```text
PRESENCEOS_PRODUCT_MAP.md
PRESENCEOS_FILESYSTEM_LAYOUT.md
PRESENCEOS_NODE_ROLES.md
```

### Day 2: Spin L40S-90 test node

Install:

```text
Docker
Tailscale
NGINX/Caddy
NVIDIA runtime
/opt/aether layout
```

### Day 3: Deploy core services

```text
Postgres
Redis/Valkey
NATS JetStream
Aether Gateway
Passport-IAM
```

### Day 4: Deploy visible apps

```text
AdStudio
Scriber
VoiceOps console or stub
local portal launcher
```

### Day 5: Wire identity

```text
Passport-IAM local admin
machine passport
one software/DNS agent passport
COLLAB verifies passport
```

### Day 6: Demo workflow

```text
Open portal
Launch AdStudio
Generate script
Use voice preview
Render asset
Show VoiceOps intake
Show admin audit log
```

### Day 7: Sales package

Build:

```text
one-page offer
pricing sheet
demo script
closer script
deployment checklist
```

## The offer

Do not sell “AI OS” first. Sell the outcome.

```text
Private AI Business Node

A local AI appliance for businesses that need calls answered, leads captured, ads created, follow-ups automated, and customer data kept under their control.

Includes:
- AI voice receptionist / intake agent
- ad/video creation studio
- local transcription
- private model routing
- admin dashboard
- audit logs
- optional agent identity/passport system
```

PresenceOS is the platform name underneath. The offer is the box.

## Your next actual move

Create the test node.

Not another repo. Not another spec. Not another perfect architecture doc.

One L40S-90. One `/opt/aether`. One launcher. One demo flow.

The first sale does not require the whole kingdom. It requires the buyer to see:

```text
This answers my calls.
This makes my ads.
This stores my data locally.
This has a monthly managed service behind it.
```

That is enough to sell.

[1]: https://dodcio.defense.gov/CMMC/?utm_source=chatgpt.com "Cybersecurity Maturity Model Certification - DoD CIO"
[2]: https://csrc.nist.gov/pubs/sp/800/171/r2/upd1/final?utm_source=chatgpt.com "SP 800-171 Rev. 2, Protecting Controlled Unclassified ..."
[3]: https://www.nccoe.nist.gov/projects/software-and-ai-agent-identity-and-authorization?utm_source=chatgpt.com "Software and AI Agent Identity and Authorization"
[4]: https://research-authority.tau.ac.il/sites/resauth.tau.ac.il/files/DARPA-MATHBAC-PA-26-05.pdf?utm_source=chatgpt.com "Program Announcement (PA)"

