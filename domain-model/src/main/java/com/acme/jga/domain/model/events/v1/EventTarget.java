package com.acme.jga.domain.model.events.v1;

public enum EventTarget {
    TENANT(0),
    ORGANIZATION(1),
    USER(2),
    SECTOR(3);

    private final Integer value;

    EventTarget(Integer aValue) {
        this.value = aValue;
    }

    public Integer getValue() {
        return this.value;
    }

    public static EventTarget fromValue(Integer aValue) {
        if (aValue != null && aValue.equals(0)) {
            return TENANT;
        } else if (aValue != null && aValue.equals(1)) {
            return ORGANIZATION;
        } else if (aValue != null && aValue.equals(2)) {
            return USER;
        } else if (aValue != null && aValue.equals(3)) {
            return SECTOR;
        } else {
            throw new IllegalArgumentException("Unknown eventScope value [" + aValue + "]");
        }
    }

}
