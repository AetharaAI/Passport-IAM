package org.passport.testframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.passport.testframework.PassportIntegrationTestExtension;
import org.passport.testframework.server.DefaultPassportServerConfig;
import org.passport.testframework.server.PassportServerConfig;

import org.junit.jupiter.api.extension.ExtendWith;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith({PassportIntegrationTestExtension.class})
public @interface PassportIntegrationTest {

    Class<? extends PassportServerConfig> config() default DefaultPassportServerConfig.class;

}
