/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.passport.operator.testsuite.unit;

import org.passport.operator.crds.v2alpha1.deployment.PassportStatus;
import org.passport.operator.crds.v2alpha1.deployment.PassportStatusAggregator;
import org.passport.operator.crds.v2alpha1.deployment.PassportStatusCondition;
import org.passport.operator.testsuite.utils.CRAssert;

import io.fabric8.kubernetes.client.utils.Serialization;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class PassportStatusTest {

    @Test
    public void testEqualityWithScale() {
        PassportStatus status1 = new PassportStatusAggregator(0L).apply(b -> b.withInstances(1)).build();

        PassportStatus status2 = new PassportStatusAggregator(0L).apply(b -> b.withInstances(2)).build();

        assertNotEquals(status1, status2);
    }

    @Test
    public void testDefaults() {
        PassportStatus status = new PassportStatusAggregator(1L).build();
        CRAssert.assertPassportStatusCondition(status, PassportStatusCondition.READY, true, "", 1L);

        CRAssert.assertPassportStatusCondition(status, PassportStatusCondition.ROLLING_UPDATE, false, "", 1L);

        CRAssert.assertPassportStatusCondition(status, PassportStatusCondition.HAS_ERRORS, false, "", 1L);
    }

    @Test
    public void testReadyWithWarning() {
        PassportStatus status = new PassportStatusAggregator(1L).addWarningMessage("something's not right").build();
        CRAssert.assertPassportStatusCondition(status, PassportStatusCondition.READY, true, "", 1L);

        CRAssert.assertPassportStatusCondition(status, PassportStatusCondition.HAS_ERRORS, false, "warning: something's not right", 1L); // could also be unknown
    }

    @Test
    public void testNotReady() {
        PassportStatus status = new PassportStatusAggregator(1L).addNotReadyMessage("waiting").build();
        CRAssert.assertPassportStatusCondition(status, PassportStatusCondition.READY, false, "waiting", 1L);

        CRAssert.assertPassportStatusCondition(status, PassportStatusCondition.HAS_ERRORS, false, "", 1L);
    }

    @Test
    public void testReadyRolling() {
        PassportStatus status = new PassportStatusAggregator(1L).addRollingUpdateMessage("rolling").build();
        CRAssert.assertPassportStatusCondition(status, PassportStatusCondition.READY, true, "", 1L);

        CRAssert.assertPassportStatusCondition(status, PassportStatusCondition.ROLLING_UPDATE, true, "rolling", 1L);
    }

    @Test
    public void testError() {
        // without prior status, ready and rolling are unknown
        PassportStatus status = new PassportStatusAggregator(1L).addErrorMessage("this is bad").build();
        CRAssert.assertPassportStatusCondition(status, PassportStatusCondition.READY, null, null, null);

        CRAssert.assertPassportStatusCondition(status, PassportStatusCondition.HAS_ERRORS, true, "this is bad", 1L);

        CRAssert.assertPassportStatusCondition(status, PassportStatusCondition.ROLLING_UPDATE, null, null, null);
    }

    @Test
    public void testErrorWithPriorStatus() {
        // with prior status, ready and rolling are preserved
        PassportStatus prior = new PassportStatusAggregator(1L).build();
        prior.getConditions().stream().forEach(c -> c.setLastTransitionTime("prior"));

        PassportStatus status = new PassportStatusAggregator(prior, 2L).addErrorMessage("this is bad").build();
        CRAssert.assertPassportStatusCondition(status, PassportStatusCondition.READY, true, "", 1L)
            .extracting(PassportStatusCondition::getLastTransitionTime).isEqualTo("prior");

        CRAssert.assertPassportStatusCondition(status, PassportStatusCondition.HAS_ERRORS, true, "this is bad", 2L)
            .extracting(PassportStatusCondition::getLastTransitionTime).isNotEqualTo("prior");

        CRAssert.assertPassportStatusCondition(status, PassportStatusCondition.ROLLING_UPDATE, false, "", 1L);
    }

    @Test
    public void testReadyWithPriorStatus() {
        // without prior status, ready and rolling are known and keep the transition time
        PassportStatus prior = new PassportStatusAggregator(1L).build();
        prior.getConditions().stream().forEach(c -> c.setLastTransitionTime("prior"));

        PassportStatus status = new PassportStatusAggregator(prior, 2L).build();
        CRAssert.assertPassportStatusCondition(status, PassportStatusCondition.READY, true, "", 2L)
            .extracting(PassportStatusCondition::getLastTransitionTime).isEqualTo("prior");

        CRAssert.assertPassportStatusCondition(status, PassportStatusCondition.HAS_ERRORS, false, "", 2L);

        CRAssert.assertPassportStatusCondition(status, PassportStatusCondition.ROLLING_UPDATE, false, "", 2L);
    }

    @Test
    public void testMessagesChangesLastTransitionTime() {
        PassportStatus prior = new PassportStatusAggregator(1L).build();
        prior.getConditions().stream().forEach(c -> {
            c.setLastTransitionTime("prior");
            c.setMessage("old");
        });

        PassportStatus status = new PassportStatusAggregator(prior, 2L).build();
        CRAssert.assertPassportStatusCondition(status, PassportStatusCondition.READY, true, "", 2L).has(new Condition<>(
                c -> !c.getLastTransitionTime().equals("prior") && !c.getMessage().equals("old"), "transitioned"));
    }

    @Test
    public void testPreservesScale() {
        PassportStatus prior = new PassportStatusAggregator(1L).apply(b -> b.withObservedGeneration(1L).withInstances(3)).build();
        prior.getConditions().stream().forEach(c -> c.setLastTransitionTime("prior"));

        PassportStatus status = new PassportStatusAggregator(prior, 2L).apply(b -> b.withObservedGeneration(2L)).build();
        assertEquals(2, status.getObservedGeneration());
        assertEquals(3, status.getInstances());
    }

    @Test
    public void testStatusSerializtion() {
        PassportStatusCondition condition = new PassportStatusCondition();
        condition.setStatus(false);

        String yaml = Serialization.asYaml(condition);
        assertEquals("---\nstatus: \"False\"\n", yaml);

        var deserialized = Serialization.unmarshal(yaml, PassportStatusCondition.class);
        assertFalse(deserialized.getStatus());
    }

}
