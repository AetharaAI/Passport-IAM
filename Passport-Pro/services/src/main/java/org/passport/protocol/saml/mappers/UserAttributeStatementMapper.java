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

package org.passport.protocol.saml.mappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.passport.dom.saml.v2.assertion.AttributeStatementType;
import org.passport.models.AuthenticatedClientSessionModel;
import org.passport.models.PassportSession;
import org.passport.models.ProtocolMapperModel;
import org.passport.models.UserModel;
import org.passport.models.UserSessionModel;
import org.passport.models.utils.PassportModelUtils;
import org.passport.protocol.ProtocolMapperUtils;
import org.passport.provider.ProviderConfigProperty;

/**
 * Mappings UserModel attribute (not property name of a getter method) to an AttributeStatement.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UserAttributeStatementMapper extends AbstractSAMLProtocolMapper implements SAMLAttributeStatementMapper {
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(ProtocolMapperUtils.USER_ATTRIBUTE);
        property.setLabel(ProtocolMapperUtils.USER_MODEL_ATTRIBUTE_LABEL);
        property.setHelpText(ProtocolMapperUtils.USER_MODEL_ATTRIBUTE_HELP_TEXT);
        property.setType(ProviderConfigProperty.USER_PROFILE_ATTRIBUTE_LIST_TYPE);
        property.setRequired(Boolean.TRUE);
        configProperties.add(property);
        AttributeStatementHelper.setConfigProperties(configProperties);

        property = new ProviderConfigProperty();
        property.setName(ProtocolMapperUtils.AGGREGATE_ATTRS);
        property.setLabel(ProtocolMapperUtils.AGGREGATE_ATTRS_LABEL);
        property.setHelpText(ProtocolMapperUtils.AGGREGATE_ATTRS_HELP_TEXT);
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        configProperties.add(property);
    }

    public static final String PROVIDER_ID = "saml-user-attribute-mapper";


    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "User Attribute";
    }

    @Override
    public String getDisplayCategory() {
        return AttributeStatementHelper.ATTRIBUTE_STATEMENT_CATEGORY;
    }

    @Override
    public String getHelpText() {
        return "Map a custom user attribute to a SAML attribute.";
    }

    @Override
    public void transformAttributeStatement(AttributeStatementType attributeStatement, ProtocolMapperModel mappingModel, PassportSession session, UserSessionModel userSession, AuthenticatedClientSessionModel clientSession) {
        UserModel user = userSession.getUser();
        String attributeName = mappingModel.getConfig().get(ProtocolMapperUtils.USER_ATTRIBUTE);
        boolean aggregateAttrs = Boolean.valueOf(mappingModel.getConfig().get(ProtocolMapperUtils.AGGREGATE_ATTRS));
        Collection<String> attributeValues = PassportModelUtils.resolveAttribute(user, attributeName, aggregateAttrs);
        if (attributeValues.isEmpty()) return;
        AttributeStatementHelper.addAttributes(attributeStatement, mappingModel, attributeValues);
    }

    public static ProtocolMapperModel createAttributeMapper(String name, String userAttribute,
                                                            String samlAttributeName, String nameFormat, String friendlyName) {
        String mapperId = PROVIDER_ID;
        return AttributeStatementHelper.createAttributeMapper(name, userAttribute, samlAttributeName, nameFormat, friendlyName,
                mapperId);

    }

}
