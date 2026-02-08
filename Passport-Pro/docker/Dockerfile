# =====================================================
# Passport Pro Production Dockerfile (Multi-Stage)
# =====================================================

# Stage 1: Build the Application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy source code
COPY . .

# Build the custom distribution
# Skips tests and unnecessary modules for speed
RUN ./mvnw clean install -DskipTestsuite -DskipExamples -DskipTests -DskipProtoLock=true

# Stage 2: Create the Runtime Image
# using the Keycloak base image
FROM quay.io/keycloak/keycloak:26.2.2

# Enable features
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
ENV KC_DB=postgres

# Copy built artifacts from the build stage
COPY --from=build /app/passport-extensions/agency/target/*.jar /opt/keycloak/providers/
COPY --from=build /app/js/apps/admin-ui/target/passport-admin-ui-*.jar /opt/keycloak/providers/
# Add other extensions here if needed

WORKDIR /opt/keycloak

# Optimize the build
RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
