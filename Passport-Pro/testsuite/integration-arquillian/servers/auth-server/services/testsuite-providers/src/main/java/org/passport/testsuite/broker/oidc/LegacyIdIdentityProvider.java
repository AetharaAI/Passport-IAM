/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

import org.passport.broker.oidc.PassportOIDCIdentityProvider;
import org.passport.broker.oidc.OIDCIdentityProviderConfig;
import org.passport.broker.provider.BrokeredIdentityContext;
import org.passport.models.PassportSession;

/**
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public class LegacyIdIdentityProvider extends PassportOIDCIdentityProvider {
    public static final String LEGACY_ID = "3.14159265359";

    public LegacyIdIdentityProvider(PassportSession session, OIDCIdentityProviderConfig config) {
        super(session, config);
    }

    @Override
    public BrokeredIdentityContext getFederatedIdentity(String response) {
        BrokeredIdentityContext user = super.getFederatedIdentity(response);
        user.setLegacyId(LEGACY_ID);
        return user;
    }
}
