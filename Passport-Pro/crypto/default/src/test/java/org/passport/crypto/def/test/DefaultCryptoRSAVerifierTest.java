package org.passport.crypto.def.test;

import org.passport.RSAVerifierTest;
import org.passport.common.util.Environment;

import org.junit.Assume;
import org.junit.Before;

/**
 * Test with bouncycastle security provider
 * 
 */
public class DefaultCryptoRSAVerifierTest extends RSAVerifierTest {

    @Before
    public void before() {
        // Run this test just if java is not in FIPS mode
        Assume.assumeFalse("Java is in FIPS mode. Skipping the test.", Environment.isJavaInFipsMode());
    }
}
