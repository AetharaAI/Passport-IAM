package org.passport.tests.clustering;

import org.passport.testframework.annotations.InjectTestDatabase;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.conditions.DisabledForDatabases;
import org.passport.testframework.database.DatabaseConfig;
import org.passport.testframework.database.DatabaseConfigBuilder;
import org.passport.testframework.database.TestDatabase;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.server.PassportServerConfig;
import org.passport.testframework.server.PassportServerConfigBuilder;

import org.junit.jupiter.api.Test;

@PassportIntegrationTest(config = JdbcPingCustomSchemaTest.JdbcPingCustomSchemaServerConfig.class)
@DisabledForDatabases({"mariadb", "mssql", "mysql", "oracle", "tidb"})
public class JdbcPingCustomSchemaTest {

    @InjectTestDatabase(config = JdbcPingCustomSchemaDatabaseConfig.class, lifecycle = LifeCycle.CLASS)
    TestDatabase db;

    @Test
    public void testClusterFormed() {
        // no-op ClusteredPassportServer will fail if a cluster is not formed
    }

    public static class JdbcPingCustomSchemaServerConfig implements PassportServerConfig {
        @Override
        public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
            return config.option("db-schema", "PASSPORT");
        }
    }

    public static class JdbcPingCustomSchemaDatabaseConfig implements DatabaseConfig {
        @Override
        public DatabaseConfigBuilder configure(DatabaseConfigBuilder database) {
            database.initScript("org/passport/tests/clustering/case-sensitive-schema-postgres.sql");
            return database;
        }
    }
}
