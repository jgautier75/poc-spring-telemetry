package com.acme.jga.domain.model.v1;

public enum UserStatus {

	DRAFT("draft", 0), ACTIVE("active", 1), INACTIVE("suspended", 2);

	private final String label;
	private final Integer code;

	UserStatus(String label, Integer code) {
		this.label = label;
		this.code = code;
	}

	public String getLabel() {
		return this.label;
	}

	public Integer getCode() {
		return this.code;
	}

	public static UserStatus fromIntValue(Integer aValue) {
		if (aValue != null && aValue == 0) {
			return UserStatus.DRAFT;
		} else if (aValue != null && aValue == 1) {
			return UserStatus.ACTIVE;
		} else if (aValue != null && aValue == 2) {
			return UserStatus.INACTIVE;
		} else {
			throw new IllegalArgumentException(
					"Unknown user status value [" + (aValue != null ? aValue : "null") + "]");
		}
	}

}
