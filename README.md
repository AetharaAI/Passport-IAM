# Passport-Pro (Passport-IAM)

Passport-Pro is a rebranded fork of Keycloak focused on Agency Access Management and Legal Based Access Control for Agentic Systems. It provides Identity and Access Management capabilities tailored for AI agent ecosystems.

## Overview

This project implements Passport-IAM (Identity Access Management) with a focus on:
- Agency Access Management
- Legal Based Access Control for Agentic Systems
- SSO across the AetherPro Technologies ecosystem
- OAuth token minting for AI agents ("agent passports")

## Features

- Rebranded from Keycloak to Passport
- Customized for AI agent authentication and authorization
- Multi-realm identity management
- OAuth 2.0 and OpenID Connect support
- SAML 2.0 support
- User federation capabilities
- Customizable admin console

## Branding Changes

All instances of "Keycloak" have been replaced with "Passport" including:
- "Security Admin Console" → "Passport Security Admin Console"
- "Admin CLI" → "Passport Admin CLI"
- All client-facing UI elements
- Documentation references
- Internal code references

## Deployment

See [DEPLOYMENT.md](DEPLOYMENT.md) for detailed deployment instructions.

## Integration with AetherPro Technologies

This IAM solution is designed to provide SSO across:
- aetherpro.tech (Aether Chat)
- aetherpro.us (Corporate Landing)
- aetheragentforge.org (AI Agent Marketplace)
- blackboxaudio.tech (Voice Models)
- mcpfabric.space (MCP Communications)
- perceptor.us (Client Monitoring)

## Known Issues

### Maven Dependency Resolution
Some internal Maven dependencies may not resolve correctly due to this being a fork. This is normal and typically resolved by:
1. Building the entire project from the root
2. Installing missing artifacts locally
3. Adjusting version references in pom.xml files

To resolve dependency issues:
```bash
mvn clean install -DskipTests
```

If specific modules fail, you may need to:
1. Check version numbers in pom.xml files
2. Ensure parent project is built first
3. Manually install missing JARs to local Maven repository

## Building the Project

Due to this being a fork with renamed artifacts, you must build the entire project to resolve internal dependencies:

```bash
mvn clean install -DskipTests
```

This will take some time on the first run as it builds all modules. The build process:
1. Compiles all source code
2. Resolves internal dependencies (like passport-core)
3. Runs any necessary code generation
4. Packages all modules

If you encounter build errors:
1. Ensure you're running the command from the root directory
2. Check that you have sufficient memory (recommend 4GB+ RAM)
3. Verify Java version compatibility
4. Try running with `-X` flag for debug output: `mvn clean install -DskipTests -X`

## Contributing

This is a specialized fork for AetherPro Technologies. Contributions should align with the agency access management and legal-based access control objectives.

## License

This project is based on Keycloak and maintains the same licensing model. See LICENSE file for details.

## Contact

For issues related to AetherPro Technologies deployment, contact the development team.