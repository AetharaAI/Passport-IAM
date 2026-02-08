Passport Pro Build & Run Walkthrough
Summary
Successfully debugged and built the Passport Pro project, fixing multiple issues caused by an incorrect Keycloak-to-Passport renaming regex.

Issues Fixed
1. Corrupted Java Package Declarations (67+ files)
The original renaming replaced org.keycloak.organization with org.passport-pro.aianization instead of org.passport.organization.

Fixed with:

find . -name "*.java" -exec sed -i 's/org\.passport-pro\.aianization/org.passport.organization/g' {} \;
2. Invalid npm Package Reference
Changed passport-js (non-existent) back to keycloak-js in:

js/apps/account-ui/package.json
js/apps/admin-ui/package.json
js/libs/ui-shared/package.json
3. TypeScript Import Fixes
Fixed imports from "passport-js" → "keycloak-js"
Fixed type PassportTokenParsed → KeycloakTokenParsed
4. File/Class Name Mismatches
Renamed files to match their class names:

KEYCLOAK_JDBC_PING2.java
 → 
PASSPORT_JDBC_PING2.java
KEYCLOAK_JDBC_PING2_FOR_TESTING.java → PASSPORT_JDBC_PING2_FOR_TESTING.java
5. META-INF/services Configuration Files
Fixed corrupted class names in service provider files.

Build Commands
# Full build (skip tests and examples)
./mvnw clean install -DskipTestsuite -DskipExamples -DskipTests -DskipProtoLock=true
# Run from distribution
cd /tmp && mkdir passport-run && cd passport-run
tar -xzf /path/to/Passport-Pro/quarkus/dist/target/passport-999.0.0-SNAPSHOT.tar.gz
cd passport-999.0.0-SNAPSHOT
./bin/kc.sh start-dev
Verification Results
✅ Build completed successfully in ~1:20 min
✅ Server started at http://localhost:8080
✅ Dev mode active with H2 database (default)

Server Startup Log
Passport 999.0.0-SNAPSHOT on JVM (powered by Quarkus 3.31.1) started in 9.942s
Listening on: http://localhost:8080
Profile dev activated
Installed features: [agroal, cdi, hibernate-orm, jdbc-h2, narayana-jta, opentelemetry, passport, reactive-routes, rest, rest-jackson, smallrye-context-propagation, vertx]
Next Steps
Access Admin Console: http://localhost:8080/admin
Create Admin User: First access will prompt for admin credentials
Verify Agency Domain: Test organization management features
Dockerize: Create production Docker configuration