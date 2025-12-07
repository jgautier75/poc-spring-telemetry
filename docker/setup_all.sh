#!/bin/bash

set -e

MYIP=$(hostname -I | awk '{print $1}')

#echo "Copying spi dependencies in poc-libs directory mounted on /opt/keycloak/lib/lib/main"
#cp  ~/.m2/repository/org/apache/kafka/kafka-clients/4.1.1/kafka-clients-4.1.1.jar poc-libs/kafka-clients-4.1.1.jar

echo "Copying spi-user-storage in poc-providers"
cp ../spi-user-storage/target/spi-user-storage-1.0.0-SNAPSHOT.jar poc-providers/spi-user-federation.jar

echo "Copying spi-user-storage in poc-providers"
cp ../spi-kafka/target/spi-kafka-1.0.0-SNAPSHOT.jar poc-providers/spi-kafka.jar

echo "Current IPV4 address: ${MYIP}"
echo "Substitute ip address in docker-services.yml"
sed -e "s/MYIPADDRESS/${MYIP}/g" all_stack.yml > docker-services-base.yml

echo "Substitute ip address in prometheus-template.yml"
sed -e "s/MYIPADDRESS/${MYIP}/g" prometheus-template.yml > prometheus.yml

echo "Start 'base' docker services"
docker compose -f docker-services-base.yml --env-file app.env up -d

echo "Checking keycloak health"
STATUS=$(curl -L http://localhost:9000/health/ready | jq '.status' | sed -e "s/\"//g")
echo "Keycloak health status: [$STATUS]"
COUNTER=0;
while [ "$STATUS" != "UP" ]
 do

  COUNTER=$(($COUNTER+1));
  sleep 2s;
  STATUS=$(curl -L http://localhost:9000/health/ready | jq '.status' | sed -e "s/\"//g")
  echo "Keycloak health status: [$STATUS] / Check [$COUNTER]"
  if [ "$STATUS" == "UP" ] || [ "$COUNTER" -gt  10 ]; then
    break;
  fi
done
if [ "$STATUS" == "UP" ]; then
 docker exec -it $(docker ps --filter "label=com.acme.jga.pst=pst-keycloak" -q) sh -c /opt/keycloak/bin/setupFederation.sh
fi
