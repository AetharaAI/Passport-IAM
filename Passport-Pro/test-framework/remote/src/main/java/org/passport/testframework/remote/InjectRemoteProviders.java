package org.passport.testframework.remote;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.passport.testframework.injection.LifeCycle;

@Retention(RetentionPolicy.RUNTIME)
public @interface InjectRemoteProviders {

    LifeCycle lifecycle() default LifeCycle.GLOBAL;
}
