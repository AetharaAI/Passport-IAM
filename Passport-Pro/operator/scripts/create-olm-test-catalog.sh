#! /bin/bash
set -euxo pipefail

VERSION=$1
BUNDLE_IMAGE=$2

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

rm -rf $SCRIPT_DIR/../olm/catalog
mkdir -p $SCRIPT_DIR/../olm/catalog/test-catalog

(
  cd $SCRIPT_DIR/../olm/catalog

  opm generate dockerfile test-catalog

  opm init passport-operator \
    --default-channel=alpha \
    --output yaml > test-catalog/operator.yaml

  opm render $BUNDLE_IMAGE:$VERSION \
    --output=yaml --skip-tls >> test-catalog/operator.yaml

  cat << EOF >> test-catalog/operator.yaml
---
schema: olm.channel
package: passport-operator
name: alpha
entries:
  - name: passport-operator.v$VERSION
EOF

  opm validate test-catalog
)
