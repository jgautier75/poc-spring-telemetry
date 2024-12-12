package com.acme.jga.infra.dao.extractors;

import com.acme.jga.domain.model.v1.UserStatus;
import com.acme.jga.infra.dto.users.v1.UserDb;
import com.acme.jga.jdbc.utils.DaoConstants;
import com.acme.jga.jdbc.utils.SQLExtractor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UsersDbExtractor {
    public static UserDb extractUser(ResultSet resultSet, boolean checkNext) throws SQLException {
        UserDb userDb = null;
        if (!checkNext || resultSet.next()) {
            userDb = UserDb.builder()
                    .email(SQLExtractor.extractString(resultSet, "email"))
                    .firstName(SQLExtractor.extractString(resultSet, "first_name"))
                    .id(SQLExtractor.extractLong(resultSet, DaoConstants.FIELD_ID))
                    .lastName(SQLExtractor.extractString(resultSet, "last_name"))
                    .login(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_LOGIN))
                    .middleName(SQLExtractor.extractString(resultSet, "middle_name"))
                    .orgId(SQLExtractor.extractLong(resultSet, DaoConstants.FIELD_ORG_ID))
                    .status(UserStatus.fromIntValue(SQLExtractor.extractInteger(resultSet, DaoConstants.FIELD_STATUS)))
                    .tenantId(SQLExtractor.extractLong(resultSet, DaoConstants.FIELD_TENANT_ID))
                    .uid(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_UID))
                    .secrets(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_SECRETS))
                    .build();
        }
        return userDb;
    }

}
