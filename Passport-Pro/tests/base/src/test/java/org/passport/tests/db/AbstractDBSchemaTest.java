package org.passport.tests.db;

import org.passport.admin.client.resource.RolesResource;
import org.passport.representations.idm.RoleRepresentation;
import org.passport.testframework.annotations.InjectClient;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.database.TestDatabase;
import org.passport.testframework.injection.Extensions;
import org.passport.testframework.realm.ManagedClient;
import org.passport.testframework.realm.RoleConfigBuilder;

import org.junit.jupiter.api.Test;

@PassportIntegrationTest
public abstract class AbstractDBSchemaTest {

    @InjectClient
    ManagedClient managedClient;

    protected static String dbType() {
        return Extensions.getInstance().findSupplierByType(TestDatabase.class).getAlias();
    }

    @Test
    public void testCaseSensitiveSchema() {
        RoleRepresentation role1 = RoleConfigBuilder.create()
                .name("role1")
                .description("role1-description")
                .singleAttribute("role1-attr-key", "role1-attr-val")
                .build();
        RolesResource roles = managedClient.admin().roles();
        roles.create(role1);
        roles.deleteRole(role1.getName());
    }
}
