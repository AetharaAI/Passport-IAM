package org.passport.crypto.def.test;

import org.passport.common.util.Environment;
import org.passport.jose.JWETest;

import org.junit.Assume;
import org.junit.Before;

/**
 * Test with default security provider and non-fips bouncycastle
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DefaultCryptoJWETest extends JWETest {

    @Before
    public void before() {
        // Run this test just if java is not in FIPS mode
        Assume.assumeFalse("Java is in FIPS mode. Skipping the test.", Environment.isJavaInFipsMode());
    }
}
