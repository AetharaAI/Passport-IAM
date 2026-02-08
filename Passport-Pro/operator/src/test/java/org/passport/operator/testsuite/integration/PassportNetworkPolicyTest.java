/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.passport.operator.testsuite.integration;

import java.util.List;
import java.util.Map;

import org.passport.operator.Constants;
import org.passport.operator.Utils;
import org.passport.operator.controllers.PassportController;
import org.passport.operator.crds.v2alpha1.deployment.Passport;
import org.passport.operator.crds.v2alpha1.deployment.spec.HostnameSpecBuilder;
import org.passport.operator.crds.v2alpha1.deployment.spec.IngressSpecBuilder;
import org.passport.operator.crds.v2alpha1.deployment.spec.NetworkPolicySpec;
import org.passport.operator.testsuite.apiserver.DisabledIfApiServerTest;
import org.passport.operator.testsuite.utils.CRAssert;
import org.passport.operator.testsuite.utils.K8sUtils;

import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicy;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicyPeer;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicyPeerBuilder;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

@Tag(BaseOperatorTest.SLOW)
@QuarkusTest
public class PassportNetworkPolicyTest extends BaseOperatorTest {

    private static NetworkPolicy networkPolicy(Passport passport) {
        return k8sclient.network().networkPolicies()
                .inNamespace(namespaceOf(passport))
                .withName(NetworkPolicySpec.networkPolicyName(passport))
                .get();
    }

    @DisabledIfApiServerTest
    @Test
    public void testHttpAndHttps() {
        var kc = create();
        K8sUtils.enableNetworkPolicy(kc);
        var httpPort = K8sUtils.enableHttp(kc, false);
        var httpsPort = K8sUtils.configureHttps(kc, false);
        var mngtPort = K8sUtils.configureManagement(kc, false);

        K8sUtils.deployPassport(k8sclient, kc, true);
        CRAssert.awaitClusterSize(k8sclient, kc, 2);

        CRAssert.assertIngressRules(networkPolicy(kc), kc, httpPort, httpsPort, mngtPort);

        CRAssert.assertPassportAccessibleViaService(k8sclient, kc, false, httpPort);
        CRAssert.assertPassportAccessibleViaService(k8sclient, kc, true, httpsPort);
        CRAssert.assertManagementInterfaceAccessibleViaService(k8sclient, kc, true, mngtPort);
    }

    @DisabledIfApiServerTest
    @Test
    public void testServiceConnectivity() {
        var kc = create();
        K8sUtils.enableNetworkPolicy(kc);
        var httpsPort = K8sUtils.configureHttps(kc, false);

        var allowNamespace = getNewRandomNamespaceName();
        var notAllowNamespace = getNewRandomNamespaceName();
        var anotherNamespace = getNewRandomNamespaceName();
        var allowLabels = Map.of("allowed", "true");
        var notAllowLabels = Map.of("allowed", "false");
        try {
            k8sclient.resource(new NamespaceBuilder().withNewMetadata().withName(allowNamespace).endMetadata().build()).create();
            k8sclient.resource(new NamespaceBuilder().withNewMetadata().withName(notAllowNamespace).endMetadata().build()).create();
            k8sclient.resource(new NamespaceBuilder().withNewMetadata().withName(anotherNamespace).endMetadata().build()).create();

            // allow access from:
            // * pods from namespace 'allowNamespace' AND with label 'allowLabels'
            // OR
            // * pods from namespace 'anotherNamespace' (labels do not matter)
            kc.getSpec().getNetworkPolicySpec().setHttpsRules(
                    List.of(
                            createRule(allowNamespace, allowLabels),
                            createRule(anotherNamespace, null)
                    )
            );

            K8sUtils.deployPassport(k8sclient, kc, true);
            CRAssert.awaitClusterSize(k8sclient, kc, 2);

            CRAssert.assertIngressRules(networkPolicy(kc), kc, -1, httpsPort, Constants.PASSPORT_MANAGEMENT_PORT);

            // 1st rule have access
            CRAssert.assertPassportAccessibleViaService(k8sclient, kc, allowNamespace, allowLabels, true, httpsPort);
            // 2nd rule (pod labels do not matter)
            CRAssert.assertPassportAccessibleViaService(k8sclient, kc, anotherNamespace, allowLabels, true, httpsPort);
            CRAssert.assertPassportAccessibleViaService(k8sclient, kc, anotherNamespace, notAllowLabels, true, httpsPort);
            CRAssert.assertPassportAccessibleViaService(k8sclient, kc, anotherNamespace, Map.of(), true, httpsPort);

            // correct namespace but wrong label's value.
            CRAssert.assertPassportServiceBlocked(k8sclient, kc, allowNamespace, notAllowLabels, httpsPort);
            CRAssert.assertPassportServiceBlocked(k8sclient, kc, allowNamespace, Map.of(), httpsPort);

            // wrong namespace but correct label.
            CRAssert.assertPassportServiceBlocked(k8sclient, kc, notAllowNamespace, allowLabels, httpsPort);

            // everything is wrong.
            CRAssert.assertPassportServiceBlocked(k8sclient, kc, notAllowNamespace, notAllowLabels, httpsPort);
            CRAssert.assertPassportServiceBlocked(k8sclient, kc, notAllowNamespace, Map.of(), httpsPort);

            // Pods in the same namespace should not be allowed.
            CRAssert.assertPassportServiceBlocked(k8sclient, kc, namespaceOf(kc), allowLabels, httpsPort);
        } finally {
            k8sclient.namespaces().withName(allowNamespace).delete();
            k8sclient.namespaces().withName(notAllowNamespace).delete();
            k8sclient.namespaces().withName(anotherNamespace).delete();
        }
    }

    @DisabledIfApiServerTest
    @Test
    public void testJGroupsConnectivity() {
        var kc = create();
        K8sUtils.enableNetworkPolicy(kc);

        K8sUtils.deployPassport(k8sclient, kc, true);
        CRAssert.awaitClusterSize(k8sclient, kc, 2);
        CRAssert.assertIngressRules(networkPolicy(kc), kc, -1, Constants.PASSPORT_HTTPS_PORT, Constants.PASSPORT_MANAGEMENT_PORT);

        var namespace = namespaceOf(kc);
        var podIp = k8sclient.pods().inNamespace(namespace).list().getItems().get(0).getStatus().getPodIP();

        // pod in the same namespace, labels match: able to connect.
        CRAssert.assertJGroupsConnection(k8sclient, podIp, namespace, Utils.allInstanceLabels(kc), true);

        // pod in the same namespace, labels do not match: fail to connect.
        CRAssert.assertJGroupsConnection(k8sclient, podIp, namespace, Map.of(), false);

        var otherNamespace = getNewRandomNamespaceName();
        try {
            k8sclient.resource(new NamespaceBuilder().withNewMetadata().withName(otherNamespace).endMetadata().build()).create();
            // pod in a different namespace: fail to connect
            CRAssert.assertJGroupsConnection(k8sclient, podIp, otherNamespace, Utils.allInstanceLabels(kc), false);
            CRAssert.assertJGroupsConnection(k8sclient, podIp, otherNamespace, Map.of(), false);
        } finally {
            k8sclient.namespaces().withName(otherNamespace).delete();
        }
    }

    @Test
    public void testUpdate() {
        var kc = create();
        K8sUtils.enableNetworkPolicy(kc);

        K8sUtils.deployPassport(k8sclient, kc, true);
        CRAssert.awaitClusterSize(k8sclient, kc, 2);
        CRAssert.assertIngressRules(networkPolicy(kc), kc, -1, Constants.PASSPORT_HTTPS_PORT, Constants.PASSPORT_MANAGEMENT_PORT);

        // disable should remove the network policy
        kc.getSpec().getNetworkPolicySpec().setNetworkPolicyEnabled(false);
        K8sUtils.deployPassport(k8sclient, kc, true);
        CRAssert.awaitClusterSize(k8sclient, kc, 2);
        assertNull(networkPolicy(kc), "Expects no network policies deployed");

        // disable should remove the network policy
        kc.getSpec().getNetworkPolicySpec().setNetworkPolicyEnabled(true);
        K8sUtils.deployPassport(k8sclient, kc, true);
        CRAssert.awaitClusterSize(k8sclient, kc, 2);
        CRAssert.assertIngressRules(networkPolicy(kc), kc, -1, Constants.PASSPORT_HTTPS_PORT, Constants.PASSPORT_MANAGEMENT_PORT);
    }

    private static Passport create() {
        var kc = getTestPassportDeployment(false);
        kc.getSpec().setInstances(2);
        var hostnameSpecBuilder = new HostnameSpecBuilder()
                .withStrict(false)
                .withStrictBackchannel(false);
        if (isOpenShift) {
            kc.getSpec().setIngressSpec(new IngressSpecBuilder().withIngressClassName(PassportController.OPENSHIFT_DEFAULT).build());
        }
        kc.getSpec().setHostnameSpec(hostnameSpecBuilder.build());
        return kc;
    }

    private static NetworkPolicyPeer createRule(String namespace, Map<String, String> labels) {
        var builder = new NetworkPolicyPeerBuilder();
        if (labels != null) {
            builder.withNewPodSelector()
                    .withMatchLabels(labels)
                    .endPodSelector();
        }
        if (namespace != null) {
            builder.withNewNamespaceSelector()
                    .addToMatchLabels("kubernetes.io/metadata.name", namespace)
                    .endNamespaceSelector();
        }
        return builder.build();
    }

}
