package org.passport.testframework.injection.predicates;

import java.util.Objects;
import java.util.function.Predicate;

import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.LifeCycle;

public interface InstanceContextPredicates {

    static Predicate<InstanceContext<?, ?>> hasLifeCycle(LifeCycle lifeCycle) {
        return i -> i.getLifeCycle().equals(lifeCycle);
    }

    static Predicate<InstanceContext<?, ?>> isInstanceof(Class<?> valueTypeClass) {
        return i -> valueTypeClass.isInstance(i.getValue());
    }

    static Predicate<InstanceContext<?, ?>> matches(Class<?> typeClass, String ref) {
        return i -> i.getSupplier().getValueType().equals(typeClass) && Objects.equals(i.getRef(), ref);
    }

}
