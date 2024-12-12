package com.acme.jga.infra.dao.extractors;

import com.acme.jga.infra.dto.tenants.v1.TenantDb;
import com.acme.jga.jdbc.utils.DaoConstants;
import com.acme.jga.jdbc.utils.SQLExtractor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TenantsDbExtractor {
    public static TenantDb extractTenant(ResultSet resultSet, boolean checkNext) throws SQLException {
        TenantDb tenant = null;
        if (!checkNext || resultSet.next()) {
            tenant = TenantDb.builder()
                    .id(SQLExtractor.extractLong(resultSet, DaoConstants.FIELD_ID))
                    .uid(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_UID))
                    .code(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_CODE))
                    .label(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_LABEL))
                    .build();
        }
        return tenant;
    }

}
