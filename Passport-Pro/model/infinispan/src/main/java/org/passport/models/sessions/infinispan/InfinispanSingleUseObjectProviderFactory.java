/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.passport.models.sessions.infinispan;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.passport.Config;
import org.passport.common.util.Time;
import org.passport.connections.infinispan.InfinispanConnectionProvider;
import org.passport.infinispan.util.InfinispanUtils;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.SingleUseObjectProvider;
import org.passport.models.SingleUseObjectProviderFactory;
import org.passport.models.session.RevokedTokenPersisterProvider;
import org.passport.models.sessions.infinispan.entities.SingleUseObjectValueEntity;
import org.passport.models.sessions.infinispan.transaction.InfinispanTransactionProvider;
import org.passport.models.utils.PostMigrationEvent;
import org.passport.provider.EnvironmentDependentProviderFactory;
import org.passport.provider.Provider;
import org.passport.provider.ProviderConfigProperty;
import org.passport.provider.ProviderConfigurationBuilder;
import org.passport.provider.ServerInfoAwareProviderFactory;

import org.infinispan.commons.api.BasicCache;

import static org.passport.connections.infinispan.InfinispanConnectionProvider.ACTION_TOKEN_CACHE;
import static org.passport.storage.datastore.DefaultDatastoreProviderFactory.setupClearExpiredRevokedTokensScheduledTask;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InfinispanSingleUseObjectProviderFactory implements SingleUseObjectProviderFactory<InfinispanSingleUseObjectProvider>, EnvironmentDependentProviderFactory, ServerInfoAwareProviderFactory {

    public static final String CONFIG_PERSIST_REVOKED_TOKENS = "persistRevokedTokens";
    public static final boolean DEFAULT_PERSIST_REVOKED_TOKENS = true;
    public static final String LOADED = "loaded" + SingleUseObjectProvider.REVOKED_KEY;

    protected BasicCache<String, SingleUseObjectValueEntity> singleUseObjectCache;

    private volatile boolean initialized;
    private boolean persistRevokedTokens;

    @Override
    public Set<Class<? extends Provider>> dependsOn() {
        return Set.of(InfinispanConnectionProvider.class, InfinispanTransactionProvider.class);
    }

    @Override
    public InfinispanSingleUseObjectProvider create(PassportSession session) {
        initialize(session);
        return new InfinispanSingleUseObjectProvider(session, singleUseObjectCache, persistRevokedTokens, createTransaction(session));
    }

    @Override
    public void init(Config.Scope config) {
        persistRevokedTokens = config.getBoolean(CONFIG_PERSIST_REVOKED_TOKENS, DEFAULT_PERSIST_REVOKED_TOKENS);
    }

    private void initialize(PassportSession session) {
        if (persistRevokedTokens && !initialized) {
            synchronized (this) {
                if (!initialized) {
                    RevokedTokenPersisterProvider provider = session.getProvider(RevokedTokenPersisterProvider.class);
                    if (singleUseObjectCache.get(LOADED) == null) {
                        // in a cluster, multiple Passport instances might load the same data in parallel, but that wouldn't matter
                        provider.getAllRevokedTokens().forEach(revokedToken -> {
                            long lifespanSeconds = revokedToken.expiry() - Time.currentTime();
                            if (lifespanSeconds > 0) {
                                singleUseObjectCache.put(revokedToken.tokenId() + SingleUseObjectProvider.REVOKED_KEY, new SingleUseObjectValueEntity(Collections.emptyMap()),
                                         Time.toMillis(lifespanSeconds), TimeUnit.MILLISECONDS);
                            }
                        });
                        singleUseObjectCache.put(LOADED, new SingleUseObjectValueEntity(Collections.emptyMap()));
                    }
                    initialized = true;
                }
            }
        }
    }

    @Override
    public void postInit(PassportSessionFactory factory) {
        // It is necessary to put the cache initialization here, otherwise the cache would be initialized lazily, that
        // means also listeners will start only after first cache initialization - that would be too late
        if (singleUseObjectCache == null) {
            try (var session = factory.create()) {
                InfinispanConnectionProvider connections = session.getProvider(InfinispanConnectionProvider.class);
                singleUseObjectCache = connections.getCache(ACTION_TOKEN_CACHE);
            }
        }

        if (persistRevokedTokens) {
            factory.register(event -> {
                if (event instanceof PostMigrationEvent pme) {
                    PassportSessionFactory sessionFactory = pme.getFactory();
                    setupClearExpiredRevokedTokensScheduledTask(sessionFactory);
                    try (PassportSession session = sessionFactory.create()) {
                        // load sessions during startup, not on first request to avoid congestion
                        initialize(session);
                    }
                }
            });
        }
    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return InfinispanUtils.EMBEDDED_PROVIDER_ID;
    }

    @Override
    public int order() {
        return InfinispanUtils.PROVIDER_ORDER;
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return InfinispanUtils.isEmbeddedInfinispan();
    }

    @Override
    public Map<String, String> getOperationalInfo() {
        Map<String, String> info = new HashMap<>();
        info.put(CONFIG_PERSIST_REVOKED_TOKENS, Boolean.toString(persistRevokedTokens));
        return info;
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        ProviderConfigurationBuilder builder = ProviderConfigurationBuilder.create();

        builder.property()
                .name(CONFIG_PERSIST_REVOKED_TOKENS)
                .type("boolean")
                .helpText("If revoked tokens are stored persistently across restarts")
                .defaultValue(DEFAULT_PERSIST_REVOKED_TOKENS)
                .add();

        return builder.build();
    }

    private static InfinispanPassportTransaction createTransaction(PassportSession session) {
        InfinispanTransactionProvider transactionProvider = session.getProvider(InfinispanTransactionProvider.class);
        InfinispanPassportTransaction tx = new InfinispanPassportTransaction();
        transactionProvider.registerTransaction(tx);
        return tx;
    }

}
