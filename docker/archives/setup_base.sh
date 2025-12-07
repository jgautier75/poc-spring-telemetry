#!/bin/bash

MYIP=$(hostname -I | awk '{print $1}')

#echo "Copying spi dependencies in poc-libs directory mounted on /opt/keycloak/lib/lib/main"
#cp  ~/.m2/repository/org/apache/kafka/kafka-clients/4.1.1/kafka-clients-4.1.1.jar poc-libs/kafka-clients-4.1.1.jar

echo "Copying spi-user-storage in poc-providers"
cp ../spi-user-storage/target/spi-user-storage-1.0.0-SNAPSHOT.jar poc-providers/spi-user-federation.jar

echo "Copying spi-user-storage in poc-providers"
cp ../spi-kafka/target/spi-kafka-1.0.0-SNAPSHOT.jar poc-providers/spi-kafka.jar

echo "Current IPV4 address: ${MYIP}"
echo "Substitute ip address in docker-services.yml"
sed -e "s/MYIPADDRESS/${MYIP}/g" docker-services.yml > docker-services-base.yml

echo "Substitute ip address in prometheus-template.yml"
sed -e "s/MYIPADDRESS/${MYIP}/g" prometheus-template.yml > prometheus.yml

echo "Start 'base' docker services"
docker compose -f docker-services-base.yml up -d --force-recreate
