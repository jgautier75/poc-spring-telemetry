#!/usr/bin/env bash

echo "Authenticate"
/opt/keycloak/bin/kcadm.sh config credentials -x --server http://localhost:7080 --realm master --user ${KEYCLOAK_ADMIN} --password ${KEYCLOAK_ADMIN_PASSWORD}

FEDERATION_DEPLOYED=$(/opt/keycloak/bin/kcadm.sh get components -r ${KEYCLOAK_REALM} -x | grep 'federation-spi' | grep 'providerId' | wc -l)
echo "Federation configured: $FEDERATION_DEPLOYED"
if [[ "${FEDERATION_DEPLOYED}" == "1" ]]; then
  echo "Federation SPI already enabled"
else
  echo "Setup Federation SPI"
  /opt/keycloak/bin/kcadm.sh create components -r ${KEYCLOAK_REALM} -s name=federation-spi -s providerId=federation-spi -s providerType=org.keycloak.storage.UserStorageProvider -s 'config.enabled=["true"]' -x
fi