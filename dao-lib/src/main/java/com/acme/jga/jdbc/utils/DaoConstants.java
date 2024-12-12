package com.acme.jga.jdbc.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DaoConstants {
    public static final String FIELD_ID = "id";
    public static final String FIELD_UID = "uid";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_LOGIN = "login";
    public static final String FIELD_CODE = "code";
    public static final String FIELD_LABEL = "label";
    public static final String FIELD_TENANT_ID = "tenant_id";
    public static final String FIELD_ORG_ID = "org_id";
    public static final String FIELD_PARENT_ID = "parent_id";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_SECRETS = "secrets";
    public static final String P_ID = "pId";
    public static final String P_TENANT_ID = "pTenantId";
    public static final String P_ORG_ID = "pOrgId";
    public static final String P_UID = "pUid";
    public static final String P_CODE = "pCode";
    public static final String P_LABEL = "pLabel";
    public static final String P_LOGIN = "pLogin";
    public static final String P_EMAIL = "pEmail";
    public static final String P_FIRST_NAME = "pFirstName";
    public static final String P_LAST_NAME = "pLastName";
    public static final String P_MIDDLE_NAME = "pMiddleName";
    public static final String P_KIND = "pKind";
    public static final String P_COUNTRY = "pCountry";
    public static final String P_STATUS = "pStatus";
    public static final String P_PARENT_ID = "pParentId";
    public static final String P_SECRETS = "pSecrets";
    public static final String ORDER_BY = " order by ";
    public static final String ORDER_ASC_SIGN = "+";
    public static final String ORDER_DESC_SIGN = "-";
    public static final String ORDER_ASC_KEYWORD = " asc";
    public static final String ORDER_DESC_KEYWORD = " desc";
    public static final String PAGINATION_PATTERN = " limit %s  offset %s";
    public static final Integer DEFAULT_PAGE_SIZE = 10;
    public static final String WHERE_CLAUSE = " where ";
    public static final String AND = " and ";

    public static String generatedUUID() {
        return UUID.randomUUID().toString();
    }
}
