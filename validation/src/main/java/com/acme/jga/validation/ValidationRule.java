package com.acme.jga.validation;

/**
 * Validation rule.
 */
public enum ValidationRule {
    /**
     * Field cannot be empty.
     */
    NOT_EMPTY,
    /**
     * Field cannot be null.
     */
    NOT_NULL,
    /**
     * List cannot be null or empty.
     */
    LIST_NOT_NULL_NOT_EMPTY,
    /**
     * Email is invalid.
     */
    EMAIL,
    /**
     * Payload cannot be null.
     */
    PAYLOAD,
    /**
     * Country code is not a valid iso code.
     */
    COUNTRY_ISO,
    /**
     * Field size invalid (outside expected range).
     */
    LENGTH;
}
