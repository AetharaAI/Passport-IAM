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

package org.passport.services.clientregistration.policy;

import java.util.List;

import org.passport.Config;
import org.passport.component.ComponentModel;
import org.passport.component.ComponentValidationException;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RealmModel;
import org.passport.provider.ProviderConfigProperty;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AbstractClientRegistrationPolicyFactory implements ClientRegistrationPolicyFactory {

    protected PassportSessionFactory sessionFactory;

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(PassportSessionFactory factory) {
        this.sessionFactory = factory;
    }

    @Override
    public void close() {
    }

    @Override
    public void validateConfiguration(PassportSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties(PassportSession session) {
        return getConfigProperties();
    }
}
