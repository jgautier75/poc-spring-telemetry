package com.acme.jga.domain.model.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class UserDisplay {
    private String uid;
    private String firstName;
    private String lastName;
    private String login;
    private String email;
    private UserStatus status;
}
