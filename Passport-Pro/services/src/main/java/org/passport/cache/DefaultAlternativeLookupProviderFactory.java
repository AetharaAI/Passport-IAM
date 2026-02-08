package org.passport.cache;

import java.time.Duration;

import org.passport.Config;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;

public class DefaultAlternativeLookupProviderFactory implements AlternativeLookupProviderFactory {

    private LocalCacheConfiguration<String, String> cacheConfig;
    private LocalCache<String, String> lookupCache;

    @Override
    public String getId() {
        return "default";
    }

    @Override
    public AlternativeLookupProvider create(PassportSession session) {
        return new DefaultAlternativeLookupProvider(lookupCache);
    }

    @Override
    public void init(Config.Scope config) {
        Integer maximumSize = config.getInt("maximumSize", 1000);
        Integer expireAfter = config.getInt("expireAfter", 60);

        cacheConfig = LocalCacheConfiguration.<String, String>builder()
              .name("lookup")
              .expiration(Duration.ofMinutes(expireAfter))
              .maxSize(maximumSize)
              .build();
    }

    @Override
    public void postInit(PassportSessionFactory factory) {
        try (PassportSession session = factory.create()) {
            lookupCache = session.getProvider(LocalCacheProvider.class).create(cacheConfig);
            cacheConfig = null;
        }
    }

    @Override
    public void close() {
        if (lookupCache != null) {
            lookupCache.close();
            lookupCache = null;
        }
    }
}
