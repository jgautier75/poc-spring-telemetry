package com.acme.jga.ports.port.organizations.v1;

import com.acme.jga.domain.model.api.MainApiVersion;
import com.acme.jga.domain.model.api.IVersioned;
import com.acme.jga.ports.port.sectors.v1.SectorDisplayDto;
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
public class OrganizationDto implements Serializable, IVersioned {
	@Serial
	private static final long serialVersionUID = -9068431715296608638L;
	private Long id;
	private String uid;
	private String tenantUid;
	private OrganizationCommonsDto commons;
	private SectorDisplayDto sectors;

	@Override
	public MainApiVersion getVersion() {
		return MainApiVersion.V1;
	}
}
