package org.passport.tests.db;

import org.passport.testframework.annotations.InjectTestDatabase;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.conditions.DisabledForDatabases;
import org.passport.testframework.database.DatabaseConfig;
import org.passport.testframework.database.DatabaseConfigBuilder;
import org.passport.testframework.database.EnterpriseDbTestDatabase;
import org.passport.testframework.database.PostgresTestDatabase;
import org.passport.testframework.database.TestDatabase;
import org.passport.testframework.server.PassportServerConfig;
import org.passport.testframework.server.PassportServerConfigBuilder;

@PassportIntegrationTest(config = CaseSensitiveSchemaTest.CaseSensitiveServerConfig.class)
// Remotely running databases do not support running SQL init scripts.
// MSSQL does not support setting the default schema per session
// TiDb does not support setting the default schema per session.
@DisabledForDatabases({ "remote", "mssql", "tidb" })
public class CaseSensitiveSchemaTest extends AbstractDBSchemaTest {

    @InjectTestDatabase(config = CaseSensitiveDatabaseConfig.class)
    TestDatabase db;

    public static class CaseSensitiveServerConfig implements PassportServerConfig {
        @Override
        public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {

            return switch (dbType()) {
                // DBs that convert unquoted to lower-case by default
                case PostgresTestDatabase.NAME, EnterpriseDbTestDatabase.NAME
                        -> config.option("db-schema", "PASSPORT");
                // DBs that convert unquoted to upper-case by default
                case "dev-file", "dev-mem" ->
                        config.option("db-url-properties", ";INIT=CREATE SCHEMA IF NOT EXISTS passport").option("db-schema", "passport");
                default -> config.option("db-schema", "passport");
            };
        }
    }

    public static class CaseSensitiveDatabaseConfig implements DatabaseConfig {
        @Override
        public DatabaseConfigBuilder configure(DatabaseConfigBuilder database) {
            if (PostgresTestDatabase.NAME.equals(dbType()) || EnterpriseDbTestDatabase.NAME.equals(dbType())) {
                database.initScript("org/passport/tests/db/case-sensitive-schema-postgres.sql");
            }
            return database;
        }
    }
}
