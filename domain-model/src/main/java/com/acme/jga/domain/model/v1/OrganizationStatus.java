package com.acme.jga.domain.model.v1;

public enum OrganizationStatus {
	DRAFT("draft", 0), ACTIVE("active", 1), INACTIVE("inactive", 2);

	private final String label;
	private final Integer code;

	OrganizationStatus(String label, Integer code) {
		this.label = label;
		this.code = code;
	}

	public String getLabel() {
		return this.label;
	}

	public Integer getCode() {
		return this.code;
	}

	public static OrganizationStatus fromIntValue(Integer aValue) {
		if (aValue != null && aValue == 0) {
			return OrganizationStatus.DRAFT;
		} else if (aValue != null && aValue == 1) {
			return OrganizationStatus.ACTIVE;
		} else if (aValue != null && aValue == 2) {
			return OrganizationStatus.INACTIVE;
		} else {
			throw new IllegalArgumentException(
					"Unknown organization status value [" + (aValue != null ? aValue : "null") + "]");
		}
	}

}
