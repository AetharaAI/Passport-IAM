package org.passport.tests.admin.user;

import jakarta.ws.rs.core.Response;

import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.events.AdminEventAssertion;
import org.passport.testframework.realm.UserConfigBuilder;
import org.passport.testframework.util.ApiUtil;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@PassportIntegrationTest
public class UserDeleteTest extends AbstractUserTest {

    @Test
    public void delete() {
        String userId = ApiUtil.getCreatedId(managedRealm.admin().users().create(UserConfigBuilder.create().username("user1").email("user1@localhost.com").build()));
        AdminEventAssertion.assertSuccess(adminEvents.poll());
        deleteUser(userId);
    }

    @Test
    public void deleteNonExistent() {
        try (Response response = managedRealm.admin().users().delete("does-not-exist")) {
            assertEquals(404, response.getStatus());
        }
        Assertions.assertNull(adminEvents.poll());
    }
}
