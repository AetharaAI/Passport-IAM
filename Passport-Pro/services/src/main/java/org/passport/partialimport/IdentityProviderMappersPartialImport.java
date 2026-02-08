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

package org.passport.partialimport;

import java.util.List;

import org.passport.models.IdentityProviderMapperModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.utils.RepresentationToModel;
import org.passport.representations.idm.IdentityProviderMapperRepresentation;
import org.passport.representations.idm.PartialImportRepresentation;

/**
 * PartialImport handler for Identity Provider Mappers.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2016 Red Hat Inc.
 */
public class IdentityProviderMappersPartialImport extends AbstractPartialImport<IdentityProviderMapperRepresentation> {

    @Override
    public List<IdentityProviderMapperRepresentation> getRepList(PartialImportRepresentation partialImportRep) {
        return partialImportRep.getIdentityProviderMappers();
    }

    @Override
    public String getName(IdentityProviderMapperRepresentation idpMapperRep) {
        return idpMapperRep.getName();
    }

    @Override
    public String getModelId(RealmModel realm, PassportSession session, IdentityProviderMapperRepresentation idpMapperRep) {
        return session.identityProviders().getMapperByName(idpMapperRep.getIdentityProviderAlias(), idpMapperRep.getName()).getId();
    }

    @Override
    public boolean exists(RealmModel realm, PassportSession session, IdentityProviderMapperRepresentation idpMapperRep) {
        return session.identityProviders().getMapperByName(idpMapperRep.getIdentityProviderAlias(), idpMapperRep.getName()) != null;
    }

    @Override
    public String existsMessage(RealmModel realm, IdentityProviderMapperRepresentation idpMapperRep) {
        return "Identity Provider Mapper'" + getName(idpMapperRep) + "' already exists.";
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.IDP_MAPPER;
    }

    @Override
    public void remove(RealmModel realm, PassportSession session, IdentityProviderMapperRepresentation idpMapperRep) {
        IdentityProviderMapperModel idpMapper = session.identityProviders().getMapperByName(idpMapperRep.getIdentityProviderAlias(), idpMapperRep.getName());
        if (idpMapper != null) {
            session.identityProviders().removeMapper(idpMapper);
        }
    }

    @Override
    public void create(RealmModel realm, PassportSession session, IdentityProviderMapperRepresentation idpMapperRep) {
        IdentityProviderMapperModel existing = session.identityProviders().getMapperByName(idpMapperRep.getIdentityProviderAlias(), idpMapperRep.getName());
        if (existing != null) {
            session.identityProviders().removeMapper(existing);
        }
        IdentityProviderMapperModel idpMapper = RepresentationToModel.toModel(idpMapperRep);
        session.identityProviders().createMapper(idpMapper);
    }

}
