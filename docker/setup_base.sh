#!/bin/bash

MYIP=$(hostname -I | awk '{print $1}')

echo "Current IPV4 address: ${MYIP}"
echo "Substitute ip address in docker-services.yml"
sed -e "s/MYIPADDRESS/${MYIP}/g" docker-services.yml > docker-services-base.yml

echo "Substitute ip address in prometheus-template.yml"
sed -e "s/MYIPADDRESS/${MYIP}/g" prometheus-template.yml > prometheus.yml

echo "Start 'base' docker services"
docker-compose -f docker-services-base.yml up -d
