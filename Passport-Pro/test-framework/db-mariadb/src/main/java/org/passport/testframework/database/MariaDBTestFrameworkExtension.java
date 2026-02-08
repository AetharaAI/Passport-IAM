package org.passport.testframework.database;

import java.util.List;

import org.passport.testframework.TestFrameworkExtension;
import org.passport.testframework.injection.Supplier;

public class MariaDBTestFrameworkExtension implements TestFrameworkExtension {

    @Override
    public List<Supplier<?, ?>> suppliers() {
        return List.of(new MariaDBDatabaseSupplier());
    }
}
