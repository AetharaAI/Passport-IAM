package org.passport.authentication.authenticators.client;

import java.util.Map;

import org.passport.OAuth2Constants;
import org.passport.authentication.ClientAuthenticationFlowContext;
import org.passport.broker.provider.ClientAssertionIdentityProviderFactory;
import org.passport.cache.AlternativeLookupProvider;
import org.passport.models.ClientModel;
import org.passport.models.IdentityProviderModel;

public class DefaultClientAssertionStrategy implements ClientAssertionIdentityProviderFactory.ClientAssertionStrategy {

    @Override
    public boolean isSupportedAssertionType(String  assertionType) {
        return OAuth2Constants.CLIENT_ASSERTION_TYPE_JWT.equals(assertionType);
    }

    @Override
    public ClientAssertionIdentityProviderFactory.LookupResult lookup(ClientAuthenticationFlowContext context) throws Exception {
        ClientAssertionState clientAssertionState = context.getState(ClientAssertionState.class, ClientAssertionState.supplier());
        AlternativeLookupProvider lookupProvider = context.getSession().getProvider(AlternativeLookupProvider.class);

        String issuer = clientAssertionState.getToken().getIssuer();
        String federatedClientId =  clientAssertionState.getToken().getSubject();

        IdentityProviderModel identityProvider = lookupProvider.lookupIdentityProviderFromIssuer(context.getSession(), issuer);
        if (identityProvider == null) {
            return null;
        }

        ClientModel client = lookupProvider.lookupClientFromClientAttributes(
                context.getSession(),
                Map.of(
                        FederatedJWTClientAuthenticator.JWT_CREDENTIAL_SUBJECT_KEY, federatedClientId,
                        FederatedJWTClientAuthenticator.JWT_CREDENTIAL_ISSUER_KEY, identityProvider.getAlias()
                )
        );

        return new ClientAssertionIdentityProviderFactory.LookupResult(client, identityProvider);
    }

}
