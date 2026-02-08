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

package org.passport.testsuite.broker.oidc;

import org.passport.Config.Scope;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.services.resource.RealmResourceProvider;
import org.passport.services.resource.RealmResourceProviderFactory;

public class MissingUseJwksRealmResourceProviderFactory implements RealmResourceProviderFactory {

    public static final String ID = "missing-use-jwks";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public RealmResourceProvider create(PassportSession session) {
        return new MissingUseJwksRealmResourceProvider(session);
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

}
