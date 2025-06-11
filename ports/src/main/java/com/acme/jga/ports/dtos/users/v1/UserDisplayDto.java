package com.acme.jga.ports.dtos.users.v1;

import com.acme.jga.domain.model.v1.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class UserDisplayDto {
    private String uid;
    private String login;
    private String email;
    private String firstName;
    private String lastName;
    private UserStatus status;
}
