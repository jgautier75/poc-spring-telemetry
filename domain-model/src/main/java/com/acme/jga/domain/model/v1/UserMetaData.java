package com.acme.jga.domain.model.v1;

import com.acme.jga.domain.model.utils.DataType;
import com.acme.jga.domain.model.utils.KeyValuePair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum UserMetaData {
    UID("uid", "uid", DataType.STRING),
    UUID("uuid", "uid", DataType.STRING),
    FIRST_NAME("firstName", "first_name", DataType.STRING),
    LAST_NAME("lastName", "last_name", DataType.STRING),
    LOGIN("login", "login", DataType.STRING),
    EMAIL("email", "email", DataType.STRING);

    private final String alias;
    private final String columnName;
    private final DataType dataType;

    UserMetaData(String alias, String columnName, DataType dataType) {
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
        Arrays.stream(UserMetaData.values()).forEach(userMetaData -> {
            colsByAlias.put(userMetaData.getAlias(), new KeyValuePair(userMetaData.getColumnName(), userMetaData.getDataType().name()));
        });
        return colsByAlias;
    }

}
