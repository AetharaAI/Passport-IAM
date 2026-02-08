package org.passport.testframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.realm.DefaultUserConfig;
import org.passport.testframework.realm.UserConfig;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectUser {

    Class<? extends UserConfig> config() default DefaultUserConfig.class;

    LifeCycle lifecycle() default LifeCycle.CLASS;

    String ref() default "";

    String realmRef() default "";
}
