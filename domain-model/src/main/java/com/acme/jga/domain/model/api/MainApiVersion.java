package com.acme.jga.domain.model.api;

public enum MainApiVersion {

	V1("v1");

	private final String version;

	MainApiVersion(String aVersion) {
		this.version = aVersion;
	}

	public String getVersion() {
		return version;
	}

}
