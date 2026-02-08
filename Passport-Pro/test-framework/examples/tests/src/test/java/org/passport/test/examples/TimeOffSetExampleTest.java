package org.passport.test.examples;

import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.remote.timeoffset.InjectTimeOffSet;
import org.passport.testframework.remote.timeoffset.TimeOffSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest
public class TimeOffSetExampleTest {

    @InjectTimeOffSet(offset = 3)
    TimeOffSet timeOffSet;

    @Test
    public void testSetOffset() {
        int offset = timeOffSet.get();
        Assertions.assertEquals(3, offset);
        Assertions.assertDoesNotThrow(() -> timeOffSet.set(10));
        offset = timeOffSet.get();
        Assertions.assertEquals(10, offset);
    }

    @Test
    public void testGetOffset() {
        int offset = timeOffSet.get();
        Assertions.assertEquals(3, offset);
    }
}
