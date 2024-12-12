package com.acme.jga.ports.converters.user;

import com.acme.jga.domain.model.v1.User;
import com.acme.jga.domain.model.v1.UserCommons;
import com.acme.jga.domain.model.v1.UserCredentials;
import com.acme.jga.ports.port.organizations.v1.OrganizationLightDto;
import com.acme.jga.ports.port.spi.v1.UserInfosDto;
import com.acme.jga.ports.port.users.v1.UserDisplayDto;
import com.acme.jga.ports.port.users.v1.UserDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsersPortConverter {

    public User convertUserDtoToDomain(UserDto userDto) {
        User user = null;
        if (userDto != null) {
            user = new User();
            user.setUid(userDto.getUid());
            if (userDto.getCommons() != null) {
                UserCommons userCommons = UserCommons.builder()
                        .firstName(userDto.getCommons().getFirstName())
                        .lastName(userDto.getCommons().getLastName())
                        .middleName(userDto.getCommons().getMiddleName())
                        .build();
                user.setCommons(userCommons);
            }
            if (userDto.getCredentials() != null) {
                UserCredentials userCredentials = UserCredentials.builder()
                        .email(userDto.getCredentials().getEmail())
                        .login(userDto.getCredentials().getLogin())
                        .defaultPassword(userDto.getCredentials().getDefaultPassword())
                        .build();
                user.setCredentials(userCredentials);
            }
            user.setStatus(userDto.getStatus());
        }
        return user;
    }

    public UserDisplayDto convertUserDomainToDisplay(User user) {
        return Optional.ofNullable(user).map(u -> {
            UserDisplayDto displayDto = new UserDisplayDto();
            displayDto.setUid(user.getUid());
            Optional.ofNullable(user.getCommons()).ifPresent(c -> {
                displayDto.setFirstName(user.getCommons().getFirstName());
                displayDto.setLastName(user.getCommons().getLastName());
                displayDto.setMiddleName(user.getCommons().getMiddleName());
            });
            Optional.ofNullable(user.getCredentials()).ifPresent(userCredentials -> {
                displayDto.setEmail(userCredentials.getEmail());
                displayDto.setLogin(userCredentials.getLogin());
            });
            Optional.ofNullable(user.getOrganization()).ifPresent(o -> {
                OrganizationLightDto orgDto = new OrganizationLightDto();
                orgDto.setKind(user.getOrganization().getCommons().getKind());
                orgDto.setLabel(user.getOrganization().getCommons().getLabel());
                orgDto.setStatus(user.getOrganization().getCommons().getStatus());
                orgDto.setUid(user.getOrganization().getUid());
                displayDto.setOrganization(orgDto);
            });
            return displayDto;
        }).orElse(null);
    }

    public Optional<UserInfosDto> convertUserDomainToSpi(User user){
        return Optional.ofNullable(user).map(u -> {
           UserInfosDto userInfosDto = new UserInfosDto();
           userInfosDto.setEncryptedPassword(user.getCredentials().getEncryptedPassword());
           userInfosDto.setEmail(user.getCredentials().getEmail());
           userInfosDto.setLogin(user.getCredentials().getLogin());
           userInfosDto.setUid(user.getUid());
           userInfosDto.setFirstName(user.getCommons().getFirstName());
           userInfosDto.setLastName(user.getCommons().getLastName());
           return userInfosDto;
        });
    }

}
