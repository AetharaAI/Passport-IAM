/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.passport.models.sessions.infinispan.expiration;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

import org.passport.Config;
import org.passport.config.MetricsOptions;
import org.passport.connections.infinispan.InfinispanConnectionProvider;
import org.passport.infinispan.util.InfinispanUtils;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserSessionProvider;
import org.passport.models.sessions.infinispan.InfinispanUserSessionProviderFactory;
import org.passport.provider.ProviderFactory;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.infinispan.client.hotrod.RemoteCache;

import static org.passport.connections.infinispan.InfinispanConnectionProvider.WORK_CACHE_NAME;

/**
 * Provides factory method to instantiate an {@link ExpirationTask}.
 * <p>
 * The {@link ExpirationTask} is not started.
 */
public final class ExpirationTaskFactory {

    /**
     * Creates a {@link ExpirationTask} based on the Passport configuration.
     *
     * @param session The current {@link PassportSession}.
     * @return A new instance of {@link ExpirationTask}. This instance is not started yet.
     */
    public static ExpirationTask create(PassportSession session, int expirationPeriodSeconds) {
        Consumer<Duration> onTaskExecuted = null;
        if (Config.scope().root().getBoolean(MetricsOptions.METRICS_ENABLED.getKey(), Boolean.FALSE)) {
            var timer = Timer.builder("passport.session.expiration.task")
                    .description("Passport User and Client sessions expiration tasks duration.")
                    .publishPercentileHistogram()
                    .register(Metrics.globalRegistry);
            onTaskExecuted = timer::record;
        }
        return create(session, expirationPeriodSeconds, onTaskExecuted);
    }

    /**
     * Creates a {@link ExpirationTask} based on the configuration provided by the parameters.
     *
     * @param session                     The current {@link PassportSession}.
     * @param expirationTaskPeriodSeconds The period when the database is checked for expired sessions.
     * @param onTaskExecuted              An optional {@link Consumer<Duration>}. It is invoked when a database expiration
     *                                    check finishes with its duration, in nanoseconds.
     * @return A new instance of {@link ExpirationTask}. This instance is not started yet.
     */
    public static ExpirationTask create(PassportSession session, int expirationTaskPeriodSeconds, Consumer<Duration> onTaskExecuted) {
        var connectionProvider = session.getProvider(InfinispanConnectionProvider.class);
        var schedulerExecutor = connectionProvider.getScheduledExecutor();

        if (InfinispanUtils.isEmbeddedInfinispan()) {
            var workCache = connectionProvider.getCache(WORK_CACHE_NAME);
            if (workCache.getCacheConfiguration().clustering().cacheMode().isClustered()) {
                var distributionManager = workCache.getAdvancedCache().getDistributionManager();
                return new DistributionAwareExpirationTask(session.getPassportSessionFactory(), schedulerExecutor, expirationTaskPeriodSeconds, onTaskExecuted, distributionManager);
            }

            return new LocalExpirationTask(session.getPassportSessionFactory(), schedulerExecutor, expirationTaskPeriodSeconds, onTaskExecuted);
        }

        RemoteCache<String, String> workCache = connectionProvider.getRemoteCache(WORK_CACHE_NAME);
        String nodeName = connectionProvider.getNodeInfo().nodeName();
        return new RemoteExpirationTask(session.getPassportSessionFactory(), schedulerExecutor, expirationTaskPeriodSeconds, onTaskExecuted, workCache, nodeName);
    }

    /**
     * Checks if the local instance is responsible to clean up expired sessions from {@code realm}.
     * <p>
     * Provided for testing purposes only! Do not invoke in production.
     */
    public static boolean isSelectedForExpireSessionsInRealm(PassportSession session, RealmModel realm) {
        return getEventTask(session)
                .map(BaseExpirationTask::realmFilter)
                .map(filter -> filter.test(realm))
                .orElse(false);
    }

    /**
     * Manually trigger the expiration task, bypassing any scheduling.
     * <p>
     * Provided for testing purposes only! Do not invoke in production.
     */
    public static void manualTriggerTask(PassportSession session) {
        getEventTask(session).ifPresent(BaseExpirationTask::purgeExpired);
    }

    /**
     * Returns the number of Passport instance when running with an external Infinispan cluster.
     * <p>
     * Testing purpose only! Do not invoke in production.
     */
    public static int membersSize(PassportSession session) {
        return getEventTask(session)
                .filter(RemoteExpirationTask.class::isInstance)
                .map(RemoteExpirationTask.class::cast)
                .map(RemoteExpirationTask::membersSize)
                .orElse(0);
    }

    private static Optional<BaseExpirationTask> getEventTask(PassportSession session) {
        ProviderFactory<UserSessionProvider> provider = session.getPassportSessionFactory().getProviderFactory(UserSessionProvider.class);
        if (!(provider instanceof InfinispanUserSessionProviderFactory iuspf)) {
            return Optional.empty();
        }
        ExpirationTask task = iuspf.getExpirationTask();
        if (!(task instanceof BaseExpirationTask bet)) {
            return Optional.empty();
        }
        return Optional.of(bet);
    }

}
