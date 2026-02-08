package org.passport.tests.utils.infinispan;

import java.io.Serializable;

import org.passport.connections.infinispan.InfinispanConnectionProvider;
import org.passport.models.PassportSession;
import org.passport.testframework.remote.providers.runonserver.RunOnServer;

import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.logging.Logger;

import static org.passport.connections.infinispan.InfinispanUtil.setTimeServiceToPassportTime;

/**
 * Should be executed on the server-side with RunOnServer or @TestOnServer
 */
public class InfinispanTimeUtil implements Serializable {

    protected static final Logger logger = Logger.getLogger(InfinispanTimeUtil.class);

    private static Runnable origTimeService = null;

    public static RunOnServer enableTestingTimeService() {
        return InfinispanTimeUtil::enableTestingTimeService;
    }

    public static RunOnServer disableTestingTimeService() {
        return InfinispanTimeUtil::disableTestingTimeService;
    }

    public static void enableTestingTimeService(PassportSession session) {
        if (origTimeService != null) {
            throw new IllegalStateException("Calling setTestingTimeService when testing TimeService was already set");
        }

        InfinispanConnectionProvider ispnProvider = session.getProvider(InfinispanConnectionProvider.class);

        logger.info("Will set PassportIspnTimeService to the infinispan cacheManager");
        EmbeddedCacheManager cacheManager = ispnProvider.getCache(InfinispanConnectionProvider.USER_CACHE_NAME).getCacheManager();
        origTimeService = setTimeServiceToPassportTime(cacheManager);
    }

    public static void disableTestingTimeService(PassportSession session) {
        if (origTimeService == null) {
            throw new IllegalStateException("Calling revertTimeService when testing TimeService was not set");
        }

        origTimeService.run();
        origTimeService = null;
    }

}
