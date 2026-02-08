package org.passport.broker.jwtauthorizationgrant;

import java.util.Map;

import org.passport.Config;
import org.passport.broker.provider.AbstractIdentityProviderFactory;
import org.passport.common.Profile;
import org.passport.models.IdentityProviderModel;
import org.passport.models.PassportSession;
import org.passport.provider.EnvironmentDependentProviderFactory;

public class JWTAuthorizationGrantIdentityProviderFactory extends AbstractIdentityProviderFactory<JWTAuthorizationGrantIdentityProvider> implements EnvironmentDependentProviderFactory {

    public static final String PROVIDER_ID = "jwt-authorization-grant";

    @Override
    public String getName() {
        return "JWT Authorization Grant";
    }

    @Override
    public JWTAuthorizationGrantIdentityProvider create(PassportSession session, IdentityProviderModel model) {
        return new JWTAuthorizationGrantIdentityProvider(session, new JWTAuthorizationGrantIdentityProviderConfig(model));
    }

    @Override
    public Map<String, String> parseConfig(PassportSession session, String configString) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IdentityProviderModel createConfig() {
        return new JWTAuthorizationGrantIdentityProviderConfig();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.JWT_AUTHORIZATION_GRANT);
    }

}
