package com.acme.jga.infra.dao.utils;

import liquibase.command.CommandScope;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;

import java.sql.Connection;
import java.sql.DriverManager;

public class DaoTestUtils {

    public static void performLiquibaseUpdate(String jdbcUrl, String userName, String userPassword) throws Exception {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, userName, userPassword)) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(conn));
            CommandScope updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);
            updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
            updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "postgresql/changelogs.xml");
            updateCommand.execute();
        }
    }
}
