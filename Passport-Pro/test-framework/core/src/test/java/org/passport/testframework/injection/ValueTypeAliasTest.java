package org.passport.testframework.injection;

import java.util.Map;

import org.passport.admin.client.Passport;
import org.passport.testframework.server.PassportServer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValueTypeAliasTest {

    @Test
    public void withAlias() {
        ValueTypeAlias valueTypeAlias = new ValueTypeAlias();
        valueTypeAlias.addAll(Map.of(PassportServer.class, "server"));
        Assertions.assertEquals("server", valueTypeAlias.getAlias(PassportServer.class));
    }

    @Test
    public void withoutAlias() {
        Assertions.assertEquals("Passport", new ValueTypeAlias().getAlias(Passport.class));
    }

}
