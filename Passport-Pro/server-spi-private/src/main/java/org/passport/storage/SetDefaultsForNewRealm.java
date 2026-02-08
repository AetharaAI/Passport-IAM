/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.passport.storage;

import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.provider.ProviderEvent;

/**
 * Event to trigger that will add defaults for a realm after it has been imported.
 *
 * @author Alexander Schwartz
 */
public class SetDefaultsForNewRealm implements ProviderEvent {
    private final PassportSession session;
    private final RealmModel realmModel;

    public SetDefaultsForNewRealm(PassportSession session, RealmModel realmModel) {
        this.session = session;
        this.realmModel = realmModel;
    }

    public static void fire(PassportSession session, RealmModel realm) {
        SetDefaultsForNewRealm event = new SetDefaultsForNewRealm(session, realm);
        session.getPassportSessionFactory().publish(event);
    }

    public PassportSession getSession() {
        return session;
    }

    public RealmModel getRealmModel() {
        return realmModel;
    }
}
