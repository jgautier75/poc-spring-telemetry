FROM quay.io/keycloak/keycloak:26.4.7 as builder
ARG SPI_FEDERATION_FILE
ARG SPI_KAFKA_FILE
ENV KC_DB=postgres
COPY ["setupFederation.sh","/opt/keycloak/bin"]
COPY ${SPI_FEDERATION_FILE} /opt/keycloak/providers/spi-federation.jar
COPY ${SPI_KAFKA_FILE} /opt/keycloak/providers/spi-kafka.jar
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:26.4.7
COPY --from=builder /opt/keycloak/ /opt/keycloak/
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]