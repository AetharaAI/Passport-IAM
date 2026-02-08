package org.passport.testsuite.theme;

import org.passport.representations.idm.RealmRepresentation;
import org.passport.services.resource.AccountResourceProvider;
import org.passport.testsuite.AbstractTestRealmPassportTest;

import org.junit.Assert;
import org.junit.Test;

public class CustomAccountResourceProviderTest extends AbstractTestRealmPassportTest {

    @Override
    public void configureTestRealm(RealmRepresentation testRealm) {

    }

    @Test
    public void testProviderOverride() {
        testingClient.server().run(session -> {
            AccountResourceProvider arp = session.getProvider(AccountResourceProvider.class, "ext-custom-account-console");
            Assert.assertTrue(arp instanceof CustomAccountResourceProviderFactory);
        });
    }

}
