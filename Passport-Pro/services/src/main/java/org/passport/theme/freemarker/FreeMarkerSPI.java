package org.passport.theme.freemarker;

import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;

public class FreeMarkerSPI implements Spi {
    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "freemarker";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return FreeMarkerProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return FreeMarkerProviderFactory.class;
    }
}
