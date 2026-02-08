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

package org.passport.testsuite.rest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.passport.Config.Scope;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.representations.adapters.action.LogoutAction;
import org.passport.representations.adapters.action.PushNotBeforeAction;
import org.passport.representations.adapters.action.TestAvailabilityAction;
import org.passport.services.resource.RealmResourceProvider;
import org.passport.services.resource.RealmResourceProviderFactory;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class TestSamlApplicationResourceProviderFactory implements RealmResourceProviderFactory {

    private final BlockingQueue<LogoutAction> adminLogoutActions = new LinkedBlockingDeque<>();
    private final BlockingQueue<PushNotBeforeAction> pushNotBeforeActions = new LinkedBlockingDeque<>();
    private final BlockingQueue<TestAvailabilityAction> testAvailabilityActions = new LinkedBlockingDeque<>();

    @Override
    public RealmResourceProvider create(PassportSession session) {
        return new TestSamlApplicationResourceProvider(session, adminLogoutActions, pushNotBeforeActions, testAvailabilityActions);
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(PassportSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "saml-app";
    }
}
