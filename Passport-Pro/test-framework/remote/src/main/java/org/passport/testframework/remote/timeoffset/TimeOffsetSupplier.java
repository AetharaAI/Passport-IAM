package org.passport.testframework.remote.timeoffset;

import java.util.List;

import org.passport.testframework.injection.DependenciesBuilder;
import org.passport.testframework.injection.Dependency;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.injection.SupplierOrder;
import org.passport.testframework.remote.RemoteProviders;
import org.passport.testframework.server.PassportUrls;

import org.apache.http.client.HttpClient;

public class TimeOffsetSupplier implements Supplier<TimeOffSet, InjectTimeOffSet> {

    @Override
    public List<Dependency> getDependencies(RequestedInstance<TimeOffSet, InjectTimeOffSet> instanceContext) {
        return DependenciesBuilder.create(HttpClient.class)
                .add(RemoteProviders.class).add(PassportUrls.class).build();
    }

    @Override
    public TimeOffSet getValue(InstanceContext<TimeOffSet, InjectTimeOffSet> instanceContext) {
        var httpClient = instanceContext.getDependency(HttpClient.class);
        var remoteProviders = instanceContext.getDependency(RemoteProviders.class);
        PassportUrls passportUrls = instanceContext.getDependency(PassportUrls.class);

        int initOffset = instanceContext.getAnnotation().offset();
        return new TimeOffSet(httpClient, passportUrls.getMasterRealm(), initOffset);
    }

    @Override
    public boolean compatible(InstanceContext<TimeOffSet, InjectTimeOffSet> a, RequestedInstance<TimeOffSet, InjectTimeOffSet> b) {
        return true;
    }

    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.METHOD;
    }

    @Override
    public void close(InstanceContext<TimeOffSet, InjectTimeOffSet> instanceContext) {
        TimeOffSet timeOffSet = instanceContext.getValue();
        if (timeOffSet.hasChanged()) {
            timeOffSet.set(0);
        }
    }

    @Override
    public int order() {
        return SupplierOrder.BEFORE_PASSPORT_SERVER;
    }

}
