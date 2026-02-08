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

package org.passport.testsuite.federation.sync;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.PassportSessionTask;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.utils.PassportModelUtils;
import org.passport.provider.ProviderConfigProperty;
import org.passport.provider.ProviderConfigurationBuilder;
import org.passport.storage.UserStoragePrivateUtil;
import org.passport.storage.UserStorageProviderModel;
import org.passport.storage.user.SynchronizationResult;
import org.passport.testsuite.federation.DummyUserFederationProviderFactory;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SyncDummyUserFederationProviderFactory extends DummyUserFederationProviderFactory {

    // Used during SyncFederationTest
    public static volatile CountDownLatch latchStarted = new CountDownLatch(1);
    public static volatile CountDownLatch latchWait = new CountDownLatch(1);
    public static volatile CountDownLatch latchFinished = new CountDownLatch(1);

    public static void restartLatches() {
        latchStarted = new CountDownLatch(1);
        latchWait = new CountDownLatch(1);
        latchFinished = new CountDownLatch(1);
    }



    private static final Logger logger = Logger.getLogger(SyncDummyUserFederationProviderFactory.class);

    public static final String SYNC_PROVIDER_ID = "sync-dummy";
    public static final String WAIT_TIME = "wait-time"; // waitTime before transaction is commited

    @Override
    public String getId() {
        return SYNC_PROVIDER_ID;
    }


    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property().name("important.config")
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(WAIT_TIME)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .build();
    }


    @Override
    public SynchronizationResult syncSince(Date lastSync, PassportSessionFactory sessionFactory, String realmId, UserStorageProviderModel model) {
        if (latchStarted.getCount() <= 0) {
            logger.info("Already executed, returning");
            return SynchronizationResult.empty();
        }
        // we are starting => allow the test to continue
        latchStarted.countDown();

        PassportModelUtils.runJobInTransaction(sessionFactory, new PassportSessionTask() {

            @Override
            public void run(PassportSession session) {
                int waitTime = Integer.parseInt(model.getConfig().getFirst(WAIT_TIME));

                logger.infof("Starting sync of changed users. Wait time is: %s", waitTime);

                RealmModel realm = session.realms().getRealm(realmId);

                // PASSPORT-2412 : Just remove and add some users for testing purposes
                for (int i = 0; i < 10; i++) {
                    String username = "dummyuser-" + i;
                    UserModel user = UserStoragePrivateUtil.userLocalStorage(session).getUserByUsername(realm, username);

                    if (user != null) {
                        UserStoragePrivateUtil.userLocalStorage(session).removeUser(realm, user);
                    }

                    user = UserStoragePrivateUtil.userLocalStorage(session).addUser(realm, username);
                }

                logger.infof("Finished sync of changed users. Waiting now for %d seconds", waitTime);


                try {
                    // await the test to finish
                    latchWait.await(waitTime * 1000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted!", ie);
                }

                logger.infof("Finished waiting");
            }

        });

        // countDown, so the SyncFederationTest can finish
        latchFinished.countDown();

        return SynchronizationResult.empty();
    }

}
