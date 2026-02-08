package org.passport.testsuite.authentication;

import org.passport.Config;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.protocol.saml.ArtifactResolver;
import org.passport.protocol.saml.ArtifactResolverFactory;
import org.passport.protocol.saml.util.ArtifactBindingUtils;

/**
 * This ArtifactResolver should be used only for testing purposes.
 */
public class CustomTestingSamlArtifactResolverFactory implements ArtifactResolverFactory {

    public  static final byte[] TYPE_CODE = {0, 5};
    public static final CustomTestingSamlArtifactResolver resolver = new CustomTestingSamlArtifactResolver();
    
    @Override
    public ArtifactResolver create(PassportSession session) {
        return resolver;
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
    public String getId() {
        return ArtifactBindingUtils.byteArrayToResolverProviderId(TYPE_CODE);
    }
}
