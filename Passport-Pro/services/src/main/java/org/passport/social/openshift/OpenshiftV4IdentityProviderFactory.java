package org.passport.social.openshift;

import java.util.List;

import org.passport.broker.provider.AbstractIdentityProviderFactory;
import org.passport.broker.social.SocialIdentityProviderFactory;
import org.passport.models.IdentityProviderModel;
import org.passport.models.PassportSession;
import org.passport.provider.ProviderConfigProperty;

/**
 * OpenShift 4 Identity Provider factory class.
 *
 * @author David Festal and Sebastian Łaskawiec
 */
public class OpenshiftV4IdentityProviderFactory extends AbstractIdentityProviderFactory<OpenshiftV4IdentityProvider> implements SocialIdentityProviderFactory<OpenshiftV4IdentityProvider> {

    public static final String PROVIDER_ID = "openshift-v4";
    public static final String NAME = "Openshift v4";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public OpenshiftV4IdentityProvider create(PassportSession passportSession, IdentityProviderModel identityProviderModel) {
        return new OpenshiftV4IdentityProvider(passportSession, new OpenshiftV4IdentityProviderConfig(identityProviderModel));
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public OpenshiftV4IdentityProviderConfig createConfig() {
        return new OpenshiftV4IdentityProviderConfig();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return OpenshiftV4IdentityProviderConfig.getConfigProperties();
    }
}
