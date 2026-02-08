package org.passport.protocol.oidc.ext;

import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;

public class OIDCExtSPI implements Spi {

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "openid-connect-ext";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return OIDCExtProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return OIDCExtProviderFactory.class;
    }

}
