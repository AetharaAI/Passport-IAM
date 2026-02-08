package org.passport.protocol.oidc.ext;

import org.passport.Config;
import org.passport.models.PassportSessionFactory;
import org.passport.provider.ProviderFactory;

public interface OIDCExtProviderFactory extends ProviderFactory<OIDCExtProvider> {

    @Override
    default void init(Config.Scope config) {

    }

    @Override
    default void postInit(PassportSessionFactory factory) {

    }

    @Override
    default void close() {

    }

    @Override
    default int order() {
        return 0;
    }

}
