package org.passport.protocol.saml;

import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;

/**
 *
 */
public class ArtifactResolverSpi implements Spi {
    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "saml-artifact-resolver";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return ArtifactResolver.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ArtifactResolverFactory.class;
    }
}
