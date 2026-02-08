package org.passport.device;

import java.util.Set;

import org.passport.Config;
import org.passport.cache.LocalCacheProvider;
import org.passport.models.PassportSessionFactory;
import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;

public interface DeviceRepresentationProviderFactory extends ProviderFactory<DeviceRepresentationProvider> {

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
    default Set<Class<? extends Provider>> dependsOn() {
        return Set.of(LocalCacheProvider.class);
    }
}
