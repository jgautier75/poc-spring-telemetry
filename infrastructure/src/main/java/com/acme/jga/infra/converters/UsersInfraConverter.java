package com.acme.jga.infra.converters;

import com.acme.jga.domain.model.v1.User;
import com.acme.jga.domain.model.v1.UserCommons;
import com.acme.jga.domain.model.v1.UserCredentials;
import com.acme.jga.infra.dto.users.v1.UserDb;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsersInfraConverter {

    public UserDb convertUserToDb(User user) {
        return UserDb.builder()
                .email(user.getCredentials().getEmail())
                .firstName(user.getCommons().getFirstName())
                .lastName(user.getCommons().getLastName())
                .login(user.getCredentials().getLogin())
                .middleName(user.getCommons().getMiddleName())
                .orgId(user.getOrganizationId())
                .status(user.getStatus())
                .tenantId(user.getTenantId())
                .id(user.getId())
                .secrets(user.getCredentials().getEncryptedPassword())
                .build();
    }

    public User convertUserDbToUser(UserDb userDb) {
        return Optional.ofNullable(userDb).map(u -> {
            UserCommons userCommons = UserCommons.builder()
                    .firstName(userDb.getFirstName())
                    .lastName(userDb.getLastName())
                    .middleName(userDb.getMiddleName())
                    .build();
            UserCredentials userCredentials = UserCredentials.builder()
                    .email(userDb.getEmail())
                    .login(userDb.getLogin())
                    .encryptedPassword(userDb.getSecrets())
                    .build();
            return User.builder()
                    .commons(userCommons)
                    .credentials(userCredentials)
                    .id(userDb.getId())
                    .organizationId(userDb.getOrgId())
                    .status(userDb.getStatus())
                    .tenantId(userDb.getTenantId())
                    .uid(userDb.getUid())
                    .build();
        }).orElse(null);
    }

}
