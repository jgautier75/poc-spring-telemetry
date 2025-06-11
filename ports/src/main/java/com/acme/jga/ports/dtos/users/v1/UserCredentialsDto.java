package com.acme.jga.ports.dtos.users.v1;

import com.acme.jga.domain.model.api.IVersioned;
import com.acme.jga.domain.model.api.MainApiVersion;
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
public class UserCredentialsDto implements Serializable, IVersioned {
	@Serial
	private static final long serialVersionUID = -7399495930708950049L;
	private String login;
	private String email;
	private String defaultPassword;

	@Override
	public MainApiVersion getVersion() {
		return MainApiVersion.V1;
	}
}
