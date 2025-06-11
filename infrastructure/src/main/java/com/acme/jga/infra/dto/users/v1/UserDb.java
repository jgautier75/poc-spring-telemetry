package com.acme.jga.infra.dto.users.v1;

import com.acme.jga.domain.model.api.IVersioned;
import com.acme.jga.domain.model.api.MainApiVersion;
import com.acme.jga.domain.model.v1.UserStatus;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class UserDb implements IVersioned {
	private Long id;
	private String uid;
	private Long tenantId;
	private Long orgId;
	private String login;
	private String firstName;
	private String lastName;
	private String middleName;
	private String email;
	private UserStatus status;
	private String secrets;

	@Override
	public MainApiVersion getVersion() {
		return MainApiVersion.V1;
	}

}
