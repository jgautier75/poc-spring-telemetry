package com.acme.jga.ports.dtos.users.v1;

import com.acme.jga.ports.dtos.organizations.v1.OrganizationLightDto;
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
    private String middleName;
    private OrganizationLightDto organization;
}
