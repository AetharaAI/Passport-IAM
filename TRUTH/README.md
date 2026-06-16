
# TRUTH Documentation System

This folder provides project continuity for AI agents, coding assistants, and operator workflows.

The TRUTH system exists to prevent drift between:

- what is deployed
- what operators believe is deployed
- what agents assume is deployed
- what public/product documentation claims

Use this system in every serious AetherPro repo.

---

## Folder Structure

```text
TRUTH/
├── README.md
├── templates/
└── standards/
````

---

## Repo Root Pointer

Agents and tools (Codex, Claude Code, etc.) auto-discover an `AGENTS.md` (and sometimes `CLAUDE.md`) at the **repository root**. To bridge that convention into this system, the repo root keeps a thin `AGENTS.md` whose only job is to point here.

- Repo-root `AGENTS.md` = pointer only. It must not hold canonical content.
- All canonical project truth lives in `TRUTH/` (this folder).
- If you land at the repo root, follow the pointer here, then follow the Agent Startup Rule below.

## Agent Startup Rule

Before making changes, agents must read this file first.

Then read:

```text
TRUTH/standards/AETHER_OPERATOR_PROFILE.md
TRUTH/standards/
TRUTH/PROJECT_STATE.md
TRUTH/AGENTS.md
TRUTH/TRUTH.md
TRUTH/CHANGELOG.md
```

If project-specific files do not exist yet, create them from `TRUTH/templates/`.

---

# Templates

Files in `TRUTH/templates/` are project-specific templates.

They are copied into the active repo’s `TRUTH/` root and then filled out for that repo.

## Template Files

```text
TRUTH/templates/AGENTS.template.md
TRUTH/templates/PROJECT_STATE.template.md
TRUTH/templates/CHANGELOG.template.md
TRUTH/templates/TRUTH.template.md
```

## Copy Pattern

```text
TRUTH/templates/AGENTS.template.md        → TRUTH/AGENTS.md
TRUTH/templates/PROJECT_STATE.template.md → TRUTH/PROJECT_STATE.md
TRUTH/templates/CHANGELOG.template.md     → TRUTH/CHANGELOG.md
TRUTH/templates/TRUTH.template.md         → TRUTH/TRUTH.md
```

After copying, remove `.template` from the filename.

Templates are working memory. Fill them out for each repo.

---

# Standards

Files in `TRUTH/standards/` are durable AetherPro operating standards.

They should be copied into serious private/internal repos mostly unchanged.

## Standard Files

```text
TRUTH/standards/AETHER_OPERATOR_PROFILE.md
TRUTH/standards/AI-Readable-Crawl-Support.md
TRUTH/standards/GIT-WORKFLOW-DISCIPLINE.md
TRUTH/standards/Git-Workflows.md
TRUTH/standards/IP_HYGIENE.md
```

## Standards Rule

Do not modify standards unless explicitly instructed.

Standards define operating doctrine.

Project-specific files define the current repo truth.

Do not mix the two.

---

# Canonical Project Files

These files live in the root of `TRUTH/` after templates are copied and filled.

```text
TRUTH/AGENTS.md
TRUTH/PROJECT_STATE.md
TRUTH/CHANGELOG.md
TRUTH/TRUTH.md
```

---

## `AGENTS.md`

Use for:

* agent/operator instructions
* repo-specific working rules
* deployment workflow
* environment notes
* coordination rules
* files agents must read before work

Do not use it for:

* long historical logs
* secrets
* speculative roadmap writing

Examples:

* what the repo is
* where it runs
* how to test
* what the agent must not touch
* which docs must stay updated

---

## `PROJECT_STATE.md`

Use for:

* current implementation status
* what is live
* what is incomplete
* important active files
* active blockers
* recommended next steps

Do not use it for:

* deep historical chronology
* broad architecture philosophy
* duplicate changelog entries

Examples:

* feature X is live
* feature Y is mock-driven
* deploy target is `/var/www/service-name`
* next step is fixing auth redirect

---

## `CHANGELOG.md`

Use for:

* dated record of material changes
* merges
* deploys
* auth changes
* infra changes
* API changes
* release notes

Do not use it for:

* duplicate project-state prose
* open questions
* vague notes without dates

Example:

```text
2026-05-25: Fixed Agency config route 405, rebuilt Passport provider, deployed updated admin UI.
```

---

## `TRUTH.md`

Use for:

* terse snapshot of current reality
* public URLs
* repo names
* infra location
* active production facts
* operator mechanics

Do not use it for:

* verbose narrative
* every minor implementation detail
* speculative plans

Examples:

* site URL
* backend URL
* production branch
* deploy host
* active container names
* current auth behavior

---

# Update Triggers

Update the canonical project docs whenever:

* a live feature is added or removed
* a deploy path changes
* a public URL changes
* auth behavior changes
* a branch is merged and published
* infra/host/runtime facts change
* operator workflow changes
* dependency assumptions change
* agents should behave differently

Minimum rule:

```text
If production truth changed, update TRUTH.md.
If repo behavior changed, update PROJECT_STATE.md.
If the change matters historically, update CHANGELOG.md.
If agent behavior should change, update AGENTS.md.
```

---

# Suggested Workflow

1. Read `TRUTH/README.md`.
2. Read all relevant files in `TRUTH/standards/`.
3. Read project-specific files in `TRUTH/`.
4. Inspect repo status.
5. Make the smallest safe change.
6. Run relevant validation.
7. Update TRUTH docs.
8. Review diff.
9. Commit and push only scoped changes.

---

# Git Hygiene

Agents must inspect before changing:

```bash
git status --short
git branch --show-current
```

Before commit, agents should show:

```bash
git diff --stat
git diff
```

Do not commit:

* secrets
* private keys
* tokens
* unrelated formatting churn
* generated junk
* accidental screenshots
* local-only config
* large model files

---

# Public Safety

Do not expose private/internal details from standards or project docs in public copy.

Public-facing material must be sanitized.

Especially avoid publishing:

* admin URLs
* private endpoints
* VM topology
* tokens
* private keys
* customer data
* internal deployment details
* unverified compliance claims

---

# Rule

Templates become project memory.

Standards define operating discipline.

Project files tell the current truth.

If the repo changes, update the truth.

````
