package org.passport.testframework.server;

import java.util.List;

import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.config.Config;
import org.passport.testframework.database.TestDatabase;
import org.passport.testframework.https.ManagedCertificates;
import org.passport.testframework.infinispan.InfinispanServer;
import org.passport.testframework.injection.AbstractInterceptorHelper;
import org.passport.testframework.injection.DependenciesBuilder;
import org.passport.testframework.injection.Dependency;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.injection.Registry;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.injection.SupplierHelpers;
import org.passport.testframework.injection.SupplierOrder;

import org.jboss.logging.Logger;

public abstract class AbstractPassportServerSupplier implements Supplier<PassportServer, PassportIntegrationTest> {

    @Override
    public List<Dependency> getDependencies(RequestedInstance<PassportServer, PassportIntegrationTest> instanceContext) {
        PassportServerConfigBuilder command = getPassportServerConfigBuilder(instanceContext.getAnnotation());

        DependenciesBuilder builder = DependenciesBuilder.create(ManagedCertificates.class);
        if (requiresDatabase()) {
            builder.add(TestDatabase.class);
        }

        if (command.isExternalInfinispanEnabled()) {
            builder.add(InfinispanServer.class);
        }

        return builder.build();
    }

    @Override
    public PassportServer getValue(InstanceContext<PassportServer, PassportIntegrationTest> instanceContext) {

        PassportServerConfigBuilder command = getPassportServerConfigBuilder(instanceContext.getAnnotation());

        // Database startup and Passport connection setup
        if (requiresDatabase()) {
            instanceContext.getDependency(TestDatabase.class);
        }

        // External Infinispan startup and Passport connection setup
        if (command.isExternalInfinispanEnabled()) {
            instanceContext.getDependency(InfinispanServer.class);
        }

        ServerConfigInterceptorHelper interceptor = new ServerConfigInterceptorHelper(instanceContext.getRegistry());
        command = interceptor.intercept(command, instanceContext);

        ManagedCertificates managedCert = instanceContext.getDependency(ManagedCertificates.class);

        if (managedCert.isTlsEnabled()) {
            command.option("https-key-store-file", managedCert.getServerKeyStorePath());
            command.option("https-key-store-password", managedCert.getServerKeyStorePassword());
        }

        if (managedCert.isMTlsEnabled()) {
            command.option("https-client-auth", "request");
            command.option("https-trust-store-file", managedCert.getServerTrustStorePath());
            command.option("https-trust-store-password", managedCert.getServerTrustStorePassword());
        }

        command.log().fromConfig(Config.getConfig());

        getLogger().info("Starting Passport test server");
        if (getLogger().isDebugEnabled()) {
            getLogger().debugv("Startup command and options: \n\t{0}", String.join("\n\t", command.toArgs()));
        }

        long start = System.currentTimeMillis();

        PassportServer server = getServer();
        server.start(command, managedCert.isTlsEnabled());

        getLogger().infov("Passport test server started in {0} ms", System.currentTimeMillis() - start);

        return server;
    }

    private static PassportServerConfigBuilder getPassportServerConfigBuilder(PassportIntegrationTest annotation) {
        PassportServerConfig serverConfig = SupplierHelpers.getInstance(annotation.config());
        PassportServerConfigBuilder command = PassportServerConfigBuilder.startDev()
                .bootstrapAdminClient(Config.getAdminClientId(), Config.getAdminClientSecret())
                .bootstrapAdminUser(Config.getAdminUsername(), Config.getAdminPassword());

        command.log().handlers(PassportServerConfigBuilder.LogHandlers.CONSOLE);

        String supplierConfig = Config.getSupplierConfig(PassportServer.class);
        if (supplierConfig != null) {
            PassportServerConfig serverConfigOverride = SupplierHelpers.getInstance(supplierConfig);
            serverConfigOverride.configure(command);
        }

        command = serverConfig.configure(command);
        return command;
    }

    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    @Override
    public boolean compatible(InstanceContext<PassportServer, PassportIntegrationTest> a, RequestedInstance<PassportServer, PassportIntegrationTest> b) {
        return a.getAnnotation().config().equals(b.getAnnotation().config());
    }

    @Override
    public void close(InstanceContext<PassportServer, PassportIntegrationTest> instanceContext) {
        instanceContext.getValue().stop();
    }

    public abstract PassportServer getServer();

    public abstract boolean requiresDatabase();

    public abstract Logger getLogger();

    @Override
    public int order() {
        return SupplierOrder.PASSPORT_SERVER;
    }

    private static class ServerConfigInterceptorHelper extends AbstractInterceptorHelper<PassportServerConfigInterceptor, PassportServerConfigBuilder> {

        private ServerConfigInterceptorHelper(Registry registry) {
            super(registry, PassportServerConfigInterceptor.class);
        }

        @Override
        public PassportServerConfigBuilder intercept(PassportServerConfigBuilder value, Supplier<?, ?> supplier, InstanceContext<?, ?> existingInstance) {
            if (supplier instanceof PassportServerConfigInterceptor passportServerConfigInterceptor) {
                value = passportServerConfigInterceptor.intercept(value, existingInstance);
            }
            return value;
        }
    }

}
