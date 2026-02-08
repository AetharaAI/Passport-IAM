package org.passport.crypto.fips.test;

import org.passport.common.util.Environment;
import org.passport.util.PemUtilsTest;

import org.junit.Assume;
import org.junit.Before;

public class PemUtilsBCFIPSTest extends PemUtilsTest {

    @Before
    public void before() {
        // Run this test just if java is in FIPS mode
        Assume.assumeTrue("Java is not in FIPS mode. Skipping the test.", Environment.isJavaInFipsMode());
    }
}
