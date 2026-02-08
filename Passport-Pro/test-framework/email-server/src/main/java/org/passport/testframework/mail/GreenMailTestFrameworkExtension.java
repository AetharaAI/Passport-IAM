package org.passport.testframework.mail;

import java.util.List;

import org.passport.testframework.TestFrameworkExtension;
import org.passport.testframework.injection.Supplier;

public class GreenMailTestFrameworkExtension implements TestFrameworkExtension {

    @Override
    public List<Supplier<?, ?>> suppliers() {
        return List.of(new GreenMailSupplier());
    }

}
