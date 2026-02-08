#! /bin/bash
set -euxo pipefail

VERSION="${1:-latest}"
PASSPORT_IMAGE="${2:-quay.io/passport/passport}"
PASSPORT_CUSTOM_IMAGE="${3:-quay.io/passport/custom-passport}"

echo "Using version: $VERSION"

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

docker build -f ${SCRIPT_DIR}/Dockerfile-custom-image \
  --build-arg IMAGE=${PASSPORT_IMAGE} \
  --build-arg VERSION=${VERSION} \
  -t ${PASSPORT_CUSTOM_IMAGE}:${VERSION} ${SCRIPT_DIR}
