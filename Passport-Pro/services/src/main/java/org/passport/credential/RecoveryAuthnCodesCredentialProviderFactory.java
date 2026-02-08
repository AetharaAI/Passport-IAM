package org.passport.credential;

import org.passport.Config;
import org.passport.common.Profile;
import org.passport.models.PassportSession;
import org.passport.provider.EnvironmentDependentProviderFactory;

public class RecoveryAuthnCodesCredentialProviderFactory
        implements CredentialProviderFactory<RecoveryAuthnCodesCredentialProvider>, EnvironmentDependentProviderFactory {

    public static final String PROVIDER_ID = "passport-recovery-authn-codes";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public RecoveryAuthnCodesCredentialProvider create(PassportSession session) {
        return new RecoveryAuthnCodesCredentialProvider(session);
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.RECOVERY_CODES);
    }
}
