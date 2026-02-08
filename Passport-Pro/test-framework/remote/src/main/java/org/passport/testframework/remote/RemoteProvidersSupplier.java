package org.passport.testframework.remote;

import org.passport.testframework.annotations.InjectTestDatabase;
import org.passport.testframework.database.TestDatabase;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.injection.SupplierOrder;
import org.passport.testframework.server.PassportServerConfigBuilder;
import org.passport.testframework.server.PassportServerConfigInterceptor;

public class RemoteProvidersSupplier implements Supplier<RemoteProviders, InjectRemoteProviders>, PassportServerConfigInterceptor<TestDatabase, InjectTestDatabase> {

    @Override
    public RemoteProviders getValue(InstanceContext<RemoteProviders, InjectRemoteProviders> instanceContext) {
        return new RemoteProviders();
    }

    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    @Override
    public boolean compatible(InstanceContext<RemoteProviders, InjectRemoteProviders> a, RequestedInstance<RemoteProviders, InjectRemoteProviders> b) {
        return true;
    }

    @Override
    public int order() {
        return SupplierOrder.BEFORE_PASSPORT_SERVER;
    }

    @Override
    public PassportServerConfigBuilder intercept(PassportServerConfigBuilder serverConfig, InstanceContext<TestDatabase, InjectTestDatabase> instanceContext) {
        return serverConfig.dependency("org.passport.testframework", "passport-test-framework-remote-providers");
    }
}
