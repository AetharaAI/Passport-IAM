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
package org.passport.utils;

import java.util.List;

import org.passport.authentication.RequiredActionFactory;
import org.passport.authentication.RequiredActionProvider;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.RequiredActionProviderModel;
import org.passport.models.utils.Base32;
import org.passport.provider.ProviderConfigProperty;

/**
 * Helpers for managing RequiredActions.
 */
public class RequiredActionHelper {

    private RequiredActionHelper() {}

    public static RequiredActionFactory getConfigurableRequiredActionFactory(PassportSession session, String providerId) {
        RequiredActionFactory providerFactory = (RequiredActionFactory)session.getPassportSessionFactory().getProviderFactory(RequiredActionProvider.class, providerId);

        if (providerFactory == null) {
            return null;
        }

        List<ProviderConfigProperty> configMetadata = providerFactory.getConfigMetadata();
        if (configMetadata != null && !configMetadata.isEmpty()) {
            return providerFactory;
        }

        return null;
    }

    public static RequiredActionFactory lookupConfigurableRequiredActionFactory(PassportSession session, String providerId) {

        RequiredActionFactory factory = getConfigurableRequiredActionFactory(session, providerId);

        if (factory == null) {
            providerId = new String(Base32.decode(providerId));
            factory = getConfigurableRequiredActionFactory(session, providerId);
        }

        return factory;
    }

    public static RequiredActionProviderModel getRequiredActionByProviderId(RealmModel realm, String providerId) {
        return realm.getRequiredActionProvidersStream() //
                .filter(action -> action.getProviderId().equals(providerId)) //
                .findFirst() //
                .orElse(null);
    }
}
