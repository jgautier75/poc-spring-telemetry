package com.acme.jga.keycloak.spi.federation.adapters;

import com.acme.jga.keycloak.spi.federation.dtos.UserInfosDto;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

public class UserAdapter extends AbstractUserAdapterFederatedStorage {
    private final UserInfosDto userInfosDto;
    private final UserModel userModel;
    private final String federationLink;

    public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel storageProviderModel, UserInfosDto infosDto, UserModel userModel, String federationLink) {
        super(session, realm, storageProviderModel);
        this.userInfosDto = infosDto;
        this.userModel = userModel;
        this.federationLink = federationLink;
    }

    @Override
    public String getUsername() {
        if (this.userInfosDto != null) {
            return this.userInfosDto.getLogin();
        } else {
            return this.userModel.getUsername();
        }
    }

    @Override
    public void setUsername(String username) {
        if (this.userInfosDto != null) {
            this.userInfosDto.setLogin(username);
        } else {
            this.userModel.setUsername(username);
        }
    }

    @Override
    public String getFirstName() {
        if (userInfosDto != null) {
            return userInfosDto.getFirstName();
        } else {
            return userModel.getFirstName();
        }
    }

    @Override
    public void setFirstName(String firstName) {
        if (userInfosDto != null) {
            userInfosDto.setFirstName(firstName);
        } else {
            userModel.setFirstName(firstName);
        }
    }

    @Override
    public String getLastName() {
        if (userInfosDto != null) {
            return userInfosDto.getLastName();
        } else {
            return userModel.getLastName();
        }
    }

    @Override
    public void setLastName(String lastName) {
        if (userInfosDto != null) {
            userInfosDto.setLastName(lastName);
        } else {
            userModel.setLastName(lastName);
        }
    }

    @Override
    public String getEmail() {
        if (userInfosDto != null) {
            return userInfosDto.getEmail();
        } else {
            return userModel.getEmail();
        }
    }

    @Override
    public void setEmail(String email) {
        if (userInfosDto != null) {
            userInfosDto.setEmail(email);
        } else {
            userModel.setEmail(email);
        }
    }

    @Override
    public String getId() {
        if (userInfosDto != null) {
            return userInfosDto.getUid();
        } else {
            return StorageId.externalId(userModel.getId());
        }
    }

    @Override
    public String getFederationLink() {
        return this.federationLink;
    }
}
