#! /bin/bash
set -euxo pipefail

VERSION=$1
DOCKER_REGISTRY=$2

UUID=${3:-""}

TARGET_NAMESPACES=${4-default}

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

rm -rf $SCRIPT_DIR/../olm/testing-resources
mkdir -p $SCRIPT_DIR/../olm/testing-resources

cat << EOF >> $SCRIPT_DIR/../olm/testing-resources/catalog.yaml
apiVersion: operators.coreos.com/v1alpha1
kind: CatalogSource
metadata:
  name: test-catalog
  namespace: default
spec:
  sourceType: grpc
  image: $DOCKER_REGISTRY/${UUID}passport-test-catalog:$VERSION
  displayName: Passport Test Catalog
  publisher: Me
  updateStrategy:
    registryPoll:
      interval: 10m
EOF


OPERATOR_GROUP_FILE=$SCRIPT_DIR/../olm/testing-resources/operatorgroup.yaml

cat << EOF >> $OPERATOR_GROUP_FILE
kind: OperatorGroup
apiVersion: operators.coreos.com/v1
metadata:
  name: og
spec:
EOF

IFS=', ' read -r -a array <<< "$TARGET_NAMESPACES"
for element in "${array[@]}"
do
    yq ea -i ".spec.targetNamespaces += [\"$element\"]" $OPERATOR_GROUP_FILE
done

cat << EOF >> $SCRIPT_DIR/../olm/testing-resources/subscription.yaml
apiVersion: operators.coreos.com/v1alpha1
kind: Subscription
metadata:
  name: passport-operator
spec:
  installPlanApproval: Automatic
  name: passport-operator
  source: test-catalog
  sourceNamespace: default
  startingCSV: passport-operator.v$VERSION
EOF
