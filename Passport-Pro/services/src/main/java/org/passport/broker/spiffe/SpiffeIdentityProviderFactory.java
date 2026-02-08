package org.passport.broker.spiffe;

import java.util.Map;

import org.passport.Config;
import org.passport.broker.provider.AbstractIdentityProviderFactory;
import org.passport.broker.provider.ClientAssertionIdentityProviderFactory;
import org.passport.common.Profile;
import org.passport.models.IdentityProviderModel;
import org.passport.models.PassportSession;
import org.passport.provider.EnvironmentDependentProviderFactory;

public class SpiffeIdentityProviderFactory extends AbstractIdentityProviderFactory<SpiffeIdentityProvider> implements EnvironmentDependentProviderFactory, ClientAssertionIdentityProviderFactory {

    public static final String PROVIDER_ID = "spiffe";

    @Override
    public String getName() {
        return "SPIFFE";
    }

    @Override
    public SpiffeIdentityProvider create(PassportSession session, IdentityProviderModel model) {
        return new SpiffeIdentityProvider(session, new SpiffeIdentityProviderConfig(model));
    }

    @Override
    public Map<String, String> parseConfig(PassportSession session, String configString) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IdentityProviderModel createConfig() {
        return new SpiffeIdentityProviderConfig();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.SPIFFE);
    }

    @Override
    public ClientAssertionStrategy getClientAssertionStrategy() {
        return new SpiffeClientAssertionStrategy();
    }

}
