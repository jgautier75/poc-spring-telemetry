FROM quay.io/keycloak/keycloak:26.0 as builder
LABEL authors="jgautier"
COPY ["setupFederation.sh","/opt/keycloak/bin/"]


ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
