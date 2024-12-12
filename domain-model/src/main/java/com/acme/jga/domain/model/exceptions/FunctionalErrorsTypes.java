package com.acme.jga.domain.model.exceptions;

public enum FunctionalErrorsTypes {
    /**
     * Tenant does not exist by uid.
     */
    TENANT_NOT_FOUND,
    /**
     * Tenant already exist by code.
     */
    TENANT_CODE_ALREADY_USED,
    /**
     * Organization does not exist by uid.
     */
    ORG_NOT_FOUND,
    /**
     * Organization code already used.
     */
    ORG_CODE_ALREADY_USED,
    /**
     * User email already used.
     */
    USER_EMAIL_ALREADY_USED,
    /**
     * User log already used.
     */
    USER_LOGIN_ALREADY_USED,
    /**
     * User not found.
     */
    USER_NOT_FOUND,
    /**
     * Tenant id expected when filtering on an organization.
     */
    TENANT_ORG_EXPECTED,
    /**
     * Sector not found.
     */
    SECTOR_NOT_FOUND,
    /**
     * Sector code already used.
     */
    SECTOR_CODE_ALREADY_USED,
    /**
     * Root sector cannot be deleted.
     */
    SECTOR_ROOT_DELETE_NOT_ALLOWED,

    INVALID_PROPERTY;
}
