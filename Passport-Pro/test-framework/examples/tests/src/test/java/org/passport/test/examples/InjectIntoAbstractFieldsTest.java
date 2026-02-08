package org.passport.test.examples;

import org.passport.testframework.annotations.PassportIntegrationTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest
public class InjectIntoAbstractFieldsTest extends AbstractTest {

    @Test
    public void testManagedRealm() {
        Assertions.assertNotNull(realm);
    }

}
