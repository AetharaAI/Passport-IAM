package org.passport.testframework.infinispan;

import org.passport.testframework.annotations.InjectInfinispanServer;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.injection.SupplierOrder;
import org.passport.testframework.server.PassportServerConfigBuilder;
import org.passport.testframework.server.PassportServerConfigInterceptor;

import org.jboss.logging.Logger;

public class InfinispanExternalServerSupplier implements Supplier<InfinispanServer, InjectInfinispanServer>, PassportServerConfigInterceptor<InfinispanServer, InjectInfinispanServer> {

    private static final Logger LOGGER = Logger.getLogger(InfinispanExternalServerSupplier.class);

    @Override
    public InfinispanServer getValue(InstanceContext<InfinispanServer, InjectInfinispanServer> instanceContext) {
        InfinispanServer server = InfinispanExternalServer.create();
        getLogger().info("Starting Infinispan Server");

        long start = System.currentTimeMillis();

        server.start();

        getLogger().infov("Infinispan server started in {0} ms", System.currentTimeMillis() - start);
        return server;
    }

    @Override
    public void close(InstanceContext<InfinispanServer, InjectInfinispanServer> instanceContext) {
        instanceContext.getValue().stop();
    }

    @Override
    public boolean compatible(InstanceContext<InfinispanServer, InjectInfinispanServer> a, RequestedInstance<InfinispanServer, InjectInfinispanServer> b) {
        return true;
    }

    @Override
    public int order() {
        return SupplierOrder.BEFORE_PASSPORT_SERVER;
    }

    @Override
    public PassportServerConfigBuilder intercept(PassportServerConfigBuilder config, InstanceContext<InfinispanServer, InjectInfinispanServer> instanceContext) {
        InfinispanServer ispnServer = instanceContext.getValue();

        return config.options(ispnServer.serverConfig());
    }

    public Logger getLogger() {
        return LOGGER;
    }
}
