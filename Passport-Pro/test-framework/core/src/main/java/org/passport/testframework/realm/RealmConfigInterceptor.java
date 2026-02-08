package org.passport.testframework.realm;

import java.lang.annotation.Annotation;

import org.passport.testframework.injection.InstanceContext;

public interface RealmConfigInterceptor<T, S extends Annotation> {

    RealmConfigBuilder intercept(RealmConfigBuilder realm, InstanceContext<T, S> instanceContext);

}
