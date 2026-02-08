package org.passport.crypto.fips.test.sdjwt;

import org.passport.common.util.Environment;
import org.passport.sdjwt.consumer.SdJwtPresentationConsumerTest;

import org.junit.Assume;
import org.junit.Before;

public class FIPS1402SdJwtPresentationConsumerTest extends SdJwtPresentationConsumerTest {

    @Before
    public void before() {
        // Run this test just if java is not in FIPS mode
        Assume.assumeFalse("Java is in FIPS mode. Skipping the test.", Environment.isJavaInFipsMode());
    }
}
