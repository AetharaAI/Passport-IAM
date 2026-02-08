package org.passport.encoding;

import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;

public class ResourceEncodingSpi implements Spi {

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "resource-encoding";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return ResourceEncodingProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ResourceEncodingProviderFactory.class;
    }

}
