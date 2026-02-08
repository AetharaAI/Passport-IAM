package org.passport.services.ui.extend;

import org.passport.common.Profile;
import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;

public class UiPageSpi implements Spi {
    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "ui-page";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return UiPageProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return UiPageProviderFactory.class;
    }

    @Override
    public boolean isEnabled() {
        return Profile.isFeatureEnabled(Profile.Feature.DECLARATIVE_UI);
    }
}
