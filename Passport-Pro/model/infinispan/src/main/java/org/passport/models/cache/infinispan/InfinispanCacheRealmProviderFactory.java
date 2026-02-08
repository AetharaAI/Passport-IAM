/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.passport.models.cache.infinispan;

import org.passport.Config;
import org.passport.cluster.ClusterEvent;
import org.passport.cluster.ClusterProvider;
import org.passport.connections.infinispan.InfinispanConnectionProvider;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.cache.CacheRealmProvider;
import org.passport.models.cache.CacheRealmProviderFactory;
import org.passport.models.cache.infinispan.entities.Revisioned;
import org.passport.models.cache.infinispan.events.InvalidationEvent;

import org.infinispan.Cache;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class InfinispanCacheRealmProviderFactory implements CacheRealmProviderFactory {

    private static final Logger log = Logger.getLogger(InfinispanCacheRealmProviderFactory.class);
    public static final String REALM_CLEAR_CACHE_EVENTS = "REALM_CLEAR_CACHE_EVENTS";
    public static final String REALM_INVALIDATION_EVENTS = "REALM_INVALIDATION_EVENTS";

    protected volatile RealmCacheManager realmCache;

    @Override
    public CacheRealmProvider create(PassportSession session) {
        lazyInit(session);
        return new RealmCacheSession(realmCache, session);
    }

    private void lazyInit(PassportSession session) {
        if (realmCache == null) {
            synchronized (this) {
                if (realmCache == null) {
                    Cache<String, Revisioned> cache = session.getProvider(InfinispanConnectionProvider.class).getCache(InfinispanConnectionProvider.REALM_CACHE_NAME);
                    Cache<String, Long> revisions = session.getProvider(InfinispanConnectionProvider.class).getCache(InfinispanConnectionProvider.REALM_REVISIONS_CACHE_NAME);
                    realmCache = new RealmCacheManager(cache, revisions);

                    ClusterProvider cluster = session.getProvider(ClusterProvider.class);
                    cluster.registerListener(REALM_INVALIDATION_EVENTS, (ClusterEvent event) -> {

                        InvalidationEvent invalidationEvent = (InvalidationEvent) event;
                        realmCache.invalidationEventReceived(invalidationEvent);

                    });

                    cluster.registerListener(REALM_CLEAR_CACHE_EVENTS, (ClusterEvent event) -> {

                        realmCache.clear();

                    });

                    log.debug("Registered cluster listeners");
                }
            }
        }
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(PassportSessionFactory factory) {

    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "default";
    }

}
