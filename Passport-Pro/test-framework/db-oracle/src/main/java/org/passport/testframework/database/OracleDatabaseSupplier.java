package org.passport.testframework.database;

import org.passport.testframework.annotations.InjectTestDatabase;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.server.PassportServerConfigBuilder;

public class OracleDatabaseSupplier extends AbstractDatabaseSupplier {

    @Override
    public String getAlias() {
        return OracleTestDatabase.NAME;
    }

    @Override
    TestDatabase getTestDatabase() {
        return new OracleTestDatabase();
    }

    @Override
    public PassportServerConfigBuilder intercept(PassportServerConfigBuilder serverConfig, InstanceContext<TestDatabase, InjectTestDatabase> instanceContext) {
        return super.intercept(serverConfig, instanceContext)
                .dependency("com.oracle.database.jdbc", "ojdbc17");
    }
}
