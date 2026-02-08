package org.passport.crypto.def.test;

import org.passport.common.util.Environment;
import org.passport.jose.jwk.AKPJWKTest;

import org.junit.Assume;
import org.junit.Before;

public class DefaultCryptoAKPJWKTest extends AKPJWKTest {

    @Before
    public void before() {
        Assume.assumeFalse("Java is in FIPS mode. Skipping the test.", Environment.isJavaInFipsMode());
    }

}
