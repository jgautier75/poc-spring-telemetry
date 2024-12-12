package com.acme.jga.keycloak.spi.federation.factory;

import com.acme.jga.keycloak.spi.federation.constants.FederationConstants;
import com.acme.jga.keycloak.spi.federation.provider.UserSpiProvider;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

public class UserSpiFactory implements UserStorageProviderFactory<UserSpiProvider> {

    @Override
    public UserSpiProvider create(KeycloakSession keycloakSession, ComponentModel componentModel) {
        return new UserSpiProvider(componentModel, keycloakSession);
    }

    @Override
    public String getId() {
        return FederationConstants.SPI_NAME;
    }

}
