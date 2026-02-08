package org.passport.protocol.saml.mappers;

import java.util.ArrayList;
import java.util.List;

import org.passport.models.AuthenticatedClientSessionModel;
import org.passport.models.PassportSession;
import org.passport.models.ProtocolMapperModel;
import org.passport.models.UserSessionModel;
import org.passport.protocol.ProtocolMapperUtils;
import org.passport.provider.ProviderConfigProperty;

public class UserAttributeNameIdMapper extends AbstractSAMLProtocolMapper implements SAMLNameIdMapper {

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        NameIdMapperHelper.setConfigProperties(configProperties);
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(ProtocolMapperUtils.USER_ATTRIBUTE);
        property.setLabel(ProtocolMapperUtils.USER_MODEL_ATTRIBUTE_LABEL);
        property.setHelpText(ProtocolMapperUtils.USER_MODEL_ATTRIBUTE_HELP_TEXT);
        property.setType(ProviderConfigProperty.USER_PROFILE_ATTRIBUTE_LIST_TYPE);
        property.setRequired(Boolean.TRUE);
        configProperties.add(property);
    }

    public static final String PROVIDER_ID = "saml-user-attribute-nameid-mapper";

    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "User Attribute Mapper For NameID";
    }

    @Override
    public String getDisplayCategory() {
        return "NameID Mapper";
    }

    @Override
    public String getHelpText() {
        return "Map user attribute to SAML NameID value.";
    }

    @Override
    public String mapperNameId(String nameIdFormat, ProtocolMapperModel mappingModel, PassportSession session,
            UserSessionModel userSession, AuthenticatedClientSessionModel clientSession) {
        return userSession.getUser().getFirstAttribute(mappingModel.getConfig().get(ProtocolMapperUtils.USER_ATTRIBUTE));
    }

}
