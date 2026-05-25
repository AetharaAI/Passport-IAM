#!/bin/bash
# Passport-Pro Build & Deploy Script
# This builds the distribution with the Agency extension

set -e  # Exit on error

echo "🔨 Building Passport-Pro with Agency Extension..."
cd /home/cory/Documents/Passport-IAM/Passport-Pro

# Step 1: Clean and build the Agency extension
echo "📦 Building Agency extension..."
./mvnw clean install -pl passport-extensions/agency -am -DskipTests

# Step 2: Build the complete distribution
echo "📦 Building distribution..."
./mvnw clean install -pl quarkus/dist -am -DskipTests -DskipProtoLock=true

# Step 3: Extract the distribution
echo "📂 Extracting distribution..."
cd quarkus/dist/target
rm -rf passport-999.0.0-SNAPSHOT
tar -xzf passport-999.0.0-SNAPSHOT.tar.gz

echo "✅ Build complete!"
echo ""
echo "Distribution location: $(pwd)/passport-999.0.0-SNAPSHOT"
echo ""
echo "Next steps:"
echo "1. cd $(pwd)/passport-999.0.0-SNAPSHOT"
echo "2. ./bin/kc.sh build"
echo "3. ./bin/kc.sh start --http-enabled=true --hostname=passport.aetherpro.us --proxy-headers=xforwarded --db=postgres --db-url=jdbc:postgresql://localhost:5432/passport_iam --db-username=passport_admin --db-password=passport_dev_2026"
