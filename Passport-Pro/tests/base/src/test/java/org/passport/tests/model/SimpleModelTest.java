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

package org.passport.tests.model;

import org.passport.models.Constants;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.utils.PassportModelUtils;
import org.passport.services.managers.RealmManager;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.remote.annotations.TestOnServer;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@PassportIntegrationTest
public class SimpleModelTest {

    private static final Logger log = Logger.getLogger(SimpleModelTest.class);

    @InjectRealm(attachTo = "master")
    ManagedRealm realm;

    @TestOnServer
    public void simpleModelTestWithNestedTransactions(PassportSession session) {
        log.debug("simpleModelTestWithNestedTransactions");

        // Transaction 1
        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), (PassportSession session1) -> {

            RealmModel realm = session1.realms().createRealm("foo");
            realm.setDefaultRole(session1.roles().addRealmRole(realm, Constants.DEFAULT_ROLES_ROLE_PREFIX + "-" + realm.getName()));

        });

        // Transaction 2 - should be able to see the created realm. Update it
        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), (PassportSession session2) -> {

            RealmModel realm = session2.realms().getRealmByName("foo");
            Assertions.assertNotNull(realm);
            session2.getContext().setRealm(realm);

            realm.setAttribute("bar", "baz");

        });

        // Transaction 3 - Doublecheck update is visible. Then rollback transaction!
        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), (PassportSession session3) -> {

            RealmModel realm = session3.realms().getRealmByName("foo");
            Assertions.assertNotNull(realm);
            session3.getContext().setRealm(realm);

            String attrValue = realm.getAttribute("bar");
            Assertions.assertEquals("baz", attrValue);

            realm.setAttribute("bar", "baz2");

            session3.getTransactionManager().setRollbackOnly();
        });

        // Transaction 4 - should still see the old value of attribute. Delete realm
        PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), (PassportSession session4) -> {

            RealmModel realm = session4.realms().getRealmByName("foo");
            Assertions.assertNotNull(realm);
            session4.getContext().setRealm(realm);

            String attrValue = realm.getAttribute("bar");
            Assertions.assertEquals("baz", attrValue);

            new RealmManager(session4).removeRealm(realm);
        });
    }

}
