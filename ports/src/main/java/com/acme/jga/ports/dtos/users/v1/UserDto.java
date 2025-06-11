package com.acme.jga.ports.dtos.users.v1;

import com.acme.jga.domain.model.api.IVersioned;
import com.acme.jga.domain.model.api.MainApiVersion;
import com.acme.jga.domain.model.v1.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class UserDto implements Serializable, IVersioned {
	@Serial
	private static final long serialVersionUID = -1827189801869898632L;
	private Long id;
	private String uid;
	private UserCredentialsDto credentials;
	private UserCommonsDto commons;
	private UserStatus status;

	@Override
	public MainApiVersion getVersion() {
		return MainApiVersion.V1;
	}
}
