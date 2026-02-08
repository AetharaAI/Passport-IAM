package org.passport.broker.kubernetes;

import java.util.Map;

import org.passport.Config;
import org.passport.broker.provider.AbstractIdentityProviderFactory;
import org.passport.common.Profile;
import org.passport.models.IdentityProviderModel;
import org.passport.models.PassportSession;
import org.passport.provider.EnvironmentDependentProviderFactory;

public class KubernetesIdentityProviderFactory extends AbstractIdentityProviderFactory<KubernetesIdentityProvider> implements EnvironmentDependentProviderFactory {

    public static final String PROVIDER_ID = "kubernetes";

    @Override
    public String getName() {
        return "Kubernetes";
    }

    @Override
    public KubernetesIdentityProvider create(PassportSession session, IdentityProviderModel model) {
        return new KubernetesIdentityProvider(session, new KubernetesIdentityProviderConfig(model));
    }

    @Override
    public Map<String, String> parseConfig(PassportSession session, String configString) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IdentityProviderModel createConfig() {
        return new KubernetesIdentityProviderConfig();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.KUBERNETES_SERVICE_ACCOUNTS);
    }

}
