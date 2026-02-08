package org.passport.models.mapper;

import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;

/**
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public class ClientModelMapperSpi implements Spi {
    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "client-model-mapper";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return ClientModelMapper.class;
    }

    @Override
    public Class<? extends ProviderFactory<ClientModelMapper>> getProviderFactoryClass() {
        return ClientModelMapperFactory.class;
    }
}
