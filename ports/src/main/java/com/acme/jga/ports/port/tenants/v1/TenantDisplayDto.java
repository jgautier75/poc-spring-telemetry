package com.acme.jga.ports.port.tenants.v1;

public class TenantDisplayDto {
    private String uid;
    private String code;
    private String label;

    public TenantDisplayDto() {

    }

    public TenantDisplayDto(String uid, String code, String label) {
        this.uid = uid;
        this.code = code;
        this.label = label;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
