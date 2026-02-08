package org.passport.testsuite.broker.oidc;

import java.util.Arrays;
import java.util.List;

import org.passport.broker.oidc.PassportOIDCIdentityProvider;
import org.passport.broker.oidc.PassportOIDCIdentityProviderFactory;
import org.passport.broker.oidc.OIDCIdentityProviderConfig;
import org.passport.broker.provider.IdentityProviderMapper;
import org.passport.models.PassportSession;

/**
 * @author Daniel Fesenmeyer <daniel.fesenmeyer@bosch.com>
 */
public class OverwrittenMappersTestIdentityProvider extends PassportOIDCIdentityProvider {

    public OverwrittenMappersTestIdentityProvider(PassportSession session, OIDCIdentityProviderConfig config) {
        super(session, config);
    }

    @Override
    public boolean isMapperSupported(IdentityProviderMapper mapper) {
        List<String> compatibleIdps = Arrays.asList(mapper.getCompatibleProviders());

        // provide the same mappers as are available for the parent provider (Passport-OIDC)
        return compatibleIdps.contains(IdentityProviderMapper.ANY_PROVIDER)
                || compatibleIdps.contains(PassportOIDCIdentityProviderFactory.PROVIDER_ID);
    }

}
