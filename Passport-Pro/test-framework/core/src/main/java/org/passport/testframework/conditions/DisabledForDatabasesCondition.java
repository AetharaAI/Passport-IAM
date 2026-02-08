package org.passport.testframework.conditions;

import java.lang.annotation.Annotation;

import org.passport.testframework.database.TestDatabase;

class DisabledForDatabasesCondition extends AbstractDisabledForSupplierCondition {

    @Override
    Class<?> valueType() {
        return TestDatabase.class;
    }

    Class<? extends Annotation> annotation() {
        return DisabledForDatabases.class;
    }

}
