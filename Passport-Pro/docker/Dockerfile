# =====================================================
# Passport Pro Production Dockerfile
# =====================================================
FROM quay.io/keycloak/keycloak:26.2.2 as builder

# Enable health and metrics support
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

# Configure database
ENV KC_DB=postgres

WORKDIR /opt/keycloak

# Build optimized image
RUN /opt/keycloak/bin/kc.sh build

# =====================================================
# Production Image
# =====================================================
FROM quay.io/keycloak/keycloak:26.2.2

COPY --from=builder /opt/keycloak/ /opt/keycloak/

# Copy custom extensions (Agency, Redis Stack integration)
COPY passport-extensions/agency/target/*.jar /opt/keycloak/providers/
# COPY passport-extensions/redis-stack/target/*.jar /opt/keycloak/providers/

# Set working directory
WORKDIR /opt/keycloak

# Default entrypoint
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
