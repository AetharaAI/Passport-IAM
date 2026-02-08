package org.passport.testframework.oauth;

import java.util.List;

import org.passport.testframework.injection.DependenciesBuilder;
import org.passport.testframework.injection.Dependency;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.oauth.annotations.InjectTestApp;

import com.sun.net.httpserver.HttpServer;

public class TestAppSupplier implements Supplier<TestApp, InjectTestApp> {

    @Override
    public List<Dependency> getDependencies(RequestedInstance<TestApp, InjectTestApp> instanceContext) {
        return DependenciesBuilder.create(HttpServer.class).build();
    }

    @Override
    public TestApp getValue(InstanceContext<TestApp, InjectTestApp> instanceContext) {
        HttpServer httpServer = instanceContext.getDependency(HttpServer.class);
        return new TestApp(httpServer);
    }

    @Override
    public boolean compatible(InstanceContext<TestApp, InjectTestApp> a, RequestedInstance<TestApp, InjectTestApp> b) {
        return true;
    }

    @Override
    public void close(InstanceContext<TestApp, InjectTestApp> instanceContext) {
        instanceContext.getValue().close();
    }

}
