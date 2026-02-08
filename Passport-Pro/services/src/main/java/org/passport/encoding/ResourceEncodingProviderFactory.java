package org.passport.encoding;

import org.passport.Config;
import org.passport.models.PassportSessionFactory;
import org.passport.provider.ProviderFactory;

public interface ResourceEncodingProviderFactory extends ProviderFactory<ResourceEncodingProvider> {

    boolean encodeContentType(String contentType);

    @Override
    default void init(Config.Scope config) {
    }

    @Override
    default void postInit(PassportSessionFactory factory) {
    }

    @Override
    default void close() {
    }

}
