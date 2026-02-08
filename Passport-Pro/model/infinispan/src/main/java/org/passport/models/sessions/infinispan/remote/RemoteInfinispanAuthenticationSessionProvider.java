/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.passport.models.sessions.infinispan.remote;

import java.util.Map;
import java.util.Objects;

import org.passport.cluster.ClusterProvider;
import org.passport.common.util.SecretGenerator;
import org.passport.common.util.Time;
import org.passport.models.ClientModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.cache.infinispan.events.AuthenticationSessionAuthNoteUpdateEvent;
import org.passport.models.sessions.infinispan.InfinispanAuthenticationSessionProviderFactory;
import org.passport.models.sessions.infinispan.entities.RootAuthenticationSessionEntity;
import org.passport.models.sessions.infinispan.remote.transaction.AuthenticationSessionChangeLogTransaction;
import org.passport.sessions.AuthenticationSessionCompoundId;
import org.passport.sessions.AuthenticationSessionProvider;
import org.passport.sessions.RootAuthenticationSessionModel;

public class RemoteInfinispanAuthenticationSessionProvider implements AuthenticationSessionProvider {

    private final PassportSession session;
    private final AuthenticationSessionChangeLogTransaction transaction;
    private final int authSessionsLimit;

    public RemoteInfinispanAuthenticationSessionProvider(PassportSession session, int authSessionsLimit, AuthenticationSessionChangeLogTransaction transaction) {
        this.session = Objects.requireNonNull(session);
        this.authSessionsLimit = authSessionsLimit;
        this.transaction = Objects.requireNonNull(transaction);
    }

    @Override
    public void close() {

    }

    @Override
    public RootAuthenticationSessionModel createRootAuthenticationSession(RealmModel realm) {
        return createRootAuthenticationSession(realm, SecretGenerator.SECURE_ID_GENERATOR.get());
    }

    @Override
    public RootAuthenticationSessionModel createRootAuthenticationSession(RealmModel realm, String id) {
        RootAuthenticationSessionEntity entity = new RootAuthenticationSessionEntity(id);
        entity.setRealmId(realm.getId());
        entity.setTimestamp(Time.currentTime());
        var updater = transaction.create(id, entity);
        updater.initialize(session, realm, authSessionsLimit);
        return updater;
    }

    @Override
    public RootAuthenticationSessionModel getRootAuthenticationSession(RealmModel realm, String authenticationSessionId) {
        var updater = transaction.get(authenticationSessionId);
        if(updater != null) {
            updater.initialize(session, realm, authSessionsLimit);
        }
        return updater;
    }

    @Override
    public void removeRootAuthenticationSession(RealmModel realm, RootAuthenticationSessionModel authenticationSession) {
        transaction.remove(authenticationSession.getId());
    }

    @Override
    public void onRealmRemoved(RealmModel realm) {
        transaction.removeByRealmId(realm.getId());
    }

    @Override
    public void onClientRemoved(RealmModel realm, ClientModel client) {
        // No update anything on clientRemove for now. AuthenticationSessions of removed client will be handled at runtime if needed.
    }

    @Override
    public void updateNonlocalSessionAuthNotes(AuthenticationSessionCompoundId compoundId, Map<String, String> authNotesFragment) {
        if (compoundId == null) {
            return;
        }

        session.getProvider(ClusterProvider.class).notify(
                InfinispanAuthenticationSessionProviderFactory.AUTHENTICATION_SESSION_EVENTS,
                AuthenticationSessionAuthNoteUpdateEvent.create(compoundId.getRootSessionId(), compoundId.getTabId(), authNotesFragment),
                true,
                ClusterProvider.DCNotify.ALL_BUT_LOCAL_DC
        );
    }
}
