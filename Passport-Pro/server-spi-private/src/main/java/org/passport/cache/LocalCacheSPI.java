package org.passport.cache;

import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;

public class LocalCacheSPI implements Spi {
    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "localCache";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return LocalCacheProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory<?>> getProviderFactoryClass() {
        return LocalCacheProviderFactory.class;
    }
}
