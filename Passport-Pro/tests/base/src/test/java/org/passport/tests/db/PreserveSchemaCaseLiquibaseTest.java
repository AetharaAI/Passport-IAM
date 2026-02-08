package org.passport.tests.db;

import org.passport.testframework.annotations.InjectTestDatabase;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.conditions.DisabledForDatabases;
import org.passport.testframework.database.DatabaseConfig;
import org.passport.testframework.database.DatabaseConfigBuilder;
import org.passport.testframework.database.EnterpriseDbTestDatabase;
import org.passport.testframework.database.PostgresTestDatabase;
import org.passport.testframework.database.TestDatabase;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.server.PassportServerConfig;
import org.passport.testframework.server.PassportServerConfigBuilder;

@PassportIntegrationTest(config = PreserveSchemaCaseLiquibaseTest.PreserveSchemaCaseServerConfig.class)
// Remotely running databases do not support running SQL init scripts.
// MSSQL does not support setting the default schema per session.
// TiDb does not support setting the default schema per session.
// Oracle image does not support configuring user/databases with '-'
@DisabledForDatabases({ "remote", "mssql", "oracle", "tidb" })
public class PreserveSchemaCaseLiquibaseTest extends AbstractDBSchemaTest {

    @InjectTestDatabase(config = PreserveSchemaCaseDatabaseConfig.class, lifecycle = LifeCycle.CLASS)
    TestDatabase db;

    public static class PreserveSchemaCaseServerConfig implements PassportServerConfig {
        @Override
        public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
            switch (dbType()) {
                case "dev-file":
                case "dev-mem":
                    config.option("db-url-properties", ";INIT=CREATE SCHEMA IF NOT EXISTS \"passport-t\"");
            }
            return config.option("db-schema", "passport-t");
        }
    }

    private static class PreserveSchemaCaseDatabaseConfig implements DatabaseConfig {
        @Override
        public DatabaseConfigBuilder configure(DatabaseConfigBuilder database) {
            if (dbType().equals(PostgresTestDatabase.NAME) || dbType().equals(EnterpriseDbTestDatabase.NAME)) {
                return database.initScript("org/passport/tests/db/preserve-schema-case-liquibase-postgres.sql");
            }
            return database.database("passport-t");
        }
    }
}
