# Passport Pro Production Deployment Guide

## Quick Start (VM Deployment)

### Prerequisites on VM
- Java 21 (OpenJDK)
- Maven (or use included `./mvnw`)
- Docker & Docker Compose
- PostgreSQL (via Docker)

### Step 1: Clone Repository
```bash
cd ~
git clone <your-repo-url> Passport-IAM
cd Passport-IAM/Passport-Pro
```

### Step 2: Build Extensions
```bash
# Build ONLY the custom extensions (fast, ~2 min)
./mvnw clean package \
    -pl passport-extensions/agency,js/apps/admin-ui \
    -am \
    -DskipTests \
    -Dmaven.test.skip=true
```

### Step 3: Build & Start
```bash
# Build Docker image and start services
docker compose up --build -d

# Check logs
docker compose logs -f passport
```

### Step 4: Verify
- Admin Console: https://passport.aetherpro.us/admin
- Default login: `admin` / `admin` (CHANGE IMMEDIATELY)

---

## Configuration

### Environment Variables (.env)
```bash
# Database
KC_DB_PASSWORD=your_secure_password_here
REDIS_PASSWORD=your_redis_password_here

# Admin (CHANGE THESE!)
KC_BOOTSTRAP_ADMIN_USERNAME=admin
KC_BOOTSTRAP_ADMIN_PASSWORD=change_this_immediately

# Hostname
KC_HOSTNAME=passport.aetherpro.us
```

### Nginx Reverse Proxy
```nginx
server {
    listen 80;
    server_name passport.aetherpro.us;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name passport.aetherpro.us;

    ssl_certificate /etc/letsencrypt/live/passport.aetherpro.us/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/passport.aetherpro.us/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_buffer_size 128k;
        proxy_buffers 4 256k;
        proxy_busy_buffers_size 256k;
    }
}
```

---

## Setting Up SSO for Your Applications

### 1. Create a Realm
- Go to Admin Console → Create Realm
- Name: `aetherpro` (or your org name)

### 2. Create Clients
For each application (e.g., `aetherpro.tech`, `aetheragentforge.org`):

1. Go to Clients → Create Client
2. Client ID: `aetherpro-web` (or similar)
3. Client Type: OpenID Connect
4. Valid Redirect URIs: `https://aetherpro.tech/*`
5. Web Origins: `https://aetherpro.tech`
6. Copy the Client Secret for your app config

### 3. Application Integration (Next.js Example)
```typescript
// next-auth configuration
import NextAuth from "next-auth";
import KeycloakProvider from "next-auth/providers/keycloak";

export const authOptions = {
  providers: [
    KeycloakProvider({
      clientId: "aetherpro-web",
      clientSecret: process.env.KEYCLOAK_CLIENT_SECRET!,
      issuer: "https://passport.aetherpro.us/realms/aetherpro",
    }),
  ],
};
```

---

## Troubleshooting

### Build Fails
```bash
# Clean everything and retry
docker compose down -v
docker system prune -af
./mvnw clean
# Then run Step 2 and 3 again
```

### Check Container Logs
```bash
docker compose logs passport
docker compose logs postgres
```

### Database Reset
```bash
docker compose down -v  # WARNING: Deletes all data!
docker compose up -d
```
