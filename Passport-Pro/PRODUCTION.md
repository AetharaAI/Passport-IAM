# Production Deployment & SSO Guide

This guide details how to build, containerize, and deploy Passport Pro to your production VM (`passport.aetherpro.us`), and how to configure SSO for your applications.

## 1. Build the Distribution

First, creating the production-ready artifact from your source code.

```bash
# Run from the project root
./mvnw clean install -DskipTestsuite -DskipExamples -DskipTests -DskipProtoLock=true
```

This ensures `passport-extensions/agency` and `js/apps/admin-ui` are included.
**Artifact**: `quarkus/dist/target/passport-999.0.0-SNAPSHOT.tar.gz`

---

## 2. Dockerize

Create a `Dockerfile.prod` in the project root to package your custom build.

**File: `Dockerfile.prod`**
```dockerfile
FROM eclipse-temurin:21-jdk-jammy

# Install utilities
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy build artifact
COPY quarkus/dist/target/passport-999.0.0-SNAPSHOT.tar.gz /tmp/

# Extract and install
RUN cd /tmp && \
    tar -xvf passport-999.0.0-SNAPSHOT.tar.gz && \
    mv passport-999.0.0-SNAPSHOT /opt/passport && \
    rm passport-999.0.0-SNAPSHOT.tar.gz

# Create passport user for security
RUN groupadd -r passport && useradd -r -g passport -d /opt/passport -s /sbin/nologin passport
RUN chown -R passport:passport /opt/passport

USER passport
WORKDIR /opt/passport

# Expose ports
EXPOSE 8080

# Entrypoint
ENTRYPOINT ["/opt/passport/bin/kc.sh"]
```

**Build the Image:**
```bash
docker build -f Dockerfile.prod -t passport-pro:latest .
```

---

## 3. Deploy with Docker Compose

On your VM, create a directory structure:
```
/opt/passport-pro/
├── docker-compose.yml
└── nginx/
    └── nginx.conf
```

**File: `docker-compose.yml`**
```yaml
services:
  postgres:
    image: postgres:15
    volumes:
      - postgres_data:/var/lib/postgresql/data
    environment:
      POSTGRES_DB: passport_iam
      POSTGRES_USER: passport_admin
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    networks:
      - passport-net

  passport:
    image: passport-pro:latest
    # If building on VM: navigate to source and use 'build: .'
    command: start --optimized
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/passport_iam
      KC_DB_USERNAME: passport_admin
      KC_DB_PASSWORD: ${DB_PASSWORD}
      KC_HOSTNAME: passport.aetherpro.us
      KC_PROXY: edge
      KC_BOOTSTRAP_ADMIN_USERNAME: admin
      KC_BOOTSTRAP_ADMIN_PASSWORD: ${ADMIN_PASSWORD}
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    networks:
      - passport-net

networks:
  passport-net:

volumes:
  postgres_data:
```

---

## 4. Nginx Reverse Proxy (SSL)

Install Nginx and Certbot on your VM to handle HTTPS.

```bash
sudo apt install nginx certbot python3-certbot-nginx
```

**File: `/etc/nginx/sites-available/passport`**
```nginx
server {
    server_name passport.aetherpro.us;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**Enable and Secure:**
```bash
sudo ln -s /etc/nginx/sites-available/passport /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
sudo certbot --nginx -d passport.aetherpro.us
```

---

## 5. Setup SSO for Applications

Once deployed and accessible at `https://passport.aetherpro.us`:

### A. Create Realm
1.  Log in to Admin Console.
2.  Click **Create Realm**.
3.  Name: `AetherPro` (or `AetherAgentForge`).
4.  Click **Create**.

### B. Configure Agency Features
1.  Go to **Manage > Agency**.
2.  Toggle **Agency Enabled**.
3.  Create a Principal for your organization (e.g., "AetherPro Tech").

### C. Create OIDC Client (for Next.js/React)
1.  Go to **Clients > Create client**.
2.  **Client ID**: `aether-app` (e.g., `web-platform`).
3.  **Capability config**:
    -   Client authentication: `On` (if using backend BFF) or `Off` (public client). configuration depends on your stack.
    -   *Recommended for Web Apps*: Use **Standard Flow**.
4.  **Login settings**:
    -   **Root URL**: `https://aetherpro.tech`
    -   **Valid redirect URIs**: `https://aetherpro.tech/*`
    -   **Web origins**: `https://aetherpro.tech`

### D. Application Integration (code snippet)

For a Next.js app using `next-auth`:

```javascript
// [...nextauth].js
import KeycloakProvider from "next-auth/providers/keycloak";

export default NextAuth({
  providers: [
    KeycloakProvider({
      clientId: process.env.KEYCLOAK_ID,
      clientSecret: process.env.KEYCLOAK_SECRET,
      issuer: "https://passport.aetherpro.us/realms/AetherPro",
    }),
  ],
});
```

Repeat the Client creation step for `aetheragentforge.org` in the same realm (SSO) or a new realm (isolated).

---

## 6. Verification
1.  Navigate to `https://aetherpro.tech` (your app).
2.  Click Login.
3.  Ensure you are redirected to `passport.aetherpro.us`.
4.  Login with a user from the `AetherPro` realm.
5.  Success!
