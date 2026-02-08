package org.passport.testframework.events;

import java.io.IOException;

import org.passport.testframework.annotations.InjectSysLogServer;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.injection.SupplierOrder;
import org.passport.testframework.server.PassportServerConfigBuilder;
import org.passport.testframework.server.PassportServerConfigInterceptor;

public class SysLogServerSupplier implements Supplier<SysLogServer, InjectSysLogServer>, PassportServerConfigInterceptor<SysLogServer, InjectSysLogServer> {

    @Override
    public SysLogServer getValue(InstanceContext<SysLogServer, InjectSysLogServer> instanceContext) {
        try {
            return new SysLogServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    @Override
    public void close(InstanceContext<SysLogServer, InjectSysLogServer> instanceContext) {
        SysLogServer server = instanceContext.getValue();
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean compatible(InstanceContext<SysLogServer, InjectSysLogServer> a, RequestedInstance<SysLogServer, InjectSysLogServer> b) {
        return true;
    }

    @Override
    public PassportServerConfigBuilder intercept(PassportServerConfigBuilder serverConfig, InstanceContext<SysLogServer, InjectSysLogServer> instanceContext) {
        serverConfig.log()
                .handlers(PassportServerConfigBuilder.LogHandlers.SYSLOG)
                .syslogEndpoint(instanceContext.getValue().getEndpoint())
                .handlerLevel(PassportServerConfigBuilder.LogHandlers.SYSLOG, "INFO");

        serverConfig.option("spi-events-listener-jboss-logging-success-level", "INFO")
                .log().categoryLevel("org.passport.events", "INFO");

        return serverConfig;
    }

    @Override
    public int order() {
        return SupplierOrder.BEFORE_PASSPORT_SERVER;
    }
}
