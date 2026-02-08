package org.passport.testframework.conditions;

import java.lang.annotation.Annotation;

import org.passport.testframework.server.PassportServer;

class DisabledForServersCondition extends AbstractDisabledForSupplierCondition {

    @Override
    Class<?> valueType() {
        return PassportServer.class;
    }

    Class<? extends Annotation> annotation() {
        return DisabledForServers.class;
    }

}
