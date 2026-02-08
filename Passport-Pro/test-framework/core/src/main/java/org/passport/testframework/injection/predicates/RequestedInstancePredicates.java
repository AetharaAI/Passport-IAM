package org.passport.testframework.injection.predicates;

import java.util.Objects;
import java.util.function.Predicate;

import org.passport.testframework.injection.RequestedInstance;

public interface RequestedInstancePredicates {

    static Predicate<RequestedInstance<?, ?>> matches(Class<?> typeClass, String ref) {
        return r -> r.getSupplier().getValueType().equals(typeClass) && Objects.equals(r.getRef(), ref);
    }

}
