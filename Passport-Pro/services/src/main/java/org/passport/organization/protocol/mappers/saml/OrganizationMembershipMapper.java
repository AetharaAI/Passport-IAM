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

package org.passport.organization.protocol.mappers.saml;

import java.util.List;
import java.util.stream.Stream;

import org.passport.Config.Scope;
import org.passport.common.Profile;
import org.passport.common.Profile.Feature;
import org.passport.dom.saml.v2.assertion.AttributeStatementType;
import org.passport.dom.saml.v2.assertion.AttributeType;
import org.passport.models.AuthenticatedClientSessionModel;
import org.passport.models.PassportSession;
import org.passport.models.OrganizationModel;
import org.passport.models.ProtocolMapperModel;
import org.passport.models.UserModel;
import org.passport.models.UserSessionModel;
import org.passport.organization.OrganizationProvider;
import org.passport.protocol.saml.SamlProtocol;
import org.passport.protocol.saml.mappers.AbstractSAMLProtocolMapper;
import org.passport.protocol.saml.mappers.AttributeStatementHelper;
import org.passport.protocol.saml.mappers.SAMLAttributeStatementMapper;
import org.passport.provider.EnvironmentDependentProviderFactory;
import org.passport.provider.ProviderConfigProperty;
import org.passport.saml.common.constants.JBossSAMLURIConstants;

import static org.passport.organization.utils.Organizations.isEnabledAndOrganizationsPresent;

public class OrganizationMembershipMapper extends AbstractSAMLProtocolMapper implements SAMLAttributeStatementMapper, EnvironmentDependentProviderFactory {

    public static final String ID = "saml-organization-membership-mapper";
    public static final String ORGANIZATION_ATTRIBUTE_NAME = "organization";

    public static ProtocolMapperModel create() {
        ProtocolMapperModel mapper = new ProtocolMapperModel();

        mapper.setName("organization");
        mapper.setProtocolMapper(ID);
        mapper.setProtocol(SamlProtocol.LOGIN_PROTOCOL);

        return mapper;

    }

    @Override
    public void transformAttributeStatement(AttributeStatementType attributeStatement, ProtocolMapperModel mappingModel, PassportSession session, UserSessionModel userSession, AuthenticatedClientSessionModel clientSession) {
        OrganizationProvider provider = session.getProvider(OrganizationProvider.class);

        if (!isEnabledAndOrganizationsPresent(provider)) {
            return;
        }

        UserModel user = userSession.getUser();
        Stream<OrganizationModel> organizations = provider.getByMember(user).filter(OrganizationModel::isEnabled);
        AttributeType attribute = new AttributeType(ORGANIZATION_ATTRIBUTE_NAME);

        attribute.setFriendlyName(ORGANIZATION_ATTRIBUTE_NAME);
        attribute.setNameFormat(JBossSAMLURIConstants.ATTRIBUTE_FORMAT_BASIC.get());

        organizations.forEach(organization -> {
            attribute.addAttributeValue(organization.getAlias());
        });

        if (attribute.getAttributeValue().isEmpty()) {
            return;
        }

        attributeStatement.addAttribute(new AttributeStatementType.ASTChoiceType(attribute));
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayType() {
        return "Organization Membership";
    }

    @Override
    public String getDisplayCategory() {
        return AttributeStatementHelper.ATTRIBUTE_STATEMENT_CATEGORY;
    }

    @Override
    public String getHelpText() {
        return "Add an attribute to the assertion with information about the organization membership.";
    }

    @Override
    public boolean isSupported(Scope config) {
        return Profile.isFeatureEnabled(Feature.ORGANIZATION);
    }
}
