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

package org.passport.models.cache.infinispan.organization;

import org.passport.Config.Scope;
import org.passport.models.IdentityProviderModel;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.OrganizationModel;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.organization.OrganizationProvider;
import org.passport.organization.OrganizationProviderFactory;

public class InfinispanOrganizationProviderFactory implements OrganizationProviderFactory {

    public static final String PROVIDER_ID = "infinispan";

    @Override
    public OrganizationProvider create(PassportSession session) {
        return new InfinispanOrganizationProvider(session);
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(PassportSessionFactory factory) {
        factory.register(e -> {
            if (e instanceof RealmModel.IdentityProviderUpdatedEvent event) {
                registerOrganizationInvalidation(event.getPassportSession(), event.getUpdatedIdentityProvider());
            }
            if (e instanceof RealmModel.IdentityProviderRemovedEvent event) {
                registerOrganizationInvalidation(event.getPassportSession(), event.getRemovedIdentityProvider());
            }
            if (e instanceof UserModel.UserPreRemovedEvent event) {
                PassportSession session = event.getPassportSession();
                InfinispanOrganizationProvider orgProvider = (InfinispanOrganizationProvider) session.getProvider(OrganizationProvider.class, getId());
                orgProvider.getByMember(event.getUser()).forEach(organization -> orgProvider.registerMemberInvalidation(organization, event.getUser()));
            }
        });
    }

    private void registerOrganizationInvalidation(PassportSession session, IdentityProviderModel idp) {
        if (idp.getOrganizationId() != null) {
            InfinispanOrganizationProvider orgProvider = (InfinispanOrganizationProvider) session.getProvider(OrganizationProvider.class, getId());
            if (orgProvider != null) {
                OrganizationModel organization = orgProvider.getById(idp.getOrganizationId());
                orgProvider.registerOrganizationInvalidation(organization);
            }
        }
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public int order() {
        return 10;
    }
}
