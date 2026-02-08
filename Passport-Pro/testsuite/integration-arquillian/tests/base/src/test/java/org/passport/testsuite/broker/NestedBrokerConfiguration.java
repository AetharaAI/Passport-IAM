package org.passport.testsuite.broker;

import org.passport.representations.idm.IdentityProviderRepresentation;
import org.passport.representations.idm.RealmRepresentation;

public interface NestedBrokerConfiguration extends BrokerConfiguration {

    RealmRepresentation createSubConsumerRealm();

    String subConsumerRealmName();

    IdentityProviderRepresentation setUpConsumerIdentityProvider();

    String getSubConsumerIDPDisplayName();
}
