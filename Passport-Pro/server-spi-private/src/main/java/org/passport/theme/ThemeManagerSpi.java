package org.passport.theme;

import org.passport.models.ThemeManager;
import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;

public class ThemeManagerSpi implements Spi {

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "themeManager";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return ThemeManager.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ThemeManagerFactory.class;
    }
}
