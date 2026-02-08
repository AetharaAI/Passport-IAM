package org.passport.tests.admin.realm;

import jakarta.ws.rs.BadRequestException;

import org.passport.admin.client.Passport;
import org.passport.models.Constants;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.tests.utils.Assert;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@PassportIntegrationTest
public class RealmRemoveTest extends AbstractRealmTest {

    @Test
    public void removeRealm() {
        RealmRepresentation realmRep = managedRealm.admin().toRepresentation();
        adminClient.realm(managedRealm.getName()).remove();

        Assert.assertNames(adminClient.realms().findAll(), "master");

        adminClient.realms().create(realmRep);
    }

    @Test
    public void removeMasterRealm() {
        // any attempt to remove the master realm should fail.
        try {
            adminClient.realm("master").remove();
            fail("It should not be possible to remove the master realm");
        } catch(BadRequestException ignored) {
        }
    }

    @Test
    public void loginAfterRemoveRealm() {
        RealmRepresentation realmRep = managedRealm.admin().toRepresentation();
        adminClient.realm(managedRealm.getName()).remove();

        try (Passport client = adminClientFactory.create().realm("master")
                .username("admin").password("admin").clientId(Constants.ADMIN_CLI_CLIENT_ID).build()) {
            client.serverInfo().getInfo();
        }

        adminClient.realms().create(realmRep);
    }
}
