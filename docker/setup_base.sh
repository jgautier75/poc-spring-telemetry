#!/bin/bash

MYIP=$(hostname -I | awk '{print $1}')

echo "Copying spi dependencies in poc-libs directory mounted on /opt/keycloak/lib/lib/main"
cp  ~/.m2/repository/org/apache/kafka/kafka-clients/3.9.0/kafka-clients-3.9.0.jar poc-libs/kafka-clients-3.9.0.jar

echo "Current IPV4 address: ${MYIP}"
echo "Substitute ip address in docker-services.yml"
sed -e "s/MYIPADDRESS/${MYIP}/g" docker-services.yml > docker-services-base.yml

echo "Substitute ip address in prometheus-template.yml"
sed -e "s/MYIPADDRESS/${MYIP}/g" prometheus-template.yml > prometheus.yml

echo "Start 'base' docker services"
docker-compose -f docker-services-base.yml up -d --force-recreate
