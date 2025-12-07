#!/usr/bin/env sh

HTTP_STATUS=$(curl -s -o /dev/null -s -H "X-Vault-Token: ${OPENBAO_TOKEN}" -w "%{http_code}\n" http://pst-openbao:8200/v1/sys/mounts);
COUNTER=0;
while [ "$HTTP_STATUS" != "200" ]
 do
  COUNTER=$(($COUNTER+1));
  sleep 1000;
  HTTP_STATUS=$(curl -s -o /dev/null -s -H "X-Vault-Token: ${OPENBAO_TOKEN}" -w "%{http_code}\n" http://pst-openbao:8200/v1/sys/mounts);
  if [ "$HTTP_STATUS" == "200" ] || [ "$COUNTER" -lt  10 ]; then
    break;
  fi
done
if [ "$HTTP_STATUS" == "200" ]; then
 curl -v -H "X-Vault-Token: ${OPENBAO_TOKEN}" -X POST -d '{"type":"kv-v2"}' http://pst-openbao:8200/v1/sys/mounts/dev-secrets;
 curl -v -H "X-Vault-Token: ${OPENBAO_TOKEN}" -X PUT -d '{ "data": {"cipherKey": "${APP_CIPHER_KEY}"} }' http://pst-openbao:8200/v1/dev-secrets/data/creds;
fi