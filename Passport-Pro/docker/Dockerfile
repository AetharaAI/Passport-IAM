# =====================================================
# Passport Pro Production Dockerfile
# Uses Keycloak base + YOUR custom Passport branding
# =====================================================
# 
# Your custom themes and Admin UI OVERRIDE Keycloak defaults!
# - passport-admin-ui.jar = Your rebranded React Admin Console
# - themes/passport/ = Your custom login/account themes
#
# BUILD INSTRUCTIONS:
# 1. First, build the extensions locally:
#    ./mvnw clean package -pl passport-extensions/agency,js/apps/admin-ui,themes -am -DskipTests -Dmaven.test.skip=true
#
# 2. Then build the Docker image:
#    docker compose up --build -d
#
# =====================================================

FROM quay.io/keycloak/keycloak:26.2.2

# Enable features
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
ENV KC_DB=postgres

# =====================================================
# Copy YOUR custom Passport branding (overrides Keycloak!)
# =====================================================

# Custom Admin UI (your rebranded React app with Passport logo)
COPY js/apps/admin-ui/target/passport-admin-ui-*.jar /opt/keycloak/providers/

# Custom Agency extension
COPY passport-extensions/agency/target/passport-agency-*.jar /opt/keycloak/providers/

# Custom Passport themes (login pages, account pages, emails)
COPY themes/src/main/resources/theme/passport /opt/keycloak/themes/passport
COPY themes/src/main/resources/theme/passport.v2 /opt/keycloak/themes/passport.v2

WORKDIR /opt/keycloak

# Bake in the extensions and themes
RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
