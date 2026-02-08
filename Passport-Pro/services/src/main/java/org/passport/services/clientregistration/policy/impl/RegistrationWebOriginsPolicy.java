package org.passport.services.clientregistration.policy.impl;

import java.util.List;

import org.passport.component.ComponentModel;
import org.passport.models.ClientModel;
import org.passport.models.PassportSession;
import org.passport.services.clientregistration.ClientRegistrationContext;
import org.passport.services.clientregistration.ClientRegistrationProvider;
import org.passport.services.clientregistration.policy.ClientRegistrationPolicy;
import org.passport.services.clientregistration.policy.ClientRegistrationPolicyException;
import org.passport.services.cors.Cors;

public class RegistrationWebOriginsPolicy implements ClientRegistrationPolicy {

    private final PassportSession session;
    private final List<String> allowedWebOrigins;

    public RegistrationWebOriginsPolicy(PassportSession session, ComponentModel model) {
        this.session = session;
        allowedWebOrigins = model.getConfig().getList(RegistrationWebOriginsPolicyFactory.WEB_ORIGINS);
    }

    @Override
    public void beforeRegister(ClientRegistrationContext context) throws ClientRegistrationPolicyException {
        addOrigins();
    }

    @Override
    public void afterRegister(ClientRegistrationContext context, ClientModel clientModel) {
    }

    @Override
    public void beforeUpdate(ClientRegistrationContext context, ClientModel clientModel) throws ClientRegistrationPolicyException {
        addOrigins();
    }

    @Override
    public void afterUpdate(ClientRegistrationContext context, ClientModel clientModel) {
    }

    @Override
    public void beforeView(ClientRegistrationProvider provider, ClientModel clientModel) throws ClientRegistrationPolicyException {
        addOrigins();
    }

    @Override
    public void beforeDelete(ClientRegistrationProvider provider, ClientModel clientModel) throws ClientRegistrationPolicyException {
        addOrigins();
    }

    private void addOrigins() {
        if (allowedWebOrigins != null && !allowedWebOrigins.isEmpty()) {
            session.getProvider(Cors.class).addAllowedOrigins(allowedWebOrigins);
        }
    }

}
