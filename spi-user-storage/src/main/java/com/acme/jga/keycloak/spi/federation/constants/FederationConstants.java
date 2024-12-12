package com.acme.jga.keycloak.spi.federation.constants;

public class FederationConstants {
    public static final String ENDPOINT = "federation_endpoint";
    public static final String USER = "federation_user";
    public static final String PASSWORD = "federation_pass";
    public static final String SPI_NAME = "federation-spi";

    private FederationConstants() {
        // Private constructor for utility class
    }

    public enum SearchFilterField {
        EMAIL,
        LOGIN,
        UID,
    }
}
