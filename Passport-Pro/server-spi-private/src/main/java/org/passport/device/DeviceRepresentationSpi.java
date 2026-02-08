package org.passport.device;

import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;

public class DeviceRepresentationSpi implements Spi {

    public static final String NAME = "deviceRepresentation";
    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return DeviceRepresentationProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return DeviceRepresentationProviderFactory.class;
    }

}
