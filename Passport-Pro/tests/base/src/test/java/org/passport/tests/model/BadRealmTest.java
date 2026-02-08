package org.passport.tests.model;

import org.passport.models.PassportSession;
import org.passport.services.managers.RealmManager;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.remote.annotations.TestOnServer;
import org.passport.utils.ReservedCharValidator;

import static org.junit.jupiter.api.Assertions.fail;

@PassportIntegrationTest
public class BadRealmTest {

    @InjectRealm(attachTo = "master")
    ManagedRealm realm;

    private String name = "MyRealm";
    private String id = "MyId";
    private String script = "<script>alert(4)</script>";

    @TestOnServer
    public void testBadRealmName(PassportSession session) {
        RealmManager manager = new RealmManager(session);
        try {
            manager.createRealm(id, name + script);
            fail();
        } catch (ReservedCharValidator.ReservedCharException ex) {}
    }

    @TestOnServer
    public void testBadRealmId(PassportSession session) {
        RealmManager manager = new RealmManager(session);
        try {
            manager.createRealm(id + script, name);
            fail();
        } catch (ReservedCharValidator.ReservedCharException ex) {}
    }
}
