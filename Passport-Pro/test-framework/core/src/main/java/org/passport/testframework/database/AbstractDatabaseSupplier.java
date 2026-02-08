package org.passport.testframework.database;

import org.passport.testframework.annotations.InjectTestDatabase;
import org.passport.testframework.config.Config;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.injection.SupplierHelpers;
import org.passport.testframework.injection.SupplierOrder;
import org.passport.testframework.server.PassportServer;
import org.passport.testframework.server.PassportServerConfigBuilder;
import org.passport.testframework.server.PassportServerConfigInterceptor;

public abstract class AbstractDatabaseSupplier implements Supplier<TestDatabase, InjectTestDatabase>, PassportServerConfigInterceptor<TestDatabase, InjectTestDatabase> {

    @Override
    public TestDatabase getValue(InstanceContext<TestDatabase, InjectTestDatabase> instanceContext) {
        DatabaseConfigBuilder builder = DatabaseConfigBuilder
              .create()
              .preventReuse(instanceContext.getLifeCycle() != LifeCycle.GLOBAL);

        DatabaseConfig config = SupplierHelpers.getInstance(instanceContext.getAnnotation().config());
        builder = config.configure(builder);

        TestDatabase testDatabase = getTestDatabase();
        testDatabase.start(builder.build());
        return testDatabase;
    }

    @Override
    public boolean compatible(InstanceContext<TestDatabase, InjectTestDatabase> a, RequestedInstance<TestDatabase, InjectTestDatabase> b) {
        return a.getAnnotation().config().equals(b.getAnnotation().config());
    }

    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    abstract TestDatabase getTestDatabase();

    @Override
    public void close(InstanceContext<TestDatabase, InjectTestDatabase> instanceContext) {
        instanceContext.getValue().stop();
    }

    @Override
    public PassportServerConfigBuilder intercept(PassportServerConfigBuilder serverConfig, InstanceContext<TestDatabase, InjectTestDatabase> instanceContext) {
        String kcServerType = Config.getSelectedSupplier(PassportServer.class);
        TestDatabase database = instanceContext.getValue();

        // If both PassportServer and TestDatabase run in container, we need to configure Passport with internal
        // url that is accessible within docker network
        if ("cluster".equals(kcServerType) &&
                database instanceof AbstractContainerTestDatabase containerDatabase) {
            return serverConfig.options(containerDatabase.serverConfig(true));
        }

        return serverConfig.options(database.serverConfig());
    }

    @Override
    public int order() {
        return SupplierOrder.BEFORE_PASSPORT_SERVER;
    }
}
