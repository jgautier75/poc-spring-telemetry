package com.acme.jga.infra.dao.extractors;

import com.acme.jga.domain.model.v1.UserStatus;
import com.acme.jga.infra.dto.users.v1.UserDisplayDb;
import com.acme.jga.jdbc.utils.DaoConstants;
import com.acme.jga.jdbc.utils.SQLExtractor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UsersDisplayDbExtractor {
    public static UserDisplayDb extractUser(ResultSet resultSet, boolean checkNext) throws SQLException {
        UserDisplayDb userDb = null;
        if (!checkNext || resultSet.next()) {
            userDb = UserDisplayDb.builder()
                    .email(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_EMAIL))
                    .firstName(SQLExtractor.extractString(resultSet, "first_name"))
                    .lastName(SQLExtractor.extractString(resultSet, "last_name"))
                    .login(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_LOGIN))
                    .status(UserStatus.fromIntValue(SQLExtractor.extractInteger(resultSet, DaoConstants.FIELD_STATUS)))
                    .uid(SQLExtractor.extractString(resultSet, DaoConstants.FIELD_UID))
                    .build();
        }
        return userDb;
    }
}
