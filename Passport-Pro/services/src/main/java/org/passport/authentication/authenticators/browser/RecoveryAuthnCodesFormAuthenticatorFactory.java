package org.passport.authentication.authenticators.browser;

import java.util.List;

import org.passport.Config;
import org.passport.authentication.Authenticator;
import org.passport.authentication.AuthenticatorFactory;
import org.passport.authentication.ConfigurableAuthenticatorFactory;
import org.passport.common.Profile;
import org.passport.models.AuthenticationExecutionModel;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.credential.RecoveryAuthnCodesCredentialModel;
import org.passport.provider.EnvironmentDependentProviderFactory;
import org.passport.provider.ProviderConfigProperty;

public class RecoveryAuthnCodesFormAuthenticatorFactory implements AuthenticatorFactory, EnvironmentDependentProviderFactory {

    public static final String PROVIDER_ID = "auth-recovery-authn-code-form";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Recovery Authentication Code Form";
    }

    @Override
    public String getReferenceCategory() {
        return RecoveryAuthnCodesCredentialModel.TYPE;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return ConfigurableAuthenticatorFactory.REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public String getHelpText() {
        return "Validates a Recovery Authentication Code";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    public Authenticator create(PassportSession passportSession) {
        return new RecoveryAuthnCodesFormAuthenticator(passportSession);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(PassportSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.RECOVERY_CODES);
    }
}
