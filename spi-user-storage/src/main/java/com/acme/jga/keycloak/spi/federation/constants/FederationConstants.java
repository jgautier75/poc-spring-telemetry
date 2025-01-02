package com.acme.jga.keycloak.spi.federation.constants;

public class FederationConstants {
    public static final String ENDPOINT = "federation_endpoint";
    public static final String USER = "federation_user";
    public static final String PASSWORD = "federation_pass";
    public static final String SPI_NAME = "federation-spi";
    public static final String VAULT_ADDRESS = "vault-address";
    public static final String VAULT_TOKEN = "vault-token";
    public static final Integer VAULT_VERSION = 2;
    public static final String VAULT_PATH = "vault-path";
    public static final String VAULT_SECRETS = "vault-secrets";
    public static final String VAULT_CIPHER_KEY = "cipherKey";

    private FederationConstants() {
        // Private constructor for utility class
    }

    public enum SearchFilterField {
        EMAIL,
        LOGIN,
        UID,
    }
}
