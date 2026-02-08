package org.passport.testframework.remote.runonserver;

import java.util.List;

import org.passport.testframework.injection.DependenciesBuilder;
import org.passport.testframework.injection.Dependency;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;

import com.sun.net.httpserver.HttpServer;

public class TestClassServerSupplier implements Supplier<TestClassServer, InjectTestClassServer> {

    @Override
    public List<Dependency> getDependencies(RequestedInstance<TestClassServer, InjectTestClassServer> instanceContext) {
        return DependenciesBuilder.create(HttpServer.class).build();
    }

    @Override
    public TestClassServer getValue(InstanceContext<TestClassServer, InjectTestClassServer> instanceContext) {
        HttpServer httpServer = instanceContext.getDependency(HttpServer.class);
        return new TestClassServer(httpServer);
    }

    @Override
    public boolean compatible(InstanceContext<TestClassServer, InjectTestClassServer> a, RequestedInstance<TestClassServer, InjectTestClassServer> b) {
        return true;
    }

    @Override
    public void close(InstanceContext<TestClassServer, InjectTestClassServer> instanceContext) {
        instanceContext.getValue().close();
    }
}
