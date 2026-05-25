#!/bin/bash
# Test Passport with Agency Extension
# This runs Passport locally with the Agency extension loaded

set -e

DIST_DIR="/home/cory/Documents/Passport-IAM/Passport-Pro/quarkus/dist/target/passport-999.0.0-SNAPSHOT"
DOCKER_COMPOSE_DIR="/home/cory/Documents/Passport-IAM/Passport-Pro"

# Default password (can be overridden with environment variable)
DB_PASSWORD="${KC_DB_PASSWORD:-passport_dev_2026}"

echo "🚀 Starting Passport with Agency Extension..."
echo ""

# Check if PostgreSQL is running
echo "🔍 Checking PostgreSQL..."
cd "$DOCKER_COMPOSE_DIR"
if ! docker compose ps postgres 2>/dev/null | grep -q "Up"; then
    echo "📦 PostgreSQL not running. Starting database..."
    docker compose up -d postgres redis-stack
    echo "⏳ Waiting for PostgreSQL to be ready..."
    sleep 5
    
    # Wait for PostgreSQL to be actually ready
    for i in {1..30}; do
        if docker compose exec -T postgres pg_isready -U passport_admin -d passport_iam > /dev/null 2>&1; then
            echo "✅ PostgreSQL is ready!"
            break
        fi
        echo "   Still waiting for PostgreSQL... ($i/30)"
        sleep 2
    done
else
    echo "✅ PostgreSQL is already running"
fi

echo ""
echo "Distribution: $DIST_DIR"
echo "Agency JAR: $(ls -lh $DIST_DIR/providers/passport-agency-*.jar 2>/dev/null || echo 'NOT FOUND')"
echo ""

cd "$DIST_DIR"

# Build the server first (optimizes for production)
echo "🔧 Building Passport..."
./bin/kc.sh build

echo ""
echo "✅ Build complete!"
echo ""
echo "🌐 Starting Passport..."
echo "   Access at: http://localhost:8080"
echo "   Admin console: http://localhost:8080/admin/master/console"
echo ""
echo "💡 Tip: Default admin credentials usually 'admin' / 'admin' for dev"
echo ""

# Start the server
./bin/kc.sh start-dev \
    --http-port=8080 \
    --db=postgres \
    --db-url=jdbc:postgresql://localhost:5433/passport_iam \
    --db-username=passport_admin \
    --db-password="$DB_PASSWORD"
