package org.passport.testframework.injection.mocks;

import java.util.List;

import org.passport.testframework.TestFrameworkExtension;
import org.passport.testframework.injection.Supplier;

public class MockTestFrameworkExtension implements TestFrameworkExtension {

    @Override
    public List<Supplier<?, ?>> suppliers() {
        return List.of(
                new MockParentSupplier(),
                new MockParent2Supplier(),
                new MockChildSupplier()
        );
    }

}
