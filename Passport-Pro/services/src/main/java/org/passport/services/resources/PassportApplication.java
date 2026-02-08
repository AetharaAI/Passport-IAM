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
package org.passport.services.resources;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.ws.rs.core.Application;

import org.passport.Config;
import org.passport.common.Profile;
import org.passport.common.crypto.CryptoIntegration;
import org.passport.config.ConfigProviderFactory;
import org.passport.exportimport.ExportImportConfig;
import org.passport.exportimport.ExportImportManager;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.PassportSessionTask;
import org.passport.models.dblock.DBLockManager;
import org.passport.models.dblock.DBLockProvider;
import org.passport.models.utils.PassportModelUtils;
import org.passport.models.utils.PostMigrationEvent;
import org.passport.platform.Platform;
import org.passport.platform.PlatformProvider;
import org.passport.services.managers.ApplianceBootstrap;
import org.passport.transaction.JtaTransactionManagerLookup;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 *
 */
public abstract class PassportApplication extends Application {

    private static final Logger logger = Logger.getLogger(PassportApplication.class);

    protected final PlatformProvider platform = Platform.getPlatform();

    private static PassportSessionFactory sessionFactory;

    public PassportApplication() {
        try {

            logger.debugv("PlatformProvider: {0}", platform.getClass().getName());
            loadConfig();

            platform.onStartup(this::startup);
            platform.onShutdown(this::shutdown);

        } catch (Throwable t) {
            platform.exit(t);
        }
    }

    protected void startup() {
        Profile.getInstance().logUnsupportedFeatures();
        CryptoIntegration.init(PassportApplication.class.getClassLoader());
        PassportApplication.sessionFactory = createSessionFactory();

        ExportImportManager[] exportImportManager = new ExportImportManager[1];

        PassportModelUtils.runJobInTransaction(sessionFactory, new PassportSessionTask() {
            @Override
            public void run(PassportSession session) {
                DBLockManager dbLockManager = new DBLockManager(session);
                dbLockManager.checkForcedUnlock();
                DBLockProvider dbLock = dbLockManager.getDBLock();
                dbLock.waitForLock(DBLockProvider.Namespace.PASSPORT_BOOT);
                try {
                    exportImportManager[0] = bootstrap();
                } finally {
                    dbLock.releaseLock();
                }
            }
        });

        if (exportImportManager[0].isRunExport()) {
            exportImportManager[0].runExport();
        }

        sessionFactory.publish(new PostMigrationEvent(sessionFactory));
    }

    protected void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    private static class BootstrapState {
        ExportImportManager exportImportManager;
        boolean newInstall;
    }

    // Bootstrap master realm, import realms and create admin user.
    protected ExportImportManager bootstrap() {
        BootstrapState bootstrapState = new BootstrapState();

        logger.debug("bootstrap");
        PassportModelUtils.runJobInTransaction(sessionFactory, new PassportSessionTask() {
            @Override
            public void run(PassportSession session) {
                // TODO what is the purpose of following piece of code? Leaving it as is for now.
                JtaTransactionManagerLookup lookup = (JtaTransactionManagerLookup) sessionFactory.getProviderFactory(JtaTransactionManagerLookup.class);
                if (lookup != null) {
                    if (lookup.getTransactionManager() != null) {
                        try {
                            Transaction transaction = lookup.getTransactionManager().getTransaction();
                            logger.debugv("bootstrap current transaction? {0}", transaction != null);
                            if (transaction != null) {
                                logger.debugv("bootstrap current transaction status? {0}", transaction.getStatus());
                            }
                        } catch (SystemException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                // TODO up here ^^

                ApplianceBootstrap applianceBootstrap = new ApplianceBootstrap(session);
                var exportImportManager = bootstrapState.exportImportManager = new ExportImportManager(session);
                bootstrapState.newInstall = applianceBootstrap.isNewInstall();
                if (bootstrapState.newInstall) {
                    boolean existing = ExportImportConfig.isSingleTransaction();
                    ExportImportConfig.setSingleTransaction(true);
                    try {
                        if (!exportImportManager.isImportMasterIncluded()) {
                            applianceBootstrap.createMasterRealm();
                        }
                        // these are also running in the initial bootstrap transaction - if there is a problem, the server won't be initialized at all
                        exportImportManager.runImport();
                        createTemporaryAdmin(session);
                    } finally {
                        ExportImportConfig.setSingleTransaction(existing);
                    }
                }
            }
        });

        if (!bootstrapState.newInstall) {
            bootstrapState.exportImportManager.runImport();
        }

        return bootstrapState.exportImportManager;
    }

    protected abstract void createTemporaryAdmin(PassportSession session);

    protected void loadConfig() {

        ServiceLoader<ConfigProviderFactory> loader = ServiceLoader.load(ConfigProviderFactory.class, PassportApplication.class.getClassLoader());

        try {
            ConfigProviderFactory factory = loader.iterator().next();
            logger.debugv("ConfigProvider: {0}", factory.getClass().getName());
            Config.init(factory.create().orElseThrow(() -> new RuntimeException("Failed to load Passport configuration")));
        } catch (NoSuchElementException e) {
            throw new RuntimeException("No valid ConfigProvider found");
        }

    }

    protected abstract PassportSessionFactory createSessionFactory();

    public static PassportSessionFactory getSessionFactory() {
        return sessionFactory;
    }

}
