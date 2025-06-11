package com.acme.jga.domain.model.v1;

import com.acme.jga.domain.model.api.IVersioned;
import com.acme.jga.domain.model.api.MainApiVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.Diffable;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class Organization implements IVersioned, Serializable, Diffable<Organization> {
	@Serial
	private static final long serialVersionUID = -627071470081034100L;
	private Long tenantId;
	private Long id;
	private String uid;
	private OrganizationCommons commons;
	private Sector sector;

	@Override
	public MainApiVersion getVersion() {
		return MainApiVersion.V1;
	}

	@Override
	public DiffResult<Organization> diff(Organization obj) {
		return new DiffBuilder<>(this, obj, ToStringStyle.SHORT_PREFIX_STYLE)
       .append("commons", this.commons, obj.commons)       
       .build();
	}
}
