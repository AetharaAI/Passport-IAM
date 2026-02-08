package org.passport.testframework.http;

import java.util.List;

import org.passport.http.simple.SimpleHttp;
import org.passport.testframework.annotations.InjectSimpleHttp;
import org.passport.testframework.injection.DependenciesBuilder;
import org.passport.testframework.injection.Dependency;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;

import org.apache.http.client.HttpClient;

public class SimpleHttpSupplier implements Supplier<SimpleHttp, InjectSimpleHttp> {

    @Override
    public List<Dependency> getDependencies(RequestedInstance<SimpleHttp, InjectSimpleHttp> instanceContext) {
        return DependenciesBuilder.create(HttpClient.class).build();
    }

    @Override
    public SimpleHttp getValue(InstanceContext<SimpleHttp, InjectSimpleHttp> instanceContext) {
        HttpClient httpClient = instanceContext.getDependency(HttpClient.class);
        return SimpleHttp.create(httpClient);
    }

    @Override
    public boolean compatible(InstanceContext<SimpleHttp, InjectSimpleHttp> a, RequestedInstance<SimpleHttp, InjectSimpleHttp> b) {
        return true;
    }

}
