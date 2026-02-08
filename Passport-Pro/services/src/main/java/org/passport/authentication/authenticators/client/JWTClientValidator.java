package org.passport.authentication.authenticators.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.passport.authentication.ClientAuthenticationFlowContext;
import org.passport.protocol.LoginProtocol;
import org.passport.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.passport.protocol.oidc.OIDCLoginProtocol;
import org.passport.protocol.oidc.OIDCLoginProtocolService;
import org.passport.protocol.oidc.OIDCProviderConfig;
import org.passport.protocol.oidc.grants.ciba.CibaGrantType;
import org.passport.protocol.oidc.par.endpoints.ParEndpoint;
import org.passport.services.Urls;

public class JWTClientValidator extends AbstractJWTClientValidator {

    public JWTClientValidator(ClientAuthenticationFlowContext context, SignatureValidator signatureValidator, String clientAuthenticatorProviderId) throws Exception {
        super(context, signatureValidator, clientAuthenticatorProviderId);
    }

    @Override
    protected String getExpectedTokenIssuer() {
        return clientAssertionState.getToken().getSubject();
    }

    @Override
    protected List<String> getExpectedAudiences() {
        String issuerUrl = Urls.realmIssuer(context.getUriInfo().getBaseUri(), realm.getName());
        String tokenUrl = OIDCLoginProtocolService.tokenUrl(context.getUriInfo().getBaseUriBuilder()).build(realm.getName()).toString();
        String tokenIntrospectUrl = OIDCLoginProtocolService.tokenIntrospectionUrl(context.getUriInfo().getBaseUriBuilder()).build(realm.getName()).toString();
        String parEndpointUrl = ParEndpoint.parUrl(context.getUriInfo().getBaseUriBuilder()).build(realm.getName()).toString();
        List<String> expectedAudiences = new ArrayList<>(Arrays.asList(issuerUrl, tokenUrl, tokenIntrospectUrl, parEndpointUrl));
        String backchannelAuthenticationUrl = CibaGrantType.authorizationUrl(context.getUriInfo().getBaseUriBuilder()).build(realm.getName()).toString();
        expectedAudiences.add(backchannelAuthenticationUrl);

        return expectedAudiences;
    }

    @Override
    protected boolean isMultipleAudienceAllowed() {
        OIDCLoginProtocol loginProtocol = (OIDCLoginProtocol) context.getSession().getProvider(LoginProtocol.class, OIDCLoginProtocol.LOGIN_PROTOCOL);
        OIDCProviderConfig config = loginProtocol.getConfig();
        return config.isAllowMultipleAudiencesForJwtClientAuthentication();
    }

    @Override
    protected int getAllowedClockSkew() {
        return 15;
    }

    protected int getMaximumExpirationTime() {
        return OIDCAdvancedConfigWrapper.fromClientModel(clientAssertionState.getClient()).getTokenEndpointAuthSigningMaxExp();
    }

    @Override
    protected boolean isReusePermitted() {
        return false;
    }

    @Override
    protected String getExpectedSignatureAlgorithm() {
        return OIDCAdvancedConfigWrapper.fromClientModel(clientAssertionState.getClient()).getTokenEndpointAuthSigningAlg();
    }

}
