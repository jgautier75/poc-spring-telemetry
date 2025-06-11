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
public class UserCommonsDto implements Serializable, IVersioned {
	@Serial
	private static final long serialVersionUID = 6536062346106161321L;
	private String firstName;
	private String lastName;
	private String middleName;

	@Override
	public MainApiVersion getVersion() {
		return MainApiVersion.V1;
	}
}
