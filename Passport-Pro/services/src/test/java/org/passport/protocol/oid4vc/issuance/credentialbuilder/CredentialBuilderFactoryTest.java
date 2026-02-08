package org.passport.protocol.oid4vc.issuance.credentialbuilder;

import java.util.List;

import org.passport.common.Profile;
import org.passport.common.Profile.Feature;
import org.passport.common.crypto.CryptoIntegration;
import org.passport.common.crypto.CryptoProvider;
import org.passport.common.profile.CommaSeparatedListProfileConfigResolver;
import org.passport.models.PassportSession;
import org.passport.services.resteasy.ResteasyPassportSession;
import org.passport.services.resteasy.ResteasyPassportSessionFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

public class CredentialBuilderFactoryTest {

    private static PassportSession session;

    @BeforeClass
    public static void beforeClass() {
        Profile.configure(new CommaSeparatedListProfileConfigResolver(Feature.OID4VC_VCI.getVersionedKey(), ""));
        CryptoIntegration.init(CryptoProvider.class.getClassLoader());
        ResteasyPassportSessionFactory factory = new ResteasyPassportSessionFactory();
        factory.init();
        session = new ResteasyPassportSession(factory);
    }

    @Test
    public void testVerifyNonNullConfigProperties() {
        List<CredentialBuilderFactory> credentialBuilderFactories = session
            .getPassportSessionFactory()
            .getProviderFactoriesStream(CredentialBuilder.class)
            .filter(CredentialBuilderFactory.class::isInstance)
            .map(CredentialBuilderFactory.class::cast)
            .toList();

        assertThat(credentialBuilderFactories, is(not(empty())));

        for (CredentialBuilderFactory credentialBuilderFactory : credentialBuilderFactories) {
            assertThat(credentialBuilderFactory.getConfigProperties(), notNullValue());
        }
    }
}
