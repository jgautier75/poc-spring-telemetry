package com.acme.jga.infra.dto.organizations.v1;

import com.acme.jga.domain.model.api.IVersioned;
import com.acme.jga.domain.model.api.MainApiVersion;
import com.acme.jga.domain.model.v1.OrganizationKind;
import com.acme.jga.domain.model.v1.OrganizationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class OrganizationDb implements IVersioned {
	private Long tenantId;
	private Long id;
	private String code;
	private String uid;
	private String label;
	private OrganizationKind kind;
	private String country;
	private OrganizationStatus status;

	@Override
	public MainApiVersion getVersion() {
		return MainApiVersion.V1;
	}
}
