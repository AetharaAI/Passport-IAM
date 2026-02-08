package org.passport.broker.provider;

import org.passport.authentication.ClientAuthenticationFlowContext;
import org.passport.models.IdentityProviderModel;

public interface ClientAssertionIdentityProvider<C extends IdentityProviderModel> extends IdentityProvider<C> {

    boolean verifyClientAssertion(ClientAuthenticationFlowContext context) throws Exception;

}
