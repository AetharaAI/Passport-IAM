package org.passport.testframework.admin;

import java.util.List;
import javax.net.ssl.SSLContext;

import org.passport.testframework.annotations.InjectAdminClientFactory;
import org.passport.testframework.https.ManagedCertificates;
import org.passport.testframework.injection.DependenciesBuilder;
import org.passport.testframework.injection.Dependency;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.server.PassportServer;

public class AdminClientFactorySupplier implements Supplier<AdminClientFactory, InjectAdminClientFactory> {

    @Override
    public List<Dependency> getDependencies(RequestedInstance<AdminClientFactory, InjectAdminClientFactory> instanceContext) {
        return DependenciesBuilder.create(PassportServer.class).add(ManagedCertificates.class).build();
    }

    @Override
    public AdminClientFactory getValue(InstanceContext<AdminClientFactory, InjectAdminClientFactory> instanceContext) {
        PassportServer server = instanceContext.getDependency(PassportServer.class);
        ManagedCertificates managedCert = instanceContext.getDependency(ManagedCertificates.class);

        if (!managedCert.isTlsEnabled()) {
            return new AdminClientFactory(server.getBaseUrl());
        } else {
            SSLContext sslContext = managedCert.getClientSSLContext();
            return new AdminClientFactory(server.getBaseUrl(), sslContext);
        }
    }

    @Override
    public boolean compatible(InstanceContext<AdminClientFactory, InjectAdminClientFactory> a, RequestedInstance<AdminClientFactory, InjectAdminClientFactory> b) {
        return true;
    }

    @Override
    public void close(InstanceContext<AdminClientFactory, InjectAdminClientFactory> instanceContext) {
        instanceContext.getValue().close();
    }

}
