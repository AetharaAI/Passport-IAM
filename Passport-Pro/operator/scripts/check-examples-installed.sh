#! /bin/bash
set -euo pipefail

NAMESPACE=${1:-default}

max_retries=500
c=0
while [[ $(kubectl -n $NAMESPACE get passports/example-kc -o jsonpath="{.status.conditions[?(@.type == 'Ready')].status}") != "True" ]]
do
  echo "$(date +"%T") Waiting for Passport example-kc status"
  ((c++)) && ((c==max_retries)) && exit -1
  sleep 1
done

c=0
while [[ $(kubectl -n $NAMESPACE get passportrealmimports/example-count0-kc -o jsonpath="{.status.conditions[?(@.type == 'Done')].status}") != "True" ]]
do
  echo "$(date +"%T") Waiting for Passport Realm Import example-count0-kc status"
  ((c++)) && ((c==max_retries)) &&  exit -1
  sleep 1
done
