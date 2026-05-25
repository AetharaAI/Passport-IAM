# 1. Extract the distribution
cd quarkus/dist/target
tar -xzf passport-999.0.0-SNAPSHOT.tar.gz
cd passport-999.0.0-SNAPSHOT

# 2. Start PostgreSQL & Redis (you already have docker-compose)
cd ~/Passport-IAM/Passport-IAM/Passport-Pro
docker compose up -d postgres redis-stack

# 3. Build and start Passport
cd quarkus/dist/target/passport-999.0.0-SNAPSHOT
./bin/kc.sh build

# 4. Start in production mode
./bin/kc.sh start \
    --hostname=passport.aetherpro.us \
    --proxy=edge \
    --http-enabled=true \
    --db=postgres \
    --db-url=jdbc:postgresql://localhost:5432/passport_iam \
    --db-username=passport_admin \
    --db-password=YOUR_DB_PASSWORD

./bin/kc.sh start \
    --hostname=passport.aetherpro.us \
    --proxy-headers=xforwarded \
    --http-enabled=true \
    --http-port=8080 \
    --db=postgres \
    --db-url=jdbc:postgresql://localhost:5432/passport_iam \
    --db-password=passport_dev_2026
