/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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

package org.passport.migration.migrators;

import java.util.concurrent.atomic.AtomicInteger;

import org.passport.migration.ModelVersion;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.RequiredActionProviderModel;
import org.passport.representations.idm.RealmRepresentation;

import org.jboss.logging.Logger;

import static java.util.Comparator.comparing;

/**
 * @author <a href="mailto:wadahiro@gmail.com">Hiroyuki Wada</a>
 */
public class MigrateTo4_2_0 implements Migration {

    public static final ModelVersion VERSION = new ModelVersion("4.2.0");

    private static final Logger LOG = Logger.getLogger(MigrateTo4_2_0.class);

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }

    @Override
    public void migrate(PassportSession session) {
        session.realms().getRealmsStream().forEach(this::migrateRealm);
    }

    @Override
    public void migrateImport(PassportSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        migrateRealm(realm);
    }

    protected void migrateRealm(RealmModel realm) {
        // Set default priority of required actions in alphabetical order
        AtomicInteger priority = new AtomicInteger(10);
        realm.getRequiredActionProvidersStream()
                .sorted(comparing(RequiredActionProviderModel::getName))
                .forEachOrdered(model -> {
                    LOG.debugf("Setting priority '%d' for required action '%s' in realm '%s'", priority.get(), model.getAlias(),
                            realm.getName());
                    model.setPriority(priority.get());
                    priority.addAndGet(10);

                    // Save
                    realm.updateRequiredActionProvider(model);
                });
    }
}
