package org.passport.testframework.clustering;

import java.util.List;

import org.passport.testframework.annotations.InjectLoadBalancer;
import org.passport.testframework.injection.DependenciesBuilder;
import org.passport.testframework.injection.Dependency;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.injection.SupplierOrder;
import org.passport.testframework.server.ClusteredPassportServer;
import org.passport.testframework.server.PassportServer;
import org.passport.testframework.server.PassportServerConfigBuilder;
import org.passport.testframework.server.PassportServerConfigInterceptor;

public class LoadBalancerSupplier implements Supplier<LoadBalancer, InjectLoadBalancer>, PassportServerConfigInterceptor<LoadBalancer, InjectLoadBalancer> {

    @Override
    public LoadBalancer getValue(InstanceContext<LoadBalancer, InjectLoadBalancer> instanceContext) {
        PassportServer server = instanceContext.getDependency(PassportServer.class);

        if (server instanceof ClusteredPassportServer clusteredPassportServer) {
            return new LoadBalancer(clusteredPassportServer);
        }

        throw new IllegalStateException("Load balancer can only be used with ClusteredPassportServer");
    }

    @Override
    public void close(InstanceContext<LoadBalancer, InjectLoadBalancer> instanceContext) {
        instanceContext.getValue().close();
    }

    @Override
    public boolean compatible(InstanceContext<LoadBalancer, InjectLoadBalancer> a, RequestedInstance<LoadBalancer, InjectLoadBalancer> b) {
        return true;
    }

    @Override
    public int order() {
        return SupplierOrder.BEFORE_REALM;
    }

    @Override
    public PassportServerConfigBuilder intercept(PassportServerConfigBuilder serverConfig, InstanceContext<LoadBalancer, InjectLoadBalancer> instanceContext) {
        return serverConfig.option("hostname", LoadBalancer.HOSTNAME);
    }

    @Override
    public List<Dependency> getDependencies(RequestedInstance<LoadBalancer, InjectLoadBalancer> instanceContext) {
        return DependenciesBuilder.create(PassportServer.class).build();
    }
}
