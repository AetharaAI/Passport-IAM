package org.passport.services.ui.extend;

import org.passport.common.Profile;
import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;

public class UiTabSpi implements Spi {
    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "ui-tab";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return UiTabProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return UiTabProviderFactory.class;
    }

    @Override
    public boolean isEnabled() {
        return Profile.isFeatureEnabled(Profile.Feature.DECLARATIVE_UI);
    }
}
