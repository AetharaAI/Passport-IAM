# Passport-Pro Deployment Guide

## Prerequisites
- Java 11 or higher
- Database (PostgreSQL, MySQL, or MariaDB recommended)
- At least 2GB RAM for production
- SSL certificate for production deployment

## Building from Source

1. Clone the repository:
```bash
git clone <repository-url>
cd Passport-Pro
```

2. Build the entire project (this is required to resolve internal dependencies):
```bash
mvn clean install -DskipTests
```

Note: The first build may take considerable time as it builds all modules. Subsequent builds will be faster due to Maven caching.

If you encounter dependency resolution issues:
- Ensure you're building from the root directory
- Try building specific modules in order if needed
- Check that all required Java versions are installed

## Quick Start (Development)

1. Navigate to the distribution directory:
```bash
cd distribution/server-dist/target/passport-pro-*/bin
```

2. Start Passport in development mode:
```bash
./standalone.sh -Djboss.socket.binding.port-offset=0
```

## Production Deployment

### Option 1: Standalone Server

1. Extract the server distribution:
```bash
tar -xzf passport-pro-*.tar.gz
cd passport-pro-*/
```

2. Configure database (edit standalone/configuration/standalone.xml):
```xml
<datasource jndi-name="java:jboss/datasources/PassportDS" pool-name="passpor">
    <connection-url>jdbc:postgresql://localhost:5432/passport</connection-url>
    <driver>postgresql</driver>
    <security>
        <user-name>dbuser</user-name>
        <password>dbpassword</password>
        </security>
    </datasource>
```

3. Start the server:
```bash
./bin/standalone.sh -b 0.0.0.0
```

### Option 2: Docker Deployment

1. Build Docker image:
```bash
docker build -t passport-pro .
```

2. Run with Docker:
```bash
docker run -p 8080:8080 -p 8443:8443 passport-pro
```

### Option 3: Reverse Proxy with NGINX

1. Configure NGINX (/etc/nginx/sites-available/passport):
```nginx
server {
    listen 80;
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

2. Enable the site:
```bash
ln -s /etc/nginx/sites-available/passport /etc/nginx/sites-enabled/
nginx -t && systemctl reload nginx
```

## SSL Configuration

1. Obtain SSL certificate (Let's Encrypt recommended):
```bash
certbot --nginx -d passport.aetherpro.us
```

## Initial Configuration

1. Access the admin console at:
   https://passport.aetherpro.us/admin/

2. Create initial admin user:
```bash
./bin/add-user-keycloak.sh -u admin -p your_secure_password
```

## Environment Variables

Key environment variables for production:
```bash
PASSPORT_FRONTEND_URL=https://passport.aetherpro.us
PASSPORT_ADMIN_URL=https://passport.aetherpro.us/admin
DB_VENDOR=POSTGRES
DB_ADDR=database.host
DB_PORT=5432
DB_DATABASE=passport
DB_USER=passport_user
DB_PASSWORD=secure_password
```

## Monitoring and Logging

1. Check logs:
```bash
tail -f standalone/log/server.log
```

2. Health check endpoint:
```bash
curl https://passport.aetherpro.us/health
```

## Integration with AetherPro Technologies

### Domains to Configure:
- aetherpro.tech (Aether Chat)
- aetherpro.us (Corporate Landing)
- aetheragentforge.org (AI Agent Marketplace)
- blackboxaudio.tech (Voice Models)
- mcpfabric.space (MCP Communications)
- perceptor.us (Client Monitoring)

### SSO Configuration Steps:
1. Create realms for each domain in Passport Admin Console
2. Configure clients for each application
3. Set up identity providers as needed
4. Configure user federation if integrating with existing systems

## Backup Strategy

1. Regular database backups:
```bash
pg_dump passport > passport_backup_$(date +%Y%m%d).sql
```

2. Configuration backup:
```bash
tar -czf passport_config_backup_$(date +%Y%m%d).tar.gz standalone/configuration/
```

## Troubleshooting

Common issues and solutions:
1. Port conflicts: Change port offset in startup script
2. Memory issues: Increase JVM heap size
3. Database connectivity: Verify connection settings
4. SSL issues: Check certificate configuration
5. Maven dependency issues: Run full build from root directory first

### Maven Dependency Resolution Issues

If you encounter errors like "Missing artifact org.passport:passport-core:jar:999.0.0-SNAPSHOT":

1. Always build from the root project directory:
```bash
cd Passport-Pro
mvn clean install -DskipTests
```

2. If specific modules fail, try building them in order:
```bash
mvn clean install -pl core -am -DskipTests
mvn clean install -pl integration/client-cli/admin-cli -DskipTests
```

3. Clear Maven cache if needed:
```bash
mvn clean compile -U
```

This happens because this is a fork of Keycloak with renamed artifacts, and Maven needs to build all dependencies first.

## Security Considerations

1. Always use HTTPS in production
2. Regular security updates
3. Strong password policies
4. Network segmentation
5. Regular audit logs review