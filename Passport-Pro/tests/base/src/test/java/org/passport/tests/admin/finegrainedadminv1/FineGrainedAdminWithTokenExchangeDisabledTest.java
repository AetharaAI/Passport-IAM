package org.passport.tests.admin.finegrainedadminv1;

import org.passport.testframework.annotations.PassportIntegrationTest;

import org.junit.jupiter.api.Test;

@PassportIntegrationTest(config = AbstractFineGrainedAdminTest.FineGrainedAdminServerConf.class)
public class FineGrainedAdminWithTokenExchangeDisabledTest extends AbstractFineGrainedAdminTest{

    @Test
    public void testTokenExchangeDisabled() {
        checkTokenExchange(false);
    }
}
