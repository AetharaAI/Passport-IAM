package org.passport.authentication.authenticators.conditional;

import java.util.List;

import org.passport.Config.Scope;
import org.passport.models.AuthenticationExecutionModel;
import org.passport.models.AuthenticationExecutionModel.Requirement;
import org.passport.models.PassportSessionFactory;
import org.passport.provider.ProviderConfigProperty;

public class ConditionalUserConfiguredAuthenticatorFactory implements ConditionalAuthenticatorFactory {
    public static final String PROVIDER_ID = "conditional-user-configured";
    protected static final String CONDITIONAL_USER_ROLE = "condUserConfigured";

    @Override
    public void init(Scope config) {
        // no-op
    }

    @Override
    public void postInit(PassportSessionFactory factory) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Condition - user configured";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    private static final Requirement[] REQUIREMENT_CHOICES = {
        AuthenticationExecutionModel.Requirement.REQUIRED, AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Executes the current flow only if authenticators are configured";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    public ConditionalAuthenticator getSingleton() {
        return ConditionalUserConfiguredAuthenticator.SINGLETON;
    }
}
