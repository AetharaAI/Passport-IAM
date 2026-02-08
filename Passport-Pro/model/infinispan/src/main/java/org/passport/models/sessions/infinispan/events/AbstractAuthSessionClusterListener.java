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

package org.passport.models.sessions.infinispan.events;

import org.passport.cluster.ClusterEvent;
import org.passport.cluster.ClusterListener;
import org.passport.infinispan.util.InfinispanUtils;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.sessions.infinispan.InfinispanAuthenticationSessionProvider;
import org.passport.models.utils.PassportModelUtils;
import org.passport.sessions.AuthenticationSessionProvider;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AbstractAuthSessionClusterListener <SE extends SessionClusterEvent> implements ClusterListener {

    private static final Logger log = Logger.getLogger(AbstractAuthSessionClusterListener.class);

    private final PassportSessionFactory sessionFactory;

    public AbstractAuthSessionClusterListener(PassportSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    @Override
    public void eventReceived(ClusterEvent event) {
        PassportModelUtils.runJobInTransaction(sessionFactory, (PassportSession session) -> {
            InfinispanAuthenticationSessionProvider provider = (InfinispanAuthenticationSessionProvider) session.getProvider(AuthenticationSessionProvider.class,
                    InfinispanUtils.EMBEDDED_PROVIDER_ID);
            SE sessionEvent = (SE) event;

            if (!provider.getCache().getStatus().allowInvocations()) {
                log.debugf("Cache in state '%s' doesn't allow invocations", provider.getCache().getStatus());
                return;
            }

            log.debugf("Received authentication session event '%s'", sessionEvent.toString());

            eventReceived(provider, sessionEvent);

        });
    }

    protected abstract void eventReceived(InfinispanAuthenticationSessionProvider provider, SE sessionEvent);
}
