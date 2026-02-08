# Passport IAM: Sovereign Identity for the Agentic Web

**Passport IAM** is a production-grade Identity and Access Management system designed for the era of AI. Built on the robust foundation of Keycloak 26, it introduces the concept of **Agency**—treating AI Agents as first-class citizens with verifiable identities, delegated authority, and legal-based access controls.

## 🚀 Mission

To provide a sovereign, self-hosted identity layer where humans and autonomous agents can interact securely. Passport IAM bridges the gap between traditional OIDC/OAuth2 and the emerging Agentic Web (DIDs, Verifiable Credentials).

## ✨ Key Features

### 1. Agency Extension (LBAC)
The core differentiator of Passport IAM is the **Agency** system:
- **Principals**: Manage Legal Entities (Individuals, Corporations, DAOs).
- **Agent Passports**: Mint persistent, verifiable DIDs (`did:passport:...`) for AI Agents.
- **Delegation**: Issue **Mandates** that grant specific scopes of authority to agents (e.g., "Sign Contracts", "Manage Cloud Resources").
- **Admin UI**: Fully integrated management dashboard within the Passport Admin Console.

### 2. Premium Branding & UI
- Custom **Passport Pro** theme with a modern, dark-mode aesthetic.
- Enhanced Admin Console experience powered by React and PatternFly.
- "Agency" navigation fully integrated into the sidebar.

### 3. Enterprise-Ready Architecture
- **Core**: Keycloak 26 (Quarkus-based, high performance).
- **Database**: PostgreSQL for robust data persistence.
- **Security**: Full OIDC, SAML, and OAuth 2.0 compliance.

## 🛠️ Build & Run

### Prerequisites
- Java 21 (OpenJDK)
- Maven
- Docker (optional, for DB)

### Build Distribution
```bash
./mvnw clean install -DskipTestsuite -DskipExamples -DskipTests -DskipProtoLock=true
```

### Run Locally (Dev Mode)
```bash
# Extract the built distribution
cd quarkus/dist/target
tar -xzf passport-999.0.0-SNAPSHOT.tar.gz
cd passport-999.0.0-SNAPSHOT

# Run with H2 (default) or Postgres
bin/kc.sh start-dev
```

## 📦 Production Deployment

For full production setup instructions, including Dockerfiles, Nginx reverse proxy configuration, and SSO setup, please refer to:
👉 **[Production Deployment Guide](./PRODUCTION.md)**

## 🤝 Contributing

Passport IAM is open-source software. We welcome contributions to the Agency extension, UI enhancements, and core identity features.

## 📄 License
Apache 2.0.
