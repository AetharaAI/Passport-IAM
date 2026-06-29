# AetherSpace Workspace Isolation — Build Spec (demo slice)

**Target:** PresenceOS / AetherSpace
**Scope:** The enforced workspace boundary — data scope + agent-authorization scope + audit scope, all bound to one primitive. Sized for the **single-workspace demo**, architected so it extends.
**Hand to:** Claude Code, on the actual repo. Read the "Verify first" block before generating anything.

---

## 0. The one idea

A **workspace** is the single isolation boundary for three things at once:

1. **Data** — relational rows, vector points, cache keys, message subjects.
2. **Authorization** — which agent identity (Passport) may act, and where.
3. **Audit** — a per-workspace tamper-evident ledger (RedWatch).

These are enforced **below the application**, at the DB / token / messaging layer — not by app-side `if` checks. App-layer filtering is one bug from a confidentiality leak, which is indefensible for the pitch ("your ethical walls, enforced by the system"). Defense-in-depth: a code bug must not be able to cross a wall, because the engine underneath refuses.

The boundary is real or the pitch is a lie. Build it real.

**Maps to:** a law firm's matter-level conflict wall / a CPA's per-engagement client isolation. One workspace = one sealed matter.

---

## 1. Verify first (Claude Code — do this before writing code)

Do **not** assume a clean repo or a fresh schema. Inspect actual state and report back before generating migrations:

- `Passport` (Keycloak fork): confirm version, whether custom protocol mappers / client scopes are already in use, and how agent service accounts are currently modeled. We need a `workspace_id` claim path that doesn't collide with existing mappers.
- `Postgres`: confirm version (need ≥ 9.5 for RLS; `FORCE ROW LEVEL SECURITY` needs ≥ 9.5 too — fine). Confirm the app connects with a **non-superuser role that does NOT have `BYPASSRLS`**. If the current app role is a superuser or owner, RLS is silently void — flag it.
- `Qdrant`: list current collections and how they're named/created today. We're moving to collection-per-workspace; confirm nothing assumes a single global collection.
- `NATS`: confirm whether decentralized auth (account/user JWTs with subject permissions) is enabled, or whether it's currently a single shared cred. This determines demo-minimal vs full cred minting.
- `RedWatch`: confirm current ledger schema and whether a hash chain already exists. We're adding **per-workspace** chaining; don't clobber an existing global chain — run them side by side.
- `COLLAB`: this spec supplies the **realm-scoped authorization check on token issuance** — which is the known missing `claim_role` authorization gap. Confirm where COLLAB currently hands tasks to agents so the new token-gate slots in there.

Report findings, then generate migrations against the **real** schema.

---

## 2. The keystone: WorkspaceContext resolver

Every service (Aether Gateway, COLLAB, MCP Fabric, RedWatch writer) calls **one** function. It is the only place that reads the token and applies scope. This is what makes the system coherent instead of N services each rolling their own filter.

```python
# aetherspace/context.py
from dataclasses import dataclass
from contextlib import contextmanager
import uuid

@dataclass(frozen=True)
class WorkspaceContext:
    realm: str
    workspace_id: uuid.UUID
    actor: str          # Passport subject (agent DID / passport id)
    jti: str            # token id — correlates every audit entry
    scopes: frozenset   # e.g. {"pg:read", "qdrant:read", "mcp:docstore"}
    pg                  # scoped DB session (RLS GUC already set)
    qdrant              # ScopedQdrant pinned to this workspace's collection
    nats_creds: str     # path/handle to workspace-scoped NATS creds
    audit               # RedWatchWriter bound to this workspace

def resolve_workspace_context(token: str) -> WorkspaceContext:
    claims = verify_passport_jwt(token)          # sig, exp, iss, aud — see §4
    ws = uuid.UUID(claims["workspace_id"])       # FROM THE SIGNED TOKEN ONLY
    require_audience(claims, f"ws-{ws}")          # aud must match the workspace
    # NOTE: workspace_id is never read from a request param. The token binds it.
    pg = open_scoped_pg(ws)                        # §3
    return WorkspaceContext(
        realm=claims["realm"], workspace_id=ws,
        actor=claims["sub"], jti=claims["jti"],
        scopes=frozenset(claims.get("scope", "").split()),
        pg=pg,
        qdrant=ScopedQdrant(ws),                   # §3
        nats_creds=nats_creds_for(ws),             # §3
        audit=RedWatchWriter(ws, actor=claims["sub"], jti=claims["jti"]),  # §5
    )
```

**Non-negotiable invariant:** `workspace_id` comes from the **signed token**, never from a request body, query param, or header the agent controls. To act in a different workspace an agent needs a *different* Passport-issued token, which is an authorization-checked, logged event (§4). That single rule is what stops COLLAB from walking an agent across a wall.

Polyglot note: if any service isn't Python, replicate this resolver as the first thing that service does on every request. Same contract, same invariant. No service touches Postgres/Qdrant/NATS without going through its resolver.

---

## 3. Data layer — what a workspace IS

### 3a. Postgres — Row-Level Security (chosen)

**Decision:** single schema, every workspace-scoped table carries `workspace_id uuid NOT NULL`, isolation enforced by **RLS policies** keyed on a session GUC.

**Why this over the alternatives:**
- *Schema-per-workspace* — rejected: migrations × N schemas, doesn't scale past a handful of matters, operational tax for zero security gain over RLS.
- *Database-per-workspace* — rejected: absurd ops cost for many matters.
- *App-layer `WHERE workspace_id = ?`* — rejected: one missing clause = cross-matter leak. Not defensible.

RLS is DB-engine-enforced: a query that forgets the filter returns **zero rows**, not another matter's rows. A bug can't leak.

```sql
-- workspace registry
CREATE TABLE workspaces (
  workspace_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  realm        text NOT NULL,
  display_name text NOT NULL,
  status       text NOT NULL DEFAULT 'active',  -- active | sealed | destroyed
  created_at   timestamptz NOT NULL DEFAULT now()
);

-- example workspace-scoped table
CREATE TABLE matter_documents (
  id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  workspace_id uuid NOT NULL REFERENCES workspaces(workspace_id),
  title        text NOT NULL,
  body         text,
  created_at   timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE matter_documents ENABLE ROW LEVEL SECURITY;
ALTER TABLE matter_documents FORCE  ROW LEVEL SECURITY;  -- owner is subject to RLS too

CREATE POLICY ws_isolation ON matter_documents
  USING      (workspace_id = current_setting('app.workspace_id')::uuid)
  WITH CHECK (workspace_id = current_setting('app.workspace_id')::uuid);
```

Setting the scope (injection-safe, transaction-local):

```python
def open_scoped_pg(workspace_id):
    conn = pg_pool.getconn()        # role WITHOUT BYPASSRLS
    cur = conn.cursor()
    cur.execute("BEGIN")
    # set_config with bind param — never string-format the id into SQL
    cur.execute("SELECT set_config('app.workspace_id', %s, true)",
                (str(workspace_id),))
    return ScopedSession(conn, cur)  # commit/rollback ends the txn + the GUC
```

`set_config(..., true)` makes it **LOCAL to the transaction** — the scope cannot leak into a pooled connection's next checkout. If the app role had `BYPASSRLS`, all of this is void — verify in §1.

### 3b. Qdrant — collection-per-workspace (chosen)

**Decision:** one collection per workspace, named `ws_{workspace_id}`. A scoped client wrapper pins the collection and refuses any cross-collection call.

**Why:** hard wall (not a payload filter you can forget), and — the selling point — **clean destruction**: sealing/shredding a matter is `DELETE collection ws_{id}`, cryptographically gone. That maps directly onto legal retention/destruction obligations. "Shred the matter" = drop the collection.

```python
class ScopedQdrant:
    def __init__(self, workspace_id):
        self._coll = f"ws_{workspace_id}"
        self._client = qdrant_client
    def search(self, vector, **kw):
        return self._client.search(collection_name=self._coll, query_vector=vector, **kw)
    def upsert(self, points, **kw):
        return self._client.upsert(collection_name=self._coll, points=points, **kw)
    # no method accepts a caller-supplied collection name. The wall is structural.
```

*Scaling caveat (ASSUMPTION, later problem):* per-collection is right for bounded live-matter counts (dozens–low hundreds — i.e. every real firm). If you ever need thousands of concurrently-live workspaces, switch to single-collection + **mandatory** payload filter injected by the resolver. Don't build that now.

### 3c. Redis & NATS — namespace + subject scoping

- **Redis:** prefix every key `ws:{workspace_id}:...` via the resolver. Demo: prefix isolation. Later: Redis ACL users restricted to a key-prefix pattern for engine-enforced isolation.
- **NATS:** subject hierarchy `aether.ws.{workspace_id}.>`. An agent's NATS user JWT permits pub/sub **only** on its workspace subtree. This is the messaging wall — an agent in Matter A physically cannot publish into or subscribe to Matter B's COLLAB coordination subjects.
  - Demo-minimal: pre-provision one NATS cred per workspace; the resolver hands back the right one based on the token. (`nats_creds_for(ws)`.)
  - Later: Passport mints the workspace-scoped NATS user JWT dynamically at token-issue time. Deferred — don't block the demo on it.

---

## 4. Passport — workspace-scoped tokens

**Decision:** Passport issues short-lived (15 min) **workspace-scoped** access tokens. Workspace is a sub-scope within the APIS realm (realm → workspace).

**Claims:**

```json
{
  "iss": "https://passport.aetherpro...",
  "sub": "did:aether:agent:echo1",   // agent passport id
  "realm": "firm-acme",
  "workspace_id": "8f3c...uuid",
  "aud": "ws-8f3c...uuid",            // resource servers reject aud != their ws
  "scope": "pg:read qdrant:read mcp:docstore",
  "jti": "tok_01H...",               // correlates to RedWatch entries
  "exp": 1234567890
}
```

**Three independent checks** (defense in depth): signature/`exp`, `aud == ws-{id}`, and the `workspace_id` claim drives §3 scoping. All three must agree.

**Issuance authorization — this closes the COLLAB `claim_role` gap.** Before Passport mints a workspace-scoped token, it runs the realm-scoped authorization check that COLLAB is currently missing:

```python
def issue_workspace_token(agent_identity_token, requested_workspace):
    base = verify_passport_jwt(agent_identity_token)     # agent's base identity
    agent = base["sub"]; realm = base["realm"]
    # THE CHECK: is this agent, in this realm, authorized for this workspace?
    if not is_agent_authorized(realm, agent, requested_workspace):
        audit_global("deny", agent, "token_issue", requested_workspace)  # logged denial
        raise Unauthorized("agent not provisioned for workspace")
    return mint_jwt(sub=agent, realm=realm,
                    workspace_id=requested_workspace,
                    aud=f"ws-{requested_workspace}",
                    scope=scopes_for(agent, requested_workspace),
                    ttl=900)
```

- Demo-minimal Keycloak path: model the agent service account with a `workspace_ids` attribute (its allowed list); a protocol mapper injects the selected `workspace_id`; `is_agent_authorized` checks the requested ws is in that list. This is enough to demo the deny.
- Production path: RFC 8693 token exchange — agent presents base identity token, requests a workspace-scoped token, Passport runs the same authorization check. Same logic, cleaner protocol. Upgrade later.

An agent **cannot** widen its own scope: it holds a token bound to one workspace; getting another means re-requesting from Passport and passing the check above — which is logged whether it allows or denies.

---

## 5. RedWatch — per-workspace audit chain

**Decision:** one logical ledger table carrying `workspace_id`; the **tamper-evident hash chain is computed per workspace**. Each matter has its own self-contained, verifiable chain.

**Why per-workspace chain:** you can hand one matter's *complete and independently verifiable* audit trail to opposing counsel / a regulator **without exposing any other matter**. That's the legally-defensible artifact. A single global chain would force you to reveal unrelated entries to prove integrity.

```sql
CREATE TABLE redwatch_ledger (
  id           bigserial PRIMARY KEY,
  workspace_id uuid NOT NULL,
  ts           timestamptz NOT NULL DEFAULT now(),
  actor        text NOT NULL,          -- passport sub
  jti          text,                   -- token id (correlates the request)
  action       text NOT NULL,          -- e.g. doc.read, vector.search, egress.attempt
  resource     text,
  decision     text NOT NULL,          -- allow | deny
  prev_hash    bytea NOT NULL,
  entry_hash   bytea NOT NULL,
  payload      jsonb NOT NULL DEFAULT '{}'
) PARTITION BY HASH (workspace_id);
-- create N hash partitions sized to expected workspace count

CREATE TABLE redwatch_heads (
  workspace_id uuid PRIMARY KEY,
  head_hash    bytea NOT NULL
);
```

Append (per-workspace chain head, serialized by row lock):

```python
import hashlib, json

class RedWatchWriter:
    def __init__(self, workspace_id, actor, jti):
        self.ws, self.actor, self.jti = workspace_id, actor, jti

    def record(self, conn, action, resource, decision, payload=None):
        cur = conn.cursor()
        # lock this workspace's chain head — serializes appends within the ws
        cur.execute("SELECT head_hash FROM redwatch_heads "
                    "WHERE workspace_id=%s FOR UPDATE", (str(self.ws),))
        row = cur.fetchone()
        prev = row[0] if row else b"\x00" * 32   # genesis
        entry = {"workspace_id": str(self.ws), "actor": self.actor, "jti": self.jti,
                 "action": action, "resource": resource, "decision": decision,
                 "payload": payload or {}}
        canon = json.dumps(entry, sort_keys=True, separators=(",", ":")).encode()
        h = hashlib.sha256(prev + canon).digest()
        cur.execute("INSERT INTO redwatch_ledger "
                    "(workspace_id,actor,jti,action,resource,decision,prev_hash,entry_hash,payload) "
                    "VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s)",
                    (str(self.ws), self.actor, self.jti, action, resource,
                     decision, prev, h, json.dumps(entry["payload"])))
        cur.execute("INSERT INTO redwatch_heads (workspace_id, head_hash) VALUES (%s,%s) "
                    "ON CONFLICT (workspace_id) DO UPDATE SET head_hash=EXCLUDED.head_hash",
                    (str(self.ws), h))
```

Verification (hand-off artifact): walk a workspace's entries in `id` order, recompute `sha256(prev || canon)`, assert it equals `entry_hash` and that each `prev_hash` equals the prior `entry_hash`. Chain intact = trail untampered.

*Anchoring (production, deferred):* periodically sign each workspace head and anchor to your APIS Tier-1 root (the TPM/air-gapped root-key ceremony you already ran). Demo: per-workspace SHA-256 chain + a periodic signed checkpoint is enough.

---

## 6. Request lifecycle (the control flow)

1. Agent presents workspace-scoped Passport JWT to Gateway / COLLAB.
2. `resolve_workspace_context(token)` — verify sig/exp, `aud == ws-{id}`, extract `workspace_id` + `jti`.
3. Postgres: `SET LOCAL app.workspace_id` → RLS now active for the txn.
4. Qdrant: all calls hit `ws_{id}` only (structural).
5. NATS: agent creds restrict pub/sub to `aether.ws.{id}.>`.
6. MCP Fabric: docstore connector sees only workspace `{id}`'s documents.
7. **Every step writes a RedWatch entry** `(workspace_id, jti, action, resource, allow/deny)` — hash-chained.
8. Sentinel evaluates any egress against the default-deny allowlist; allow/deny → RedWatch.

---

## 7. Demo acceptance criteria (the "amaze people" moment)

The demo proves the wall with two undeniable beats. Build exactly this, nothing more:

1. **Two workspaces exist** — `Matter A`, `Matter B`. Each has one document in Postgres + one Qdrant collection.
2. **Cross-wall read is refused at the data layer.** An agent holding a Matter-A token attempts to read a Matter-B document → returns **zero rows** (RLS) and Qdrant search hits only `ws_A`. A **deny** entry lands in Matter A's RedWatch chain. Show the denial on screen — the wall held, and it's logged.
3. **Egress is blocked.** The agent attempts an outbound call not on Sentinel's allowlist → blocked, **deny** entry in RedWatch. "Your data tried to leave. It didn't. Here's the proof."
4. **Audit hand-off.** Export Matter A's RedWatch chain, run the verifier, show it's intact and contains **only** Matter A entries.

If those four land on the single freed-up L40S node, you've demonstrated a sovereign operating environment doing privileged work without data leaving the room — the whole pitch, in five minutes.

---

## 8. Explicitly OUT of scope for the demo

Do not build these now (they're real, they're later):
- Dynamic NATS cred minting from Passport (use pre-provisioned per-ws creds).
- RFC 8693 token exchange (use the service-account attribute path).
- TPM/APIS Tier-1 anchoring of RedWatch heads (use SHA-256 chain + checkpoint).
- Dead-agent lease / heartbeat reclamation and Redis ACL enforcement — separate COLLAB correctness work, not part of the isolation wall.
- More than one agent, one document source, one egress rule.

One workspace boundary, proven hard, audited, on owned compute. Ship that.
