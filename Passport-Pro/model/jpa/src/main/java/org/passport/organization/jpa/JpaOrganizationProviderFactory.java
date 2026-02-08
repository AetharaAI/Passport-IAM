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

package org.passport.organization.jpa;

import org.passport.Config.Scope;
import org.passport.models.GroupModel;
import org.passport.models.GroupModel.GroupEvent;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.ModelValidationException;
import org.passport.organization.OrganizationProvider;
import org.passport.organization.OrganizationProviderFactory;
import org.passport.organization.utils.Organizations;
import org.passport.provider.ProviderEvent;

public class JpaOrganizationProviderFactory implements OrganizationProviderFactory {

    public static final String ID = "jpa";

    @Override
    public OrganizationProvider create(PassportSession session) {
        return new JpaOrganizationProvider(session);
    }

    @Override
    public void init(Scope config) {

    }

    @Override
    public void postInit(PassportSessionFactory factory) {
        factory.register(this::handleEvents);
    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return ID;
    }

    private void handleEvents(ProviderEvent e) {
        if (e instanceof GroupEvent event) {
            PassportSession session = event.getPassportSession();
            GroupModel group = event.getGroup();
            if (!Organizations.canManageOrganizationGroup(session, group)) {
                throw new ModelValidationException("Can not update organization group");
            }
        }
    }
}
