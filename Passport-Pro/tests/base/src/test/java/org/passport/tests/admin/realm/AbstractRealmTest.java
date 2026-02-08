package org.passport.tests.admin.realm;

import org.passport.admin.client.Passport;
import org.passport.testframework.admin.AdminClientFactory;
import org.passport.testframework.annotations.InjectAdminClient;
import org.passport.testframework.annotations.InjectAdminClientFactory;
import org.passport.testframework.annotations.InjectAdminEvents;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.events.AdminEvents;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.remote.runonserver.InjectRunOnServer;
import org.passport.testframework.remote.runonserver.RunOnServerClient;

public class AbstractRealmTest {

    @InjectRealm(ref = "managedRealm")
    ManagedRealm managedRealm;

    @InjectAdminClient(ref = "managed", realmRef = "managedRealm")
    Passport adminClient;

    @InjectAdminClientFactory
    AdminClientFactory adminClientFactory;

    @InjectRunOnServer(ref = "managed", realmRef = "managedRealm")
    RunOnServerClient runOnServer;

    @InjectAdminEvents(ref = "managedEvents", realmRef = "managedRealm")
    AdminEvents adminEvents;
}
