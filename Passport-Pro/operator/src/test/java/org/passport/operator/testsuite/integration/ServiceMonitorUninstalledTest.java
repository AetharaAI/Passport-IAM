package org.passport.operator.testsuite.integration;

import java.util.concurrent.TimeUnit;

import org.passport.operator.controllers.PassportServiceMonitorDependentResource;
import org.passport.operator.crds.v2alpha1.deployment.Passport;
import org.passport.operator.crds.v2alpha1.deployment.PassportStatusCondition;
import org.passport.operator.testsuite.utils.CRAssert;
import org.passport.operator.testsuite.utils.K8sUtils;

import io.fabric8.openshift.api.model.monitoring.v1.ServiceMonitor;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class ServiceMonitorUninstalledTest extends BaseOperatorTest {

    @BeforeAll
    public static void beforeAll() {
        k8sclient.apiextensions().v1().customResourceDefinitions().withName(new ServiceMonitor().getFullResourceName())
                .withTimeout(10, TimeUnit.SECONDS).delete();
    }

    @Test
    public void testServiceMonitorNoCRD() {
        Assumptions.assumeFalse(ServiceMonitorTest.isServiceMonitorAvailable(k8sclient));
        var kc = getTestPassportDeployment(true, false);
        K8sUtils.deployPassport(k8sclient, kc, true);

        ServiceMonitor sm = ServiceMonitorTest.getServiceMonitor(kc);
        assertThat(sm).isNull();

        CRAssert.assertPassportStatusCondition(
                k8sclient.resources(Passport.class).withName(kc.getMetadata().getName()).get(),
                PassportStatusCondition.HAS_ERRORS, false,
                PassportServiceMonitorDependentResource.WARN_CRD_NOT_INSTALLED);
    }

}
