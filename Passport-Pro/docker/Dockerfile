# =====================================================
# Passport Pro Production Dockerfile
# SIMPLE: Uses pre-built Keycloak, adds custom extensions
# =====================================================
# 
# BUILD INSTRUCTIONS:
# 1. First, build the extensions locally:
#    ./mvnw clean package -pl passport-extensions/agency,js/apps/admin-ui -am -DskipTests
#
# 2. Then build the Docker image:
#    docker build -f docker/Dockerfile -t passport-pro:latest .
#
# =====================================================

FROM quay.io/keycloak/keycloak:26.2.2

# Enable features
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
ENV KC_DB=postgres

# Copy pre-built extension JARs (build locally first!)
COPY passport-extensions/agency/target/passport-agency-*.jar /opt/keycloak/providers/
COPY js/apps/admin-ui/target/passport-admin-ui-*.jar /opt/keycloak/providers/

# Copy custom themes if present
# COPY themes/src/main/resources/theme/* /opt/keycloak/themes/

WORKDIR /opt/keycloak

# Bake in the extensions
RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
