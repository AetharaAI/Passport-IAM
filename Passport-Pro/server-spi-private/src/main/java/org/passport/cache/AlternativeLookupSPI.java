package org.passport.cache;

import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;

public class AlternativeLookupSPI implements Spi {

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "alternativeLookup";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return AlternativeLookupProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return AlternativeLookupProviderFactory.class;
    }
}
