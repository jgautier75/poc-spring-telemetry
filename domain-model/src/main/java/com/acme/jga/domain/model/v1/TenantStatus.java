package com.acme.jga.domain.model.v1;

public enum TenantStatus {
	ACTIVE("active", 1), INACTIVE("inactive", 2);

	private final String label;
	private final Integer code;

	TenantStatus(String label, Integer code) {
		this.label = label;
		this.code = code;
	}

	public String getLabel() {
		return this.label;
	}

	public Integer getCode() {
		return this.code;
	}
}
