package org.passport.testframework.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.passport.testframework.injection.LifeCycle;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectAdminClientFactory {

    String ref() default "";

    LifeCycle lifecycle() default LifeCycle.CLASS;
}
