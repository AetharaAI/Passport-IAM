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

package org.passport.testsuite.util.cli;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.passport.models.AuthenticatedClientSessionModel;
import org.passport.models.ClientModel;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionTask;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.UserSessionModel;
import org.passport.models.session.UserSessionPersisterProvider;
import org.passport.models.utils.PassportModelUtils;
import org.passport.services.managers.UserSessionManager;


/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class PersistSessionsCommand extends AbstractCommand {

    private AtomicInteger userCounter = new AtomicInteger();

    @Override
    public String getName() {
        return "persistSessions";
    }

    @Override
    public void doRunCommand(PassportSession sess) {
        final int count = getIntArg(0);
        final int batchCount = getIntArg(1);

        int remaining = count;

        while (remaining > 0) {
            int createInThisBatch = Math.min(batchCount, remaining);
            createSessionsBatch(createInThisBatch);
            remaining = remaining - createInThisBatch;
        }

        // Write some summary
        PassportModelUtils.runJobInTransaction(sessionFactory, new PassportSessionTask() {

            @Override
            public void run(PassportSession session) {
                UserSessionPersisterProvider persister = session.getProvider(UserSessionPersisterProvider.class);
                log.info("Command finished. Total number of sessions in persister: " + persister.getUserSessionsCount(true));
            }

        });
    }


    private void createSessionsBatch(final int countInThisBatch) {
        final List<String> userSessionIds = new LinkedList<>();

        PassportModelUtils.runJobInTransaction(sessionFactory, new PassportSessionTask() {

            @Override
            public void run(PassportSession session) {
                RealmModel realm = session.realms().getRealmByName("master");

                ClientModel testApp = realm.getClientByClientId("security-admin-console");
                UserSessionPersisterProvider persister = session.getProvider(UserSessionPersisterProvider.class);
                UserSessionManager userSessionManager = new UserSessionManager(session);

                for (int i = 0; i < countInThisBatch; i++) {
                    String username = "john-" + userCounter.incrementAndGet();
                    UserModel john = session.users().getUserByUsername(realm, username);
                    if (john == null) {
                        john = session.users().addUser(realm, username);
                    }

                    UserSessionModel userSession = userSessionManager.createUserSession(realm, john, username, "127.0.0.2", "form", true, null, null);
                    AuthenticatedClientSessionModel clientSession = session.sessions().createClientSession(realm, testApp, userSession);
                    clientSession.setRedirectUri("http://redirect");
                    clientSession.setNote("foo", "bar-" + i);
                    userSessionIds.add(userSession.getId());
                }
            }

        });

        log.infof("%d sessions created in infinispan storage", countInThisBatch);

        // Persist them now

        PassportModelUtils.runJobInTransaction(sessionFactory, new PassportSessionTask() {

            @Override
            public void run(PassportSession session) {
                RealmModel realm = session.realms().getRealmByName("master");
                ClientModel testApp = realm.getClientByClientId("security-admin-console");
                UserSessionPersisterProvider persister = session.getProvider(UserSessionPersisterProvider.class);

                int counter = 0;
                for (String userSessionId : userSessionIds) {
                    counter++;
                    UserSessionModel userSession = session.sessions().getUserSession(realm, userSessionId);
                    persister.createUserSession(userSession, true);

                    AuthenticatedClientSessionModel clientSession = userSession.getAuthenticatedClientSessions().get(testApp.getId());
                    persister.createClientSession(clientSession, true);
                }

                log.infof("%d user sessions persisted. Continue", counter);
            }

        });
    }

    @Override
    public String printUsage() {
        return super.printUsage() + " <sessions-count> <sessions-count-per-each-transaction>";
    }
}
