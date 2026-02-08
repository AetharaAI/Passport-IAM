#!/bin/bash
# =====================================================
# Passport Pro Build & Deploy Script
# =====================================================
# This script builds the extensions locally and then
# builds/deploys the Docker container.
# =====================================================

set -e

echo "🔧 Step 1: Building Passport Extensions + Themes..."
./mvnw clean package \
    -pl passport-extensions/agency,js/apps/admin-ui,themes \
    -am \
    -DskipTests \
    -Dmaven.test.skip=true

echo "✅ Extensions built successfully!"
echo ""

echo "🐳 Step 2: Building Docker Image..."
docker build -f docker/Dockerfile -t passport-pro:latest .

echo "✅ Docker image built successfully!"
echo ""

echo "🚀 Step 3: Starting Services..."
docker compose up -d

echo ""
echo "=============================================="
echo "🎉 Passport Pro is starting!"
echo "=============================================="
echo ""
echo "Admin Console: https://passport.aetherpro.us/admin"
echo "Default credentials: admin / admin (change immediately!)"
echo ""
echo "View logs: docker compose logs -f passport"
echo ""
