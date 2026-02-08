package org.passport.authentication.otp;

import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;

public class OTPApplicationSpi implements Spi {

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "otp-application";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return OTPApplicationProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return OTPApplicationProviderFactory.class;
    }

}
