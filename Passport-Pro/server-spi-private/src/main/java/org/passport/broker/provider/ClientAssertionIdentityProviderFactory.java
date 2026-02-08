package org.passport.broker.provider;

import org.passport.authentication.ClientAuthenticationFlowContext;
import org.passport.models.ClientModel;
import org.passport.models.IdentityProviderModel;

public interface ClientAssertionIdentityProviderFactory {

    default ClientAssertionStrategy getClientAssertionStrategy() {
        return null;
    }

    interface ClientAssertionStrategy {

        boolean isSupportedAssertionType(String assertionType);

        LookupResult lookup(ClientAuthenticationFlowContext context) throws Exception;

    }

    record LookupResult(ClientModel clientModel, IdentityProviderModel identityProviderModel) {}

}
