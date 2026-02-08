package org.passport.protocol.docker;

import java.util.Collections;
import java.util.List;

import org.passport.Config;
import org.passport.authentication.Authenticator;
import org.passport.authentication.AuthenticatorFactory;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.provider.ProviderConfigProperty;

import static org.passport.models.AuthenticationExecutionModel.Requirement;

public class DockerAuthenticatorFactory implements AuthenticatorFactory {

    @Override
    public String getHelpText() {
        return "Uses HTTP Basic authentication to validate docker users, returning a docker error token on auth failure";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    @Override
    public String getDisplayType() {
        return "Docker Authenticator";
    }

    @Override
    public String getReferenceCategory() {
        return "docker";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    private static final Requirement[] REQUIREMENT_CHOICES = {
            Requirement.REQUIRED,
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
    public Authenticator create(PassportSession session) {
        return new DockerAuthenticator();
    }

    @Override
    public void init(Config.Scope config) {
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
        return DockerAuthenticator.ID;
    }

}
