package org.passport.testframework.remote.runonserver;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.passport.testframework.injection.DependenciesBuilder;
import org.passport.testframework.injection.Dependency;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.injection.SupplierOrder;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.remote.RemoteProviders;
import org.passport.testframework.server.PassportServer;

import org.apache.http.client.HttpClient;

public class RunOnServerSupplier implements Supplier<RunOnServerClient, InjectRunOnServer> {

    @Override
    public List<Dependency> getDependencies(RequestedInstance<RunOnServerClient, InjectRunOnServer> instanceContext) {
        return DependenciesBuilder.create(HttpClient.class)
                .add(PassportServer.class)
                .add(ManagedRealm.class, instanceContext.getAnnotation().realmRef())
                .add(RemoteProviders.class)
                .add(TestClassServer.class).build();
    }

    @Override
    public RunOnServerClient getValue(InstanceContext<RunOnServerClient, InjectRunOnServer> instanceContext) {
        PassportServer server = instanceContext.getDependency(PassportServer.class);

        HttpClient httpClient = instanceContext.getDependency(HttpClient.class);
        ManagedRealm realm = instanceContext.getDependency(ManagedRealm.class, instanceContext.getAnnotation().realmRef());
        instanceContext.getDependency(RemoteProviders.class);

        TestClassServer testClassServer = instanceContext.getDependency(TestClassServer.class);
        String[] permittedPackages = instanceContext.getAnnotation().permittedPackages();
        testClassServer.addPermittedPackages(new HashSet<>(Arrays.asList(permittedPackages)));

        return new RunOnServerClient(httpClient, realm.getBaseUrl(), server.hashCode());
    }

    @Override
    public boolean compatible(InstanceContext<RunOnServerClient, InjectRunOnServer> a, RequestedInstance<RunOnServerClient, InjectRunOnServer> b) {
        return a.getAnnotation().realmRef().equals(b.getAnnotation().realmRef());
    }

    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.METHOD;
    }

    @Override
    public int order() {
        return SupplierOrder.BEFORE_PASSPORT_SERVER;
    }

}
