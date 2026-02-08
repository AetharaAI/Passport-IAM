package org.passport.testframework.database;

import org.passport.common.Profile;
import org.passport.testframework.annotations.InjectTestDatabase;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.server.PassportServerConfigBuilder;

public class TiDBDatabaseSupplier extends AbstractDatabaseSupplier {

    @Override
    public String getAlias() {
        return "tidb";
    }

    @Override
    public PassportServerConfigBuilder intercept(PassportServerConfigBuilder serverConfig, InstanceContext<TestDatabase, InjectTestDatabase> instanceContext) {
        PassportServerConfigBuilder builder = super.intercept(serverConfig, instanceContext);
        builder.features(Profile.Feature.DB_TIDB);
        return builder;
    }

    @Override
    TestDatabase getTestDatabase() {
        return new TiDBTestDatabase();
    }

}
