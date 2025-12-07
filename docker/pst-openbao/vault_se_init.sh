#!/usr/bin/env sh

apk add curl

HTTP_STATUS=$(curl -s -o /dev/null -s -H "X-Vault-Token: ${TOKEN}" -w "%{http_code}\n" http://pst-openbao:8200/v1/sys/mounts);
COUNTER=0;
while [ "$HTTP_STATUS" != "200" ]
 do
  COUNTER=$(($COUNTER+1));
  sleep 1000;
  HTTP_STATUS=$(curl -s -o /dev/null -s -H "X-Vault-Token: ${TOKEN}" -w "%{http_code}\n" http://pst-openbao:8200/v1/sys/mounts);
  if [ "$HTTP_STATUS" == "200" ] || [ "$COUNTER" -lt  10 ]; then
    break;
  fi
done
if [ "$HTTP_STATUS" == "200" ]; then
 curl -v -H "X-Vault-Token: ${TOKEN}" -X POST -d '{"type":"kv-v2"}' http://pst-openbao:8200/v1/sys/mounts/${ROOT_SECRETS};
 curl -v -H "X-Vault-Token: ${TOKEN}" -X PUT -d '{ "data": {"cipherKey": "'${CIPHER_KEY}'"} }' http://pst-openbao:8200/v1/${ROOT_SECRETS}/data/${CREDENTIALS};
fi