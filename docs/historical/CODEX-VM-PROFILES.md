# Codex VM Profiles

## Purpose

Use the lightest Codex model that can safely complete the job.
Default to operational discipline first:

1. Inspect current runtime state before changing anything.
2. Check listeners, containers, processes, and logs first.
3. Do not restart unrelated services.
4. Preserve the current deploy shape unless the task explicitly includes changing it.
5. Verify with commands before claiming success.

## Quick Rule

- Use `gpt-5.4-mini` for simple ops.
- Use `gpt-5.4` for normal engineering and deployment work.
- Use `gpt-5.5` for high-stakes debugging, architecture, or cross-system reasoning.

## Reasoning Rule

- Use `low` for rote, checklist-style work.
- Use `medium` for most real work.
- Use `high` only when ambiguity is real and the cost of being wrong is high.

## Profile 1: Simple Ops

Use for:

- `git pull`
- rebuilding one app
- redeploying a static site
- restarting one service
- checking nginx upstreams
- checking logs and HTTP status
- copying artifacts into known deploy paths

Recommended:

- Model: `gpt-5.4-mini`
- Reasoning: `low`

Prompt pattern:

```text
Inspect runtime state first, pull latest changes, redeploy only the affected app or service, verify logs and HTTP status, and summarize exact commands run.
```

## Profile 2: Standard Engineering

Use for:

- targeted code changes
- build debugging
- repo inspection
- deployment debugging
- docs and code sync
- moderate frontend or backend fixes

Recommended:

- Model: `gpt-5.4`
- Reasoning: `medium`

Prompt pattern:

```text
Inspect repo and runtime state first, verify assumptions with commands, make minimal reversible changes, run targeted validation, and summarize outcome and blockers.
```

## Profile 3: Hard Debugging

Use for:

- auth bugs
- route mismatches
- multi-service failures
- deployment path ambiguity
- IAM or issuance failures
- tricky reverse proxy behavior
- cross-repo reasoning

Recommended:

- Model: `gpt-5.5`
- Reasoning: `medium`

Escalate to `high` only if:

- the root cause is ambiguous after inspection
- multiple services or repos interact
- the deploy shape is unclear
- the bug is business-critical

Prompt pattern:

```text
Treat this as high-stakes debugging. Read docs and source first, inspect runtime state, verify every assumption, preserve the current deploy shape, fix the issue with minimal changes, validate thoroughly, and summarize exact commands run.
```

## Profile 4: Production Triage

Use for:

- production outages
- crash loops
- broken health checks
- failed deploy recovery
- upstream connectivity problems

Recommended:

- Model: `gpt-5.4`
- Reasoning: `medium`

Escalate to `gpt-5.5` only when triage shows the issue is deeper than routine ops.

Prompt pattern:

```text
Do read-only triage first. Check listeners, containers, logs, recent changes, failing health checks, and blast radius before changing anything.
```

## Node Presets

### `b3-32-flex-us-east-va-1`

- Tailscale IP: `100.92.18.20`
- Recommended default: `gpt-5.4-mini`
- Reasoning: `low`
- Node role: simple ops and redeploys

Use this preset when the work is like:

- pulling repo changes
- rebuilding one frontend or app bundle
- redeploying behind nginx
- syncing files into `/var/www/...`
- checking HTTP 200s
- confirming an unrelated live site was not disturbed

Escalate from `gpt-5.4-mini` to `gpt-5.4` if:

- the deploy path is unclear
- multiple services interact
- nginx behavior is not obvious
- the repo is dirty or the worktree is mixed

### Passport / IAM nodes

- Recommended default: `gpt-5.4`
- Reasoning: `medium`

Escalate to `gpt-5.5` for:

- auth failures
- issuance failures
- APIS flow debugging
- realm/provider route mismatches
- cross-service deploy ambiguity

### Multi-service agent infrastructure nodes

Examples:

- COLLAB
- NATS JetStream
- APIS-connected control plane
- multi-container orchestrated nodes

Recommended default:

- Model: `gpt-5.4`
- Reasoning: `medium`

Escalate to `gpt-5.5` when the problem spans identity, transport, storage, and deployment at the same time.

## Safe Prompt Add-On

Append this to most VM prompts:

```text
Inspect current state before changing anything. Check listeners, containers, and logs first. Do not restart unrelated services. Preserve the existing deploy shape unless explicitly told otherwise. Verify with commands before claiming success. Summarize exact commands run, files changed, and remaining blockers.
```

## Practical Selection Guide

If the task feels like:

- "just pull, rebuild, restart, verify" -> `gpt-5.4-mini`
- "inspect repo and runtime, then fix something targeted" -> `gpt-5.4`
- "this is complicated, high-stakes, or spans systems" -> `gpt-5.5`

## Limit Discipline

To stretch usage:

1. Start with `gpt-5.4-mini` for easy ops work.
2. Move to `gpt-5.4` when the task stops being routine.
3. Use `gpt-5.5` intentionally for the hardest or highest-risk work.

That gives the best balance of speed, cost, and reliability across mixed node work.
