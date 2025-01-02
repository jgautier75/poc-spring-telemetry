#!/usr/bin/env sh

HTTP_STATUS=$(curl -s -o /dev/null -s -H "X-Vault-Token: dev-root-token" -w "%{http_code}\n" http://poc-st-openbao:8200/v1/sys/mounts);
COUNTER=0;
while [ "$HTTP_STATUS" != "200" ]
 do
  COUNTER=$(($COUNTER+1));
  sleep 1000;
  HTTP_STATUS=$(curl -s -o /dev/null -s -H "X-Vault-Token: dev-root-token" -w "%{http_code}\n" http://poc-st-openbao:8200/v1/sys/mounts);
  if [ "$HTTP_STATUS" == "200" ] || [ "$COUNTER" -lt  10 ]; then
    break;
  fi
done
if [ "$HTTP_STATUS" == "200" ]; then
 curl -v -H "X-Vault-Token: dev-root-token" -X POST -d '{"type":"kv-v2"}' http://poc-st-openbao:8200/v1/sys/mounts/dev-secrets;
 curl -v -H "X-Vault-Token: dev-root-token" -X PUT -d '{ "data": {"cipherKey": "1c9e1cfbe63844b1a0772aea4cba5gg6"} }' http://poc-st-openbao:8200/v1/dev-secrets/data/creds;
fi