package org.passport.cache;

import java.util.Set;

import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;

public interface AlternativeLookupProviderFactory extends ProviderFactory<AlternativeLookupProvider> {
    @Override
    default Set<Class<? extends Provider>> dependsOn() {
        return Set.of(LocalCacheProvider.class);
    }
}
