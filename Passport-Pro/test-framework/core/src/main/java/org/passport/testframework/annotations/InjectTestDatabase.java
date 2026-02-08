package org.passport.testframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.passport.testframework.database.DatabaseConfig;
import org.passport.testframework.database.DefaultDatabaseConfig;
import org.passport.testframework.injection.LifeCycle;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectTestDatabase {

    LifeCycle lifecycle() default LifeCycle.GLOBAL;

    Class<? extends DatabaseConfig> config() default DefaultDatabaseConfig.class;
}
