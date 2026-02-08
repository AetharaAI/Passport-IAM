package org.passport.test.examples;

import java.util.List;

import org.passport.admin.client.Passport;
import org.passport.admin.client.resource.RealmResource;
import org.passport.jose.jws.JWSInput;
import org.passport.jose.jws.JWSInputException;
import org.passport.models.AdminRoles;
import org.passport.models.Constants;
import org.passport.representations.AccessToken;
import org.passport.representations.idm.ClientRepresentation;
import org.passport.representations.idm.MappingsRepresentation;
import org.passport.representations.idm.UserRepresentation;
import org.passport.testframework.annotations.InjectAdminClient;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.RealmConfig;
import org.passport.testframework.realm.RealmConfigBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest
public class RealmSpecificAdminClientTest {

    @InjectRealm(config = RealmWithClientAndUser.class)
    ManagedRealm realm;

    @InjectAdminClient(ref = "bootstrap-client")
    Passport bootstrapAdminClient;

    @InjectAdminClient(mode = InjectAdminClient.Mode.MANAGED_REALM, client = "myclient", user = "myadmin")
    Passport realmAdminClient;

    @Test
    public void testAdminClientIssuers() throws JWSInputException {
        AccessToken bootstrapAccessToken = new JWSInput(bootstrapAdminClient.tokenManager().getAccessToken().getToken()).readJsonContent(AccessToken.class);
        Assertions.assertTrue(bootstrapAccessToken.getIssuer().endsWith("/realms/master"));

        AccessToken realmAccessToken = new JWSInput(realmAdminClient.tokenManager().getAccessToken().getToken()).readJsonContent(AccessToken.class);
        Assertions.assertTrue(realmAccessToken.getIssuer().endsWith("/realms/" + realm.getName()));
    }

    @Test
    public void testRealmWithClientAndUser() {
        RealmResource realmResource = realmAdminClient.realms().realm(realm.getName());

        List<ClientRepresentation> clients = realmResource.clients().findByClientId("myclient");
        Assertions.assertEquals(1, clients.size());

        ClientRepresentation client = clients.get(0);
        Assertions.assertTrue(client.isEnabled());
        Assertions.assertTrue(client.isDirectAccessGrantsEnabled());
        Assertions.assertEquals("mysecret", client.getSecret());

        List<UserRepresentation> users = realm.admin().users().search("myadmin");
        Assertions.assertEquals(1, users.size());

        UserRepresentation user = users.get(0);
        Assertions.assertTrue(user.isEnabled());
        Assertions.assertEquals("My", user.getFirstName());
        Assertions.assertEquals("Admin", user.getLastName());
        Assertions.assertEquals("myadmin@localhost", user.getEmail());
        Assertions.assertTrue(user.isEmailVerified());

        MappingsRepresentation roles = realmResource.users().get(user.getId()).roles().getAll();
        Assertions.assertEquals(1, roles.getClientMappings().get(Constants.REALM_MANAGEMENT_CLIENT_ID).getMappings().size());
    }

    public static class RealmWithClientAndUser implements RealmConfig {

        @Override
        public RealmConfigBuilder configure(RealmConfigBuilder realm) {
            realm.addClient("myclient")
                    .secret("mysecret")
                    .directAccessGrantsEnabled(true);

            realm.addUser("myadmin")
                    .name("My", "Admin")
                    .email("myadmin@localhost")
                    .emailVerified(true)
                    .password("mypassword")
                    .clientRoles(Constants.REALM_MANAGEMENT_CLIENT_ID, AdminRoles.REALM_ADMIN);

            return realm;
        }
    }

}
