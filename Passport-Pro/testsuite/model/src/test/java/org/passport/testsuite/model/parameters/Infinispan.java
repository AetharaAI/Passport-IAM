/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
package org.passport.testsuite.model.parameters;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.passport.cluster.infinispan.InfinispanClusterProviderFactory;
import org.passport.connections.infinispan.InfinispanConnectionProviderFactory;
import org.passport.connections.infinispan.InfinispanConnectionSpi;
import org.passport.infinispan.util.InfinispanUtils;
import org.passport.keys.PublicKeyStorageSpi;
import org.passport.keys.infinispan.InfinispanCachePublicKeyProviderFactory;
import org.passport.keys.infinispan.InfinispanPublicKeyStorageProviderFactory;
import org.passport.models.SingleUseObjectSpi;
import org.passport.models.UserLoginFailureSpi;
import org.passport.models.UserSessionSpi;
import org.passport.models.cache.CachePublicKeyProviderSpi;
import org.passport.models.cache.CacheRealmProviderSpi;
import org.passport.models.cache.CacheUserProviderSpi;
import org.passport.models.cache.authorization.CachedStoreFactorySpi;
import org.passport.models.cache.infinispan.InfinispanCacheRealmProviderFactory;
import org.passport.models.cache.infinispan.InfinispanUserCacheProviderFactory;
import org.passport.models.cache.infinispan.authorization.InfinispanCacheStoreFactoryProviderFactory;
import org.passport.models.cache.infinispan.organization.InfinispanOrganizationProviderFactory;
import org.passport.models.session.UserSessionPersisterSpi;
import org.passport.models.sessions.infinispan.InfinispanAuthenticationSessionProviderFactory;
import org.passport.models.sessions.infinispan.InfinispanSingleUseObjectProviderFactory;
import org.passport.models.sessions.infinispan.InfinispanUserLoginFailureProviderFactory;
import org.passport.models.sessions.infinispan.InfinispanUserSessionProviderFactory;
import org.passport.models.sessions.infinispan.transaction.InfinispanTransactionProviderFactory;
import org.passport.models.sessions.infinispan.transaction.InfinispanTransactionSpi;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;
import org.passport.sessions.AuthenticationSessionSpi;
import org.passport.sessions.StickySessionEncoderProviderFactory;
import org.passport.sessions.StickySessionEncoderSpi;
import org.passport.spi.infinispan.CacheEmbeddedConfigProviderFactory;
import org.passport.spi.infinispan.CacheEmbeddedConfigProviderSpi;
import org.passport.spi.infinispan.JGroupsCertificateProviderFactory;
import org.passport.spi.infinispan.JGroupsCertificateProviderSpi;
import org.passport.spi.infinispan.impl.embedded.DefaultCacheEmbeddedConfigProviderFactory;
import org.passport.storage.configuration.ServerConfigStorageProviderFactory;
import org.passport.storage.configuration.ServerConfigurationStorageProviderSpi;
import org.passport.testsuite.model.Config;
import org.passport.testsuite.model.PassportModelParameters;
import org.passport.timer.TimerProviderFactory;

import com.google.common.collect.ImmutableSet;

/**
 * @author hmlnarik
 */
public class Infinispan extends PassportModelParameters {

    private static final AtomicInteger NODE_COUNTER = new AtomicInteger();

    static final Set<Class<? extends Spi>> ALLOWED_SPIS = ImmutableSet.<Class<? extends Spi>>builder()
            .add(AuthenticationSessionSpi.class)
            .add(CacheRealmProviderSpi.class)
            .add(CachedStoreFactorySpi.class)
            .add(CacheUserProviderSpi.class)
            .add(InfinispanConnectionSpi.class)
            .add(StickySessionEncoderSpi.class)
            .add(UserSessionPersisterSpi.class)
            .add(SingleUseObjectSpi.class)
            .add(PublicKeyStorageSpi.class)
            .add(CachePublicKeyProviderSpi.class)
            .add(CacheEmbeddedConfigProviderSpi.class)
            .add(JGroupsCertificateProviderSpi.class)
            .add(ServerConfigurationStorageProviderSpi.class)
            .add(InfinispanTransactionSpi.class)
            .build();

    static final Set<Class<? extends ProviderFactory>> ALLOWED_FACTORIES = ImmutableSet.<Class<? extends ProviderFactory>>builder()
            .add(InfinispanAuthenticationSessionProviderFactory.class)
            .add(InfinispanCacheRealmProviderFactory.class)
            .add(InfinispanCacheStoreFactoryProviderFactory.class)
            .add(InfinispanClusterProviderFactory.class)
            .add(InfinispanConnectionProviderFactory.class)
            .add(InfinispanUserCacheProviderFactory.class)
            .add(InfinispanUserSessionProviderFactory.class)
            .add(InfinispanUserLoginFailureProviderFactory.class)
            .add(InfinispanSingleUseObjectProviderFactory.class)
            .add(StickySessionEncoderProviderFactory.class)
            .add(TimerProviderFactory.class)
            .add(InfinispanPublicKeyStorageProviderFactory.class)
            .add(InfinispanCachePublicKeyProviderFactory.class)
            .add(InfinispanOrganizationProviderFactory.class)
            .add(CacheEmbeddedConfigProviderFactory.class)
            .add(JGroupsCertificateProviderFactory.class)
            .add(ServerConfigStorageProviderFactory.class)
            .add(InfinispanTransactionProviderFactory.class)
            .build();

    @Override
    public void updateConfig(Config cf) {
        cf.spi("connectionsInfinispan")
                .provider("default")
                .config("usePassportTimeService", "true")
                .spi(UserLoginFailureSpi.NAME)
                .provider(InfinispanUtils.EMBEDDED_PROVIDER_ID)
                .config("stalledTimeoutInSeconds", "10")
                .spi(UserSessionSpi.NAME)
                .provider(InfinispanUtils.EMBEDDED_PROVIDER_ID)
                .config("sessionPreloadStalledTimeoutInSeconds", "10")
                .config("offlineSessionCacheEntryLifespanOverride", "43200")
                .config("offlineClientSessionCacheEntryLifespanOverride", "43200");
        cf.spi(CacheEmbeddedConfigProviderSpi.SPI_NAME)
                .provider(DefaultCacheEmbeddedConfigProviderFactory.PROVIDER_ID)
                .config(DefaultCacheEmbeddedConfigProviderFactory.CONFIG, "test-ispn.xml")
                .config(DefaultCacheEmbeddedConfigProviderFactory.NODE_NAME, "node-" + NODE_COUNTER.incrementAndGet());

    }

    public Infinispan() {
        super(ALLOWED_SPIS, ALLOWED_FACTORIES);
    }
}
