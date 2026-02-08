package org.passport.testframework.oauth;

import java.util.List;

import org.passport.testframework.TestFrameworkExtension;
import org.passport.testframework.injection.Supplier;

public class OAuthTestFrameworkExtension implements TestFrameworkExtension {

    @Override
    public List<Supplier<?, ?>> suppliers() {
        return List.of(new OAuthClientSupplier(), new TestAppSupplier(), new OAuthIdentityProviderSupplier());
    }

}
