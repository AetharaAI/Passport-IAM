package org.passport.testframework.server;

import java.util.List;

import org.passport.testframework.annotations.InjectPassportUrls;
import org.passport.testframework.injection.DependenciesBuilder;
import org.passport.testframework.injection.Dependency;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;

public class PassportUrlsSupplier implements Supplier<PassportUrls, InjectPassportUrls> {

    @Override
    public List<Dependency> getDependencies(RequestedInstance<PassportUrls, InjectPassportUrls> instanceContext) {
        return DependenciesBuilder.create(PassportServer.class).build();
    }

    @Override
    public PassportUrls getValue(InstanceContext<PassportUrls, InjectPassportUrls> instanceContext) {
        PassportServer server = instanceContext.getDependency(PassportServer.class);
        return new PassportUrls(server.getBaseUrl(), server.getManagementBaseUrl());
    }

    @Override
    public boolean compatible(InstanceContext<PassportUrls, InjectPassportUrls> a, RequestedInstance<PassportUrls, InjectPassportUrls> b) {
        return true;
    }
}
