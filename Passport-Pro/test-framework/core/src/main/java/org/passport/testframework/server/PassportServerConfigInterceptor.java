package org.passport.testframework.server;

import java.lang.annotation.Annotation;

import org.passport.testframework.injection.InstanceContext;

public interface PassportServerConfigInterceptor<T, S extends Annotation> {

    PassportServerConfigBuilder intercept(PassportServerConfigBuilder serverConfig, InstanceContext<T, S> instanceContext);

}
