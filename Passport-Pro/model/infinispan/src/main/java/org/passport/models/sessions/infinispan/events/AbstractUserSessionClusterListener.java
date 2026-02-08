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
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.utils.PassportModelUtils;
import org.passport.provider.Provider;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AbstractUserSessionClusterListener<SE extends SessionClusterEvent, T extends Provider> implements ClusterListener {

    private static final Logger log = Logger.getLogger(AbstractUserSessionClusterListener.class);

    private final PassportSessionFactory sessionFactory;

    private final Class<T> providerClazz;

    public AbstractUserSessionClusterListener(PassportSessionFactory sessionFactory, Class<T> providerClazz) {
        this.sessionFactory = sessionFactory;
        this.providerClazz = providerClazz;
    }


    @Override
    public void eventReceived(ClusterEvent event) {
        PassportModelUtils.runJobInTransaction(sessionFactory, (PassportSession session) -> {
            T provider = session.getProvider(providerClazz);
            SE sessionEvent = (SE) event;

            if (log.isDebugEnabled()) {
                log.debugf("Received user session event '%s'.", sessionEvent.toString());
            }

            eventReceived(provider, sessionEvent);
        });
    }

    protected abstract void eventReceived(T provider, SE sessionEvent);
}
