package com.acme.jga.domain.model.v1;

public enum OrganizationKind {
	TENANT("tenant", 0), BU("bu", 1), COMMUNITY("community", 2), ENTERPRISE("enterprise", 3);

	private final String label;
	private final Integer code;

	OrganizationKind(String label, Integer code) {
		this.label = label;
		this.code = code;
	}

	public String getLabel() {
		return this.label;
	}

	public Integer getCode() {
		return this.code;
	}

	public static OrganizationKind fromIntValue(Integer aValue) {
		if (aValue != null && aValue == 0) {
			return OrganizationKind.TENANT;
		} else if (aValue != null && aValue == 1) {
			return OrganizationKind.BU;
		} else if (aValue != null && aValue == 2) {
			return OrganizationKind.COMMUNITY;
		} else if (aValue != null && aValue == 3) {
			return OrganizationKind.ENTERPRISE;
		} else {
			throw new IllegalArgumentException(
					"Unknown organization kind value [" + (aValue != null ? aValue : "null") + "]");
		}
	}

}
