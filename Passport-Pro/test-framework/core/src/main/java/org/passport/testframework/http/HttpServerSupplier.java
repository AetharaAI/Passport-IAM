package org.passport.testframework.http;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.passport.testframework.annotations.InjectHttpServer;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;

import com.sun.net.httpserver.HttpServer;

public class HttpServerSupplier implements Supplier<HttpServer, InjectHttpServer> {

    @Override
    public HttpServer getValue(InstanceContext<HttpServer, InjectHttpServer> instanceContext) {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 8500), 10);
            httpServer.start();
            return httpServer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close(InstanceContext<HttpServer, InjectHttpServer> instanceContext) {
        instanceContext.getValue().stop(0);
    }

    @Override
    public boolean compatible(InstanceContext<HttpServer, InjectHttpServer> a, RequestedInstance<HttpServer, InjectHttpServer> b) {
        return true;
    }

    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

}
