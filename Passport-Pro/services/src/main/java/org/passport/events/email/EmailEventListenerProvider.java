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

package org.passport.events.email;

import java.util.Set;

import org.passport.email.EmailException;
import org.passport.email.EmailTemplateProvider;
import org.passport.events.Event;
import org.passport.events.EventListenerProvider;
import org.passport.events.EventListenerTransaction;
import org.passport.events.EventType;
import org.passport.events.admin.AdminEvent;
import org.passport.http.HttpRequest;
import org.passport.models.ClientModel;
import org.passport.models.PassportContext;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.PassportSessionTask;
import org.passport.models.RealmModel;
import org.passport.models.RealmProvider;
import org.passport.models.UserModel;

import org.jboss.logging.Logger;

import static org.passport.models.utils.PassportModelUtils.runJobInTransaction;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class EmailEventListenerProvider implements EventListenerProvider {

    private static final Logger log = Logger.getLogger(EmailEventListenerProvider.class);

    private PassportSession session;
    private RealmProvider model;
    private Set<EventType> includedEvents;
    private EventListenerTransaction tx = new EventListenerTransaction(null, this::sendEmail);
    private final PassportSessionFactory sessionFactory;

    public EmailEventListenerProvider(PassportSession session, Set<EventType> includedEvents) {
        this.session = session;
        this.model = session.realms();
        this.includedEvents = includedEvents;
        this.session.getTransactionManager().enlistAfterCompletion(tx);
        this.sessionFactory = session.getPassportSessionFactory();
    }

    @Override
    public void onEvent(Event event) {
        if (includedEvents.contains(event.getType())) {
            if (event.getRealmId() != null && event.getUserId() != null) {
                tx.addEvent(event);
            }
        }
    }
    
    private void sendEmail(Event event) {
        HttpRequest request = session.getContext().getHttpRequest();

        runJobInTransaction(sessionFactory, new PassportSessionTask() {
            @Override
            public void run(PassportSession session) {
                PassportContext context = session.getContext();
                RealmModel realm = session.realms().getRealm(event.getRealmId());

                context.setRealm(realm);

                String clientId = event.getClientId();

                if (clientId != null) {
                    ClientModel client = realm.getClientByClientId(clientId);
                    context.setClient(client);
                }

                context.setHttpRequest(request);

                UserModel user = session.users().getUserById(realm, event.getUserId());

                if (user != null && user.getEmail() != null && user.isEmailVerified()) {
                    try {
                        EmailTemplateProvider emailTemplateProvider = session.getProvider(EmailTemplateProvider.class);
                        emailTemplateProvider.setRealm(realm).setUser(user).sendEvent(event);
                    } catch (EmailException e) {
                        log.error("Failed to send type mail", e);
                    }
                }
            }
        });
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {

    }

    @Override
    public void close() {
    }

}
