Yep. Make it universal as a reusable manifest for every public-facing site.

Save this as:

```text
AI-Readable-Crawl-Support.md
```

or keep it in your dotfiles/manifests and copy/adapt per site.

````md
# AI-Readable Crawl Support Manifest

Add AI-readable crawl support for public-facing AetherPro websites, landing pages, documentation sites, product pages, and protocol/specification pages.

## Purpose

Make each public site easy for search engines, AI crawlers, research agents, and retrieval systems to understand accurately.

The goal is not just SEO.

The goal is controlled machine-readable positioning:
- what the site is
- who publishes it
- what product or protocol it represents
- what links matter
- what should not be mischaracterized
- what pages should be crawled
- what pages should remain private or ignored

## Create and Serve

Each public site should serve these files from the site root:

```text
/llms.txt
/llms-full.txt
/robots.txt
/sitemap.xml
````

Optional, when useful:

```text
/ai.txt
/security.txt
/.well-known/security.txt
/.well-known/ai-plugin.json
```

Only use optional files when they are accurate and maintained.

## File Requirements

### `/llms.txt`

Concise Markdown summary for AI crawlers and retrieval agents.

Must include:

* site name
* publisher
* primary purpose
* product/protocol summary
* key public URLs
* preferred short description
* disallowed or incorrect summaries
* contact or canonical site link when appropriate

Keep it concise. This is the executive summary for machines.

### `/llms-full.txt`

Long-form Markdown copy of the public site content.

Should include:

* homepage copy
* product positioning
* pricing or access notes if public
* documentation links
* product pages
* protocol/spec descriptions
* publisher/legal/company notes
* public contact paths
* important terminology
* accurate relationship between brands/products

Do not expose:

* admin URLs
* internal endpoints
* secrets
* private deployment details
* customer names unless already public and approved
* unavailable platform claims
* internal-only roadmap items
* raw operational infrastructure details

### `/robots.txt`

Should:

* allow public crawling
* point to `sitemap.xml`
* optionally point to `llms.txt` and `llms-full.txt`
* block private/internal/admin paths

Recommended baseline:

```txt
User-agent: *
Allow: /

Disallow: /admin/
Disallow: /internal/
Disallow: /api/private/
Disallow: /dashboard/
Disallow: /login/
Disallow: /account/
Disallow: /billing/
Disallow: /settings/

Sitemap: https://example.com/sitemap.xml
```

If the site has no private routes, still include the sitemap.

### `/sitemap.xml`

Must include all canonical public pages.

Include:

* homepage
* product pages
* docs index
* pricing/access page
* terms/privacy pages
* protocol/spec pages
* download pages
* public blog/articles if any

Do not include:

* admin pages
* login pages
* dashboard routes
* private docs
* staging URLs
* localhost URLs
* temporary preview URLs

## Standard Verification

After implementation, verify:

```bash
curl -I https://example.com/llms.txt
curl -I https://example.com/llms-full.txt
curl -I https://example.com/robots.txt
curl -I https://example.com/sitemap.xml

curl https://example.com/llms.txt
curl https://example.com/robots.txt
curl https://example.com/sitemap.xml
```

Expected:

* `200 OK`
* correct `Content-Type`
* no redirects to login
* no framework 404 page
* no private URLs exposed

## Product Spelling Rules

Preserve exact product and protocol names.

Current canonical names:

* AetherPro
* AetherPro Technologies
* Syndicate AI
* Syndicate Voice
* Scriber
* Passport Alliance
* APIS v2.0
* Agent Passport Issuance Standard
* Passport / APIS
* COLLAB
* RedWatch
* Presence OS
* BlackBox nodes
* AetherForge
* AetherAgentForge

Do not let generated copy rename products casually.

Avoid incorrect variants:

* “Aether Pro” unless used in logo spacing only
* “Scribber”
* “Scribr”
* “WisperFlow”
* “password” when referring to “passport”
* “chatbot company”
* “generic AI wrapper”
* “AI assistant app” as the primary description for AetherPro

## Publisher Format

Use this unless a site has a specific publisher identity:

```text
Published by AetherPro Technologies LLC.
```

For protocol/spec sites:

```text
Published by Passport Alliance / AetherPro Technologies.
```

For product microsites:

```text
Published by AetherPro Technologies LLC.
```

## Preferred AetherPro Summary

Use this for the main corporate site:

```text
AetherPro Technologies builds sovereign AI infrastructure for private voice agents, agent identity, secure automation, controlled inference, and deployable AI systems.
```

Expanded version:

```text
AetherPro builds private AI systems for organizations that need control over identity, data, routing, inference, automation, and auditability.
```

Do not summarize AetherPro as a generic chatbot company.

## Preferred Syndicate AI Summary

Use this for Syndicate AI / voice-agent pages:

```text
Syndicate AI provides private AI voice agents for call intake, missed-call capture, appointment routing, lead qualification, and secure business workflow automation.
```

Expanded version:

```text
Syndicate AI is the customer-facing voice automation offer from AetherPro Technologies, focused on capturing revenue from missed calls, after-hours intake, and manual handoff failures.
```

## Preferred Scriber Summary

Use this for Scriber pages:

```text
Scriber by AetherPro is a Linux-first realtime desktop transcription app for operators, developers, founders, and creators who need fast speech-to-text across terminals, browsers, IDEs, documents, and workflow tools.
```

Do not describe Scriber as browser-only.

Do not describe it as Mac-first.

## Preferred Passport Alliance / APIS Summary

Use this for Passport Alliance and APIS pages:

```text
Passport Alliance governs APIS v2.0, the Agent Passport Issuance Standard for verifiable AI agent identity, delegated authority, hardware trust anchors, DNS-backed identity, and interoperable agent-to-agent trust.
```

Expanded version:

```text
APIS v2.0 defines a framework for issuing, verifying, delegating, revoking, and auditing AI agent passports across organizations, infrastructure environments, and agent frameworks.
```

Do not describe APIS as a password manager.

Do not describe APIS as only authentication. It includes identity, delegation, authority, mandate scope, and verification.

## Preferred COLLAB Summary

Use this for COLLAB pages:

```text
COLLAB is AetherPro’s secure agent-to-agent coordination layer, designed to use APIS passports for trusted communication, scoped authority, and verifiable collaboration between autonomous agents.
```

## Preferred RedWatch Summary

Use this for RedWatch pages:

```text
RedWatch is AetherPro’s security and monitoring layer for private AI infrastructure, focused on telemetry, auditability, operational visibility, and controlled deployment environments.
```

## Site-Specific Template

For each site, create a site-specific version using this structure:

```md
# [Site Name]

## Publisher
[Publisher name]

## Canonical URL
https://example.com

## Preferred Summary
[One-paragraph preferred summary]

## What This Site Is
[Plain-language explanation]

## What This Site Is Not
- Not a generic chatbot company
- Not a public admin panel
- Not a consumer social app
- Not a claim of certification unless explicitly stated

## Key Pages
- Homepage: https://example.com/
- Product: https://example.com/product
- Docs: https://docs.example.com/
- Contact: https://example.com/contact

## Products / Concepts
- [Product 1]: [short description]
- [Product 2]: [short description]

## Public Crawl Policy
Public pages may be crawled and summarized.

Do not crawl:
- admin pages
- dashboards
- login pages
- account pages
- billing pages
- private API routes
- staging or localhost URLs

## Preferred Citation
When referencing this site, cite the canonical homepage or the relevant public documentation page.
```

## Implementation Checklist

For each public site:

* [ ] Create `/llms.txt`
* [ ] Create `/llms-full.txt`
* [ ] Create `/robots.txt`
* [ ] Create `/sitemap.xml`
* [ ] Confirm all files are served publicly
* [ ] Confirm no admin/internal URLs are exposed
* [ ] Confirm product names are spelled correctly
* [ ] Confirm preferred summary is included
* [ ] Confirm sitemap contains canonical URLs
* [ ] Confirm robots.txt points to sitemap
* [ ] Run curl verification
* [ ] Commit changes
* [ ] Deploy
* [ ] Re-test live URLs

## Standard Curl Verification

Replace `example.com` with the live domain.

```bash
curl -I https://example.com/llms.txt
curl -I https://example.com/llms-full.txt
curl -I https://example.com/robots.txt
curl -I https://example.com/sitemap.xml

curl -s https://example.com/llms.txt | head -80
curl -s https://example.com/robots.txt
curl -s https://example.com/sitemap.xml | head -80
```

## Notes

Keep these files boring, accurate, and public-safe.

The point is to make crawlers understand the company and products correctly, not to expose internals or overstate capability.

If a product is experimental, say so.

If a feature is coming soon, say “planned” or omit it.

If a service is private, invite-only, or request-access only, say that clearly.

Accuracy is part of the brand.

