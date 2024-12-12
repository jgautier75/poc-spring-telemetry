package com.acme.jga.ports.port.organizations.v1;

import com.acme.jga.domain.model.api.MainApiVersion;
import com.acme.jga.domain.model.api.IVersioned;
import com.acme.jga.domain.model.v1.OrganizationKind;
import com.acme.jga.domain.model.v1.OrganizationStatus;
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
public class OrganizationLightDto implements Serializable, IVersioned {
	@Serial
	private static final long serialVersionUID = 1877592832111032724L;
	private String uid;
	private String label;
	private String code;
	private OrganizationKind kind;
	private OrganizationStatus status;

	@Override
	public MainApiVersion getVersion() {
		return MainApiVersion.V1;
	}

}
