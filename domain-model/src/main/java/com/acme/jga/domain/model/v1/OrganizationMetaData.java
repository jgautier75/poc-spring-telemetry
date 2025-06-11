package com.acme.jga.domain.model.v1;

import com.acme.jga.domain.model.utils.DataType;
import com.acme.jga.domain.model.utils.KeyValuePair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum OrganizationMetaData {
    UID("uid", "uid", DataType.STRING),
    UUID("uuid", "uid", DataType.STRING),
    LABEL("label", "label", DataType.STRING),
    CODE("code", "code", DataType.STRING),
    COUNTRY("country", "country", DataType.STRING),
    KIND("kind", "kind", DataType.ENUM_NUMBER);

    private final String alias;
    private final String columnName;
    private final DataType dataType;

    OrganizationMetaData(String alias, String columnName, DataType dataType) {
        this.alias = alias;
        this.columnName = columnName;
        this.dataType = dataType;
    }

    public DataType getDataType() {
        return dataType;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getAlias() {
        return alias;
    }

    public static Map<String, KeyValuePair> columnsByAlias() {
        final Map<String, KeyValuePair> colsByAlias = new HashMap<>();
        Arrays.stream(OrganizationMetaData.values()).forEach(userMetaData -> colsByAlias.put(userMetaData.getAlias(), new KeyValuePair(userMetaData.getColumnName(), userMetaData.getDataType().name())));
        return colsByAlias;
    }
}
