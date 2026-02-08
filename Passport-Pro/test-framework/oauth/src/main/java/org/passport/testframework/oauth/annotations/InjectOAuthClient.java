package org.passport.testframework.oauth.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.oauth.DefaultOAuthClientConfiguration;
import org.passport.testframework.realm.ClientConfig;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectOAuthClient {

    Class<? extends ClientConfig> config() default DefaultOAuthClientConfiguration.class;

    LifeCycle lifecycle() default LifeCycle.CLASS;

    String ref() default "";

    String realmRef() default "";

    boolean kcAdmin() default false;

}
