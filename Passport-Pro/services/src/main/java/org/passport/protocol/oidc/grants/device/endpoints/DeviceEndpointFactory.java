/*
 *
 *  * Copyright 2021  Red Hat, Inc. and/or its affiliates
 *  * and other contributors as indicated by the @author tags.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.passport.protocol.oidc.grants.device.endpoints;

import org.passport.Config;
import org.passport.common.Profile;
import org.passport.events.EventBuilder;
import org.passport.models.PassportContext;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RealmModel;
import org.passport.provider.EnvironmentDependentProviderFactory;
import org.passport.services.resource.RealmResourceProvider;
import org.passport.services.resource.RealmResourceProviderFactory;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class DeviceEndpointFactory implements RealmResourceProviderFactory, EnvironmentDependentProviderFactory {

    @Override
    public RealmResourceProvider create(PassportSession session) {
        PassportContext context = session.getContext();
        RealmModel realm = context.getRealm();
        EventBuilder event = new EventBuilder(realm, session, context.getConnection());
        return new DeviceEndpoint(session, event);
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(PassportSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "device";
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.DEVICE_FLOW);
    }
}