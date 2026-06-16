#!/usr/bin/env bash
# =====================================================
# Build & push the aetherops/passport image (pull-and-run Passport-IAM).
#
# Produces a self-contained, OPTIMIZED image carrying the FORK distribution:
# the Agency/APIS extension (in lib/lib/main, with META-INF/beans.xml so
# RESTEasy Reactive indexes it), the rebranded admin UI, and Passport themes.
#
# Usage:
#   ./build-and-push-passport-image.sh            # build + smoke test only
#   ./build-and-push-passport-image.sh --push     # also push to Docker Hub
#   VERSION=0.1.0 ./build-and-push-passport-image.sh --push
# =====================================================
set -euo pipefail
cd "$(dirname "$0")/Passport-Pro"

VERSION="${VERSION:-0.1.0}"
IMAGE="aetherops/passport"
DIST_DIR="passport-999.0.0-SNAPSHOT"
DIST="quarkus/dist/target/${DIST_DIR}"

echo "==> 1/5 Build the Agency extension (installs the beans.xml jar into ~/.m2)"
./mvnw -q clean install -pl passport-extensions/agency -am -DskipTests

# NOTE: a full 'install -pl quarkus/dist -am' tries to rebuild the JS/admin-ui
# module (passport-js-parent) and can fail on a stale js/node/pnpm symlink.
# The admin-ui jar rarely changes and is already in ~/.m2, so we rebuild only
# quarkus/server (which declares the agency dependency, commit 92c7439) WITHOUT
# -am, then reassemble the dist WITHOUT -am — both resolve from ~/.m2.
echo "==> 2/5 Rebuild quarkus/server (no -am) so the dist pulls the fresh agency dep"
./mvnw -q install -pl quarkus/server -DskipTests -DskipProtoLock=true

echo "==> 3/5 Reassemble the distribution (no -am)"
./mvnw -q install -pl quarkus/dist -DskipTests -DskipProtoLock=true

echo "==> 4/5 Verify dist correctness (agency+beans.xml in lib/lib/main, exactly one changelog jar)"
rm -rf "$DIST"
tar -xzf "quarkus/dist/target/${DIST_DIR}.tar.gz" -C quarkus/dist/target/
M=$(find "$DIST/lib" -iname "*agency*.jar" | head -1)
[ -n "$M" ] || { echo "ABORT: no agency jar in lib/lib/main"; exit 1; }
unzip -l "$M" | grep -q "META-INF/beans.xml" || { echo "ABORT: agency jar missing beans.xml"; exit 1; }
N=$(find "$DIST" -iname "*agency*.jar" | xargs -I{} sh -c 'unzip -l "{}" 2>/dev/null | grep -q passport-agency-changelog.xml && echo X' | wc -l)
[ "$N" -eq 1 ] || { echo "ABORT: expected exactly one agency changelog jar, found $N"; exit 1; }
echo "    OK: $M (beans.xml present, single changelog jar)"

echo "==> 5/5 Build image ${IMAGE}:${VERSION} + :latest (runs kc.sh build inside)"
docker build -f docker/Dockerfile.passport \
  --build-arg DIST_DIR="${DIST_DIR}" --build-arg PASSPORT_VERSION="${VERSION}" \
  -t "${IMAGE}:${VERSION}" -t "${IMAGE}:latest" \
  quarkus/dist/target

if [ "${1:-}" = "--push" ]; then
  echo "==> Pushing ${IMAGE}:${VERSION} and :latest (ensure 'docker login' as aetherops first)"
  docker push "${IMAGE}:${VERSION}"
  docker push "${IMAGE}:latest"
fi
echo "Done. Image: ${IMAGE}:${VERSION}"
