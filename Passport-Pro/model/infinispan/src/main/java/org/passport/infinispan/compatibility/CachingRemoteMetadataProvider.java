package org.passport.infinispan.compatibility;

import java.util.stream.Stream;

import org.passport.Config;
import org.passport.compatibility.AbstractCompatibilityMetadataProvider;
import org.passport.infinispan.util.InfinispanUtils;
import org.passport.spi.infinispan.CacheRemoteConfigProviderSpi;
import org.passport.spi.infinispan.impl.remote.DefaultCacheRemoteConfigProviderFactory;

public class CachingRemoteMetadataProvider extends AbstractCompatibilityMetadataProvider {

    public CachingRemoteMetadataProvider() {
        super(CacheRemoteConfigProviderSpi.SPI_NAME, DefaultCacheRemoteConfigProviderFactory.PROVIDER_ID);
    }

    @Override
    protected boolean isEnabled(Config.Scope scope) {
        return InfinispanUtils.isRemoteInfinispan();
    }

    @Override
    protected Stream<String> configKeys() {
        return Stream.of(DefaultCacheRemoteConfigProviderFactory.HOSTNAME, DefaultCacheRemoteConfigProviderFactory.PORT);
    }
}
