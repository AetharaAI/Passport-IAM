package org.passport.services.clientregistration.policy.impl;

import java.util.List;

import org.passport.component.ComponentModel;
import org.passport.models.PassportSession;
import org.passport.provider.ProviderConfigProperty;
import org.passport.services.clientregistration.policy.AbstractClientRegistrationPolicyFactory;
import org.passport.services.clientregistration.policy.ClientRegistrationPolicy;

public class RegistrationWebOriginsPolicyFactory extends AbstractClientRegistrationPolicyFactory {

    public static final String PROVIDER_ID = "registration-web-origins";

    public static final String WEB_ORIGINS = "web-origins";

    private static final ProviderConfigProperty WEB_ORIGINS_PROPERTY = new ProviderConfigProperty(WEB_ORIGINS, "registration-web-origins.label", "registration-web-origins.tooltip", ProviderConfigProperty.MULTIVALUED_STRING_TYPE, null);

    @Override
    public ClientRegistrationPolicy create(PassportSession session, ComponentModel model) {
        return new RegistrationWebOriginsPolicy(session, model);
    }

    @Override
    public String getHelpText() {
        return "Allowed web origins for client registration requests";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of(WEB_ORIGINS_PROPERTY);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
