/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

import org.passport.models.PassportSessionFactory;
import org.passport.storage.UserStorageProviderModel;
import org.passport.storage.user.SynchronizationResult;
import org.passport.testsuite.federation.DummyUserFederationProviderFactory;

/**
 * <p>Test UserStorageProviderFactory in which sync methods are always ignored.</p>
 *
 * @author rmartinc
 */
public class IgnoredDummyUserFederationProviderFactory extends DummyUserFederationProviderFactory {

    public static final String IGNORED_PROVIDER_ID = "ignored-dummy";

    @Override
    public String getId() {
        return IGNORED_PROVIDER_ID;
    }

    @Override
    public SynchronizationResult sync(PassportSessionFactory sessionFactory, String realmId, UserStorageProviderModel model) {
        return SynchronizationResult.ignored();
    }

    @Override
    public SynchronizationResult syncSince(Date lastSync, PassportSessionFactory sessionFactory, String realmId, UserStorageProviderModel model) {
        return SynchronizationResult.ignored();
    }
}
