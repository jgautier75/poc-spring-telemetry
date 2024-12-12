package com.acme.jga.domain.model.events.v1;

public enum EventStatus {
    PENDING(0),
    PROCESSED(1),
    FAILED(2);

    private final Integer value;

    EventStatus(Integer aValue) {
        this.value = aValue;
    }

    public Integer getValue() {
        return this.value;
    }

    public static EventStatus fromValue(Integer aValue) {
        if (aValue != null && aValue.equals(0)) {
            return PENDING;
        } else if (aValue != null && aValue.equals(1)) {
            return PROCESSED;
        } else if (aValue != null && aValue.equals(3)) {
            return FAILED;
        } else {
            throw new IllegalArgumentException("Unknown eventScope value [" + aValue + "]");
        }
    }

}
