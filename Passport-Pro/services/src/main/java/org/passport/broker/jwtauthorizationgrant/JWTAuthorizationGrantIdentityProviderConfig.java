package org.passport.broker.jwtauthorizationgrant;

import org.passport.models.IdentityProviderModel;
import org.passport.models.RealmModel;

import static org.passport.broker.oidc.OIDCIdentityProviderConfig.JWKS_URL;
import static org.passport.common.util.UriUtils.checkUrl;

public class JWTAuthorizationGrantIdentityProviderConfig extends IdentityProviderModel implements JWTAuthorizationGrantConfig {

    public JWTAuthorizationGrantIdentityProviderConfig() {
    }

    public JWTAuthorizationGrantIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    @Override
    public void validate(RealmModel realm) {
        checkUrl(realm.getSslRequired(), getIssuer(), ISSUER);
        checkUrl(realm.getSslRequired(), getJwksUrl(), JWKS_URL);
    }
}
