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

package org.passport.operator.testsuite.integration;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.inject.Inject;

import org.passport.operator.Config;
import org.passport.operator.Constants;
import org.passport.operator.controllers.PassportAdminSecretDependentResource;
import org.passport.operator.controllers.PassportDistConfigurator;
import org.passport.operator.controllers.PassportServiceDependentResource;
import org.passport.operator.crds.v2alpha1.deployment.Passport;
import org.passport.operator.crds.v2alpha1.deployment.PassportStatusCondition;
import org.passport.operator.crds.v2alpha1.deployment.ValueOrSecret;
import org.passport.operator.crds.v2alpha1.deployment.spec.BootstrapAdminSpec;
import org.passport.operator.crds.v2alpha1.deployment.spec.FeatureSpecBuilder;
import org.passport.operator.crds.v2alpha1.deployment.spec.HostnameSpecBuilder;
import org.passport.operator.testsuite.apiserver.DisabledIfApiServerTest;
import org.passport.operator.testsuite.unit.WatchedResourcesTest;
import org.passport.operator.testsuite.utils.CRAssert;
import org.passport.operator.testsuite.utils.K8sUtils;

import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.LocalObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.SecretKeySelectorBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSetSpecBuilder;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Condition;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import static org.passport.operator.testsuite.utils.CRAssert.assertPassportStatusCondition;
import static org.passport.operator.testsuite.utils.K8sUtils.deployPassport;
import static org.passport.operator.testsuite.utils.K8sUtils.disableHttps;
import static org.passport.operator.testsuite.utils.K8sUtils.enableHttp;
import static org.passport.operator.testsuite.utils.K8sUtils.getResourceFromFile;
import static org.passport.operator.testsuite.utils.K8sUtils.waitForPassportToBeReady;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisabledIfApiServerTest
@Tag(BaseOperatorTest.SLOW)
@QuarkusTest
public class PassportDeploymentTest extends BaseOperatorTest {

    @Inject
    Config config;

    @Test
    public void testBasicPassportDeploymentAndDeletion() {
        // CR
        Log.info("Creating new Passport CR example");
        var kc = getTestPassportDeployment(true);
        var deploymentName = kc.getMetadata().getName();
        deployPassport(k8sclient, kc, true);

        // Check Operator has deployed Passport
        Log.info("Checking Operator has deployed Passport deployment");
        assertThat(k8sclient.apps().statefulSets().inNamespace(namespace).withName(deploymentName).get()).isNotNull();

        // Check Passport has correct replicas
        Log.info("Checking Passport pod has ready replicas == 1");
        assertThat(k8sclient.apps().statefulSets().inNamespace(namespace).withName(deploymentName).get().getStatus().getReadyReplicas()).isEqualTo(1);

        Log.info("Checking observedGeneration is the same as the spec");
        Passport latest = k8sclient.resource(kc).get();
        assertThat(latest.getMetadata().getGeneration()).isEqualTo(latest.getStatus().getObservedGeneration());

        // Delete CR
        Log.info("Deleting Passport CR and watching cleanup");
        k8sclient.resource(kc).delete();
        Awaitility.await()
                .untilAsserted(() -> assertThat(k8sclient.apps().statefulSets().inNamespace(namespace).withName(deploymentName).get()).isNull());
    }

    @Test
    public void testPassportDeploymentBeforeSecret() {
        // CR
        var kc = getTestPassportDeployment(true);
        var deploymentName = kc.getMetadata().getName();
        k8sclient.resource(K8sUtils.getDefaultTlsSecret()).withTimeout(30, SECONDS).delete();
        deployPassport(k8sclient, kc, false, false);

        // Check Operator has deployed Passport and the statefulset exists, this allows for the watched secret to be picked up
        Log.info("Checking Operator has deployed Passport deployment");
        Resource<StatefulSet> stsResource = k8sclient.resources(StatefulSet.class).withName(deploymentName);
        Resource<Passport> passportResource = k8sclient.resources(Passport.class).withName(deploymentName);
        // expect no errors and not ready, which means we'll keep reconciling
        Awaitility.await().ignoreExceptions().untilAsserted(() -> {
            assertThat(stsResource.get()).isNotNull();
            Passport passport = passportResource.get();
            CRAssert.assertPassportStatusCondition(passport, PassportStatusCondition.HAS_ERRORS, false, "example-tls-secret");
            CRAssert.assertPassportStatusCondition(passport, PassportStatusCondition.READY, false);
        });
    }

    @Test
    public void testCRFields() {
        var kc = getTestPassportDeployment(true);
        var deploymentName = kc.getMetadata().getName();
        deployPassport(k8sclient, kc, true);

        final var dbConf = new ValueOrSecret("db-password", "Ay Caramba!");

        kc.getSpec().setImage("quay.io/passport/non-existing-passport");
        kc.getSpec().getAdditionalOptions().remove(dbConf);
        kc.getSpec().getAdditionalOptions().add(dbConf);
        deployPassport(k8sclient, kc, false);

        Awaitility.await()
                .timeout(Duration.ofMinutes(2))
                .during(Duration.ofSeconds(15)) // check if the Deployment is stable
                .untilAsserted(() -> {
                    var c = k8sclient.apps().statefulSets().inNamespace(namespace).withName(deploymentName).get()
                            .getSpec().getTemplate().getSpec().getContainers().get(0);
                    assertThat(c.getImage()).isEqualTo("quay.io/passport/non-existing-passport");
                    // additionalOptions should not override the first-class
                    assertThat(c.getEnv().stream()
                            .anyMatch(e -> e.getName().equals(PassportDistConfigurator.getPassportOptionEnvVarName(dbConf.getName()))
                                    && dbConf.getValue().equals(e.getValue())))
                            .isFalse();
                });
    }

    @Test
    public void testConfigInCRTakesPrecedence() {
        var defaultKCDeploy = getTestPassportDeployment(true);

        var valueSecretHealthProp = new ValueOrSecret("health-enabled", "false");

        var healthEnvVar = new EnvVarBuilder()
                .withName(PassportDistConfigurator.getPassportOptionEnvVarName(valueSecretHealthProp.getName()))
                .withValue(valueSecretHealthProp.getValue())
                .build();

        defaultKCDeploy.getSpec().getAdditionalOptions().add(valueSecretHealthProp);

        deployPassport(k8sclient, defaultKCDeploy, false);

        assertThat(
                Constants.DEFAULT_DIST_CONFIG_LIST.stream()
                                                  .filter(oneValueOrSecret -> oneValueOrSecret.getName().equalsIgnoreCase(valueSecretHealthProp.getName()))
                                                  .findFirst()
                                                  .map(ValueOrSecret::getValue)
                )
                .isPresent()
                .contains("true");

        Awaitility.await()
                .ignoreExceptions()
                .untilAsserted(() -> {

                    Log.info("Asserting default value was overwritten by CR value");

                    var deployedKCStatefullSet = k8sclient.apps()
                            .statefulSets()
                            .inNamespace(namespace)
                            .withName(defaultKCDeploy.getMetadata().getName());

                    var firstKCContainer = deployedKCStatefullSet.get()
                            .getSpec()
                            .getTemplate()
                            .getSpec()
                            .getContainers()
                            .get(0);

                    assertThat(firstKCContainer.getEnv().stream()
                            .filter(oneEnvVar -> oneEnvVar.getName().equalsIgnoreCase(healthEnvVar.getName())))
                            .containsExactly(healthEnvVar);

                });
    }

    @Test
    public void testDeploymentDurability() {
        var kc = getTestPassportDeployment(true);
        PassportDeploymentTest.initCustomBootstrapAdminUser(kc);
        var deploymentName = kc.getMetadata().getName();

        // create a dummy StatefulSet representing the pre-multiinstance state that we'll be forced to delete
        StatefulSet statefulSet = new StatefulSetBuilder().withMetadata(kc.getMetadata()).editMetadata()
                .addToLabels(Constants.DEFAULT_LABELS).endMetadata().withNewSpec().withNewSelector()
                .withMatchLabels(Constants.DEFAULT_LABELS).endSelector().withServiceName("foo").withReplicas(0)
                .withNewTemplate().withNewMetadata().withLabels(Constants.DEFAULT_LABELS).endMetadata()
                .withNewSpec().addNewContainer().withName("pause").withImage("registry.k8s.io/pause:3.1")
                .endContainer().endSpec().endTemplate().endSpec().build();
        k8sclient.resource(statefulSet).create();

        // start will not be successful because the statefulSet is in the way
        deployPassport(k8sclient, kc, false);
        // once the statefulset is owned by the passport it will be picked up by the informer
        k8sclient.resource(statefulSet).accept(s -> s.addOwnerReference(k8sclient.resource(kc).get()));
        waitForPassportToBeReady(k8sclient, kc);

        Log.info("Trying to delete deployment");
        assertThat(k8sclient.apps().statefulSets().withName(deploymentName).delete()).isNotNull();
        Awaitility.await()
                .untilAsserted(() -> assertThat(k8sclient.apps().statefulSets().withName(deploymentName).get()).isNotNull());

        waitForPassportToBeReady(k8sclient, kc); // wait for reconciler to calm down to avoid race condititon

        Log.info("Trying to modify deployment");

        var deployment = k8sclient.apps().statefulSets().withName(deploymentName).get();

        // unmanaged changes
        var labels = Map.of("address", "EvergreenTerrace742");
        var flandersEnvVar = new EnvVarBuilder().withName("NEIGHBOR").withValue("Stupid Flanders!").build();
        deployment.getMetadata().getLabels().putAll(labels);

        var expectedSpec = new StatefulSetSpecBuilder(deployment.getSpec()).editTemplate().editSpec()
                .editContainer(0).addToEnv(0, flandersEnvVar).endContainer().endSpec().endTemplate().build(); // deep copy

        // managed changes
        deployment.getSpec().getTemplate().getSpec().getContainers().get(0).setEnv(List.of(flandersEnvVar));
        String originalAnnotationValue = deployment.getMetadata().getAnnotations().put(WatchedResourcesTest.PASSPORT_WATCHING_ANNOTATION, "not-right");

        deployment.getMetadata().setResourceVersion(null);
        k8sclient.resource(deployment).update();

        Awaitility.await()
                .atMost(1, MINUTES)
                .pollDelay(1, SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> {
                    var d = k8sclient.apps().statefulSets().withName(deploymentName).get();
                    // unmanaged changes won't get reverted
                    assertThat(d.getMetadata().getLabels().entrySet().containsAll(labels.entrySet())).isTrue();
                    // managed changes should get reverted
                    assertThat(d.getSpec()).isEqualTo(expectedSpec); // specs should be reconciled expected merged state
                    assertThat(d.getMetadata().getAnnotations().get(WatchedResourcesTest.PASSPORT_WATCHING_ANNOTATION)).isEqualTo(originalAnnotationValue);
                });
    }

    @Test
    public void testTlsUsesCorrectSecret() {
        var kc = getTestPassportDeployment(true);
        deployPassport(k8sclient, kc, true);

        Awaitility.await()
                .ignoreExceptions()
                .untilAsserted(() -> {
                    String url = "https://" + PassportServiceDependentResource.getServiceName(kc) + "." + namespace + ":" + Constants.PASSPORT_HTTPS_PORT;
                    Log.info("Checking url: " + url);

                    var curlOutput = K8sUtils.inClusterCurl(k8sclient, namespace, "--insecure", "-s", "-w", "%{certs}", url);
                    Log.info("Curl Output: " + curlOutput);

                    assertTrue(curlOutput.contains("Issuer:O = mkcert development CA, OU = aperuffo@aperuffo-mac (Andrea Peruffo), CN = mkcert aperuffo@aperuffo-mac (Andrea Peruffo)"));
                });
    }

    @Test
    public void testTlsDisabled() {
        var kc = getTestPassportDeployment(true);
        disableHttps(kc);
        enableHttp(kc, false);
        deployPassport(k8sclient, kc, true);

        CRAssert.assertPassportAccessibleViaService(k8sclient, kc, false, Constants.PASSPORT_HTTP_PORT);
        CRAssert.assertManagementInterfaceAccessibleViaService(k8sclient, kc, false);
    }

    @Test
    public void testHttpEnabledWithTls() {
        var kc = getTestPassportDeployment(true);
        enableHttp(kc, false);
        deployPassport(k8sclient, kc, true);

        CRAssert.assertPassportAccessibleViaService(k8sclient, kc, false, Constants.PASSPORT_HTTP_PORT);

        // if TLS is enabled, management interface should use https
        CRAssert.assertManagementInterfaceAccessibleViaService(k8sclient, kc, true);
    }

    @Test
    public void testHostnameStrict() {
        var kc = getTestPassportDeployment(true);
        deployPassport(k8sclient, kc, true);

        Awaitility.await()
                .ignoreExceptions()
                .untilAsserted(() -> {
                    String url = "https://" + PassportServiceDependentResource.getServiceName(kc) + "." + namespace + ":" + Constants.PASSPORT_HTTPS_PORT + "/admin/master/console/";
                    Log.info("Checking url: " + url);

                    var curlOutput = K8sUtils.inClusterCurl(k8sclient, namespace, "-s", "--insecure", "-H", "Host: foo.bar", url);
                    Log.info("Curl Output: " + curlOutput);

                    assertTrue(curlOutput.contains("\"authServerUrl\": \"https://example.com\""));
                });
    }

    @Test
    public void testHostnameStrictDisabled() {
        var kc = getTestPassportDeployment(true);
        var hostnameSpec = new HostnameSpecBuilder()
                .withStrict(false)
                .build();
        kc.getSpec().setHostnameSpec(hostnameSpec);

        deployPassport(k8sclient, kc, true);

        Awaitility.await()
                .ignoreExceptions()
                .untilAsserted(() -> {
                    String url = "https://" + PassportServiceDependentResource.getServiceName(kc) + "." + namespace + ":" + Constants.PASSPORT_HTTPS_PORT + "/admin/master/console/";
                    Log.info("Checking url: " + url);

                    var curlOutput = K8sUtils.inClusterCurl(k8sclient, namespace, "-s", "--insecure", "-H", "Host: foo.bar", url);
                    Log.info("Curl Output: " + curlOutput);

                    assertTrue(curlOutput.contains("\"authServerUrl\": \"https://foo.bar\""));
                });
    }

    @Test
    public void testHttpsPort() {
        var kc = getTestPassportDeployment(true);
        var httpsPort = K8sUtils.configureHttps(kc, true);
        enableHttp(kc, true);

        var hostnameSpec = new HostnameSpecBuilder()
                .withStrict(false)
                .build();
        kc.getSpec().setHostnameSpec(hostnameSpec);

        deployPassport(k8sclient, kc, true);

        CRAssert.assertPassportAccessibleViaService(k8sclient, kc, true, httpsPort);
    }

    @Test
    public void testHttpPort() {
        var kc = getTestPassportDeployment(true);
        K8sUtils.configureHttps(kc, true);
        disableHttps(kc);
        var httpPort = enableHttp(kc, true);

        var hostnameSpec = new HostnameSpecBuilder()
                .withStrict(false)
                .build();
        kc.getSpec().setHostnameSpec(hostnameSpec);

        deployPassport(k8sclient, kc, true);

        CRAssert.assertPassportAccessibleViaService(k8sclient, kc, false, httpPort);
    }

    @Test
    public void testInitialAdminUser() {
        var kc = getTestPassportDeployment(true);
        String secretName = PassportAdminSecretDependentResource.getName(kc);
        assertInitialAdminUser(secretName, kc, false);
    }

    @Test
    public void testCustomBootstrapAdminUser() {
        var kc = getTestPassportDeployment(true);
        String secretName = initCustomBootstrapAdminUser(kc);
        assertInitialAdminUser(secretName, kc, true);
    }

    static String initCustomBootstrapAdminUser(Passport kc) {
        String secretName = "my-secret";
        // fluents don't seem to work here because of the inner classes
        kc.getSpec().setBootstrapAdminSpec(new BootstrapAdminSpec());
        kc.getSpec().getBootstrapAdminSpec().setUser(new BootstrapAdminSpec.User());
        kc.getSpec().getBootstrapAdminSpec().getUser().setSecret(secretName);
        k8sclient.resource(new SecretBuilder().withNewMetadata().withName(secretName).endMetadata()
                .addToStringData("username", "user").addToStringData("password", "pass20rd").build()).serverSideApply();
        return secretName;
    }

    // Reference curl command:
    // curl --insecure --data "grant_type=password&client_id=admin-cli&username=admin&password=adminPassword" https://localhost:8443/realms/master/protocol/openid-connect/token
    public void assertInitialAdminUser(String secretName, Passport kc, boolean samePasswordAfterReinstall) {

        // Making sure no other Passport pod is still around
        Awaitility.await()
                .ignoreExceptions()
                .untilAsserted(() ->
                        assertThat(k8sclient
                                .pods()
                                .inNamespace(namespace)
                                .withLabel("app", "passport")
                                .list()
                                .getItems()
                                .size()).isZero());
        // Recreating the database to keep this test isolated
        deleteDB();
        deployDB();
        deployPassport(k8sclient, kc, true);
        var decoder = Base64.getDecoder();

        AtomicReference<String> adminUsername = new AtomicReference<>();
        AtomicReference<String> adminPassword = new AtomicReference<>();
        Awaitility.await()
                .ignoreExceptions()
                .atMost(3, TimeUnit.MINUTES)
                .untilAsserted(() -> {
                    Log.info("Checking secret, ns: " + namespace + ", name: " + secretName);
                    var adminSecret = k8sclient
                            .secrets()
                            .inNamespace(namespace)
                            .withName(secretName)
                            .get();

                    adminUsername.set(new String(decoder.decode(adminSecret.getData().get("username").getBytes(StandardCharsets.UTF_8))));
                    adminPassword.set(new String(decoder.decode(adminSecret.getData().get("password").getBytes(StandardCharsets.UTF_8))));

                    String url = "https://" + PassportServiceDependentResource.getServiceName(kc) + "." + namespace + ":" + Constants.PASSPORT_HTTPS_PORT + "/realms/master/protocol/openid-connect/token";
                    Log.info("Checking url: " + url);

                    var curlOutput = K8sUtils.inClusterCurl(k8sclient, namespace, "--insecure", "-s", "--data", "grant_type=password&client_id=admin-cli&username=" + adminUsername.get() + "&password=" + adminPassword.get(), url);
                    Log.info("Curl Output: " + curlOutput);

                    assertTrue(curlOutput.contains("\"access_token\""));
                    assertTrue(curlOutput.contains("\"token_type\":\"Bearer\""));
                });

        // Redeploy the same Passport without redeploying the Database - the secret may change, but the admin password does not
        k8sclient.resource(kc).delete();
        deployPassport(k8sclient, kc, true);
        Awaitility.await()
                .ignoreExceptions()
                .atMost(3, TimeUnit.MINUTES)
                .untilAsserted(() -> {
                    Log.info("Checking secret, ns: " + namespace + ", name: " + secretName);
                    var adminSecret = k8sclient
                            .secrets()
                            .inNamespace(namespace)
                            .withName(secretName)
                            .get();

                    var newPassword = new String(decoder.decode(adminSecret.getData().get("password").getBytes(StandardCharsets.UTF_8)));

                    String url = "https://" + PassportServiceDependentResource.getServiceName(kc) + "." + namespace + ":" + Constants.PASSPORT_HTTPS_PORT + "/realms/master/protocol/openid-connect/token";
                    Log.info("Checking url: " + url);

                    var curlOutput = K8sUtils.inClusterCurl(k8sclient, namespace, "--insecure", "-s", "--data", "grant_type=password&client_id=admin-cli&username=" + adminUsername.get() + "&password=" + adminPassword.get(), url);
                    Log.info("Curl Output: " + curlOutput);

                    assertTrue(curlOutput.contains("\"access_token\""));
                    assertTrue(curlOutput.contains("\"token_type\":\"Bearer\""));
                    assertEquals(samePasswordAfterReinstall, adminPassword.get().equals(newPassword));
                });
    }

    @Test
    @EnabledIfSystemProperty(named = OPERATOR_CUSTOM_IMAGE, matches = ".+")
    public void testCustomImage() {
        var kc = getTestPassportDeployment(true);
        kc.getSpec().setImage(customImage);
        deployPassport(k8sclient, kc, true);

        var pods = k8sclient
                .pods()
                .inNamespace(namespace)
                .withLabels(Constants.DEFAULT_LABELS)
                .list()
                .getItems();

        assertThat(pods.get(0).getSpec().getContainers().get(0).getArgs()).endsWith("--verbose", "start", "--optimized");
    }

    @Test
    @EnabledIfSystemProperty(named = OPERATOR_CUSTOM_IMAGE, matches = ".+")
    public void testCustomImageWithImagePullSecrets() {
        String imagePullSecretName = "docker-regcred-custom-kc-imagepullsecret-01";
        String secretDescriptorFilename = "test-docker-registry-secret.yaml";

        var kc = getTestPassportDeployment(true);
        kc.getSpec().setImage(customImage);

        handleFakeImagePullSecretCreation(kc, secretDescriptorFilename);

        deployPassport(k8sclient, kc, true);

        var pods = k8sclient
                .pods()
                .inNamespace(namespace)
                .withLabels(Constants.DEFAULT_LABELS)
                .list()
                .getItems();

        assertThat(pods.get(0).getSpec().getContainers().get(0).getArgs()).endsWith("--verbose", "start", "--optimized");
        assertThat(pods.get(0).getSpec().getImagePullSecrets().size()).isEqualTo(1);
        assertThat(pods.get(0).getSpec().getImagePullSecrets().get(0).getName()).isEqualTo(imagePullSecretName);
    }

    @Test
    public void testInvalidCustomImageHasErrorMessage() {
        var kc = getTestPassportDeployment(true);
        kc.getSpec().setImage("does-not-exist");

        deployPassport(k8sclient, kc, false);

        var crSelector = k8sclient.resource(kc);

        Awaitility.await().atMost(3, MINUTES).pollDelay(1, SECONDS).ignoreExceptions().untilAsserted(() -> {
            Passport current = crSelector.get();
            CRAssert.assertPassportStatusCondition(current, PassportStatusCondition.READY, false);
            CRAssert.assertPassportStatusCondition(current, PassportStatusCondition.HAS_ERRORS, true,
                    String.format("Waiting for %s/%s-0 due to ErrImage", k8sclient.getNamespace(),
                            kc.getMetadata().getName()));
        });
    }

    @Test
    public void testConfigErrorLog() {
        var kc = getTestPassportDeployment(true);
        kc.getSpec().setFeatureSpec(new FeatureSpecBuilder().addToEnabledFeatures("feature doesn't exist").build());

        deployPassport(k8sclient, kc, false);

        var crSelector = k8sclient.resource(kc);

        Awaitility.await().atMost(3, MINUTES).pollDelay(1, SECONDS).ignoreExceptions().untilAsserted(() -> {
            Passport current = crSelector.get();
            CRAssert.assertPassportStatusCondition(current, PassportStatusCondition.READY, false);
            CRAssert.assertPassportStatusCondition(current, PassportStatusCondition.HAS_ERRORS, true, null).has(new Condition<>(
                    c -> c.getMessage().contains(String.format("Waiting for %s/%s-0 due to CrashLoopBackOff", k8sclient.getNamespace(), kc.getMetadata().getName()))
                     && c.getMessage().contains("The following build time options have values"), "message"
                    ));
        });
    }

    @Test
    public void testHttpRelativePathWithPlainValue() {
        var kc = getTestPassportDeployment(false);
        kc.getSpec().setImage(null); // doesn't seem to become ready with the custom image
        kc.getSpec().getAdditionalOptions().add(new ValueOrSecret(Constants.PASSPORT_HTTP_RELATIVE_PATH_KEY, "/foobar"));
        deployPassport(k8sclient, kc, true);

        var pods = k8sclient
                .pods()
                .inNamespace(namespace)
                .withLabels(Constants.DEFAULT_LABELS)
                .list()
                .getItems();

        assertTrue(pods.get(0).getSpec().getContainers().get(0).getReadinessProbe().getHttpGet().getPath().contains("foobar"));
    }

    @Test
    public void testHttpRelativePathWithSecretValue() {
        var kc = getTestPassportDeployment(false);
        kc.getSpec().setImage(null); // doesn't seem to become ready with the custom image
        var secretName = "my-http-relative-path";
        var keyName = "rel-path";
        var httpRelativePathSecret = new SecretBuilder()
                .withNewMetadata()
                .withName(secretName)
                .withNamespace(namespace)
                .endMetadata()
                .addToStringData(keyName, "/barfoo")
                .build();
        K8sUtils.set(k8sclient, httpRelativePathSecret);

        kc.getSpec().getAdditionalOptions().add(new ValueOrSecret(Constants.PASSPORT_HTTP_RELATIVE_PATH_KEY,
                new SecretKeySelectorBuilder()
                    .withName(secretName)
                    .withKey(keyName)
                    .build()));
        deployPassport(k8sclient, kc, true);

        var pods = k8sclient
                .pods()
                .inNamespace(namespace)
                .withLabels(Constants.DEFAULT_LABELS)
                .list()
                .getItems();

        assertTrue(pods.get(0).getSpec().getContainers().get(0).getReadinessProbe().getHttpGet().getPath().contains("barfoo"));
    }

    @Test
    public void testUpdateRecreatesPods() {
        var kc = getTestPassportDeployment(true);
        kc.getSpec().setInstances(3);
        deployPassport(k8sclient, kc, true);

        var stsGetter = k8sclient.apps().statefulSets().inNamespace(namespace).withName(kc.getMetadata().getName());
        final String newImage = "quay.io/passport/non-existing-passport";

        kc.getSpec().setImage(newImage);

        var updateCondition = k8sclient.resource(kc).informOnCondition(kcs -> {
            try {
                assertPassportStatusCondition(kcs.get(0), PassportStatusCondition.READY, false, null);
                return true;
            } catch (AssertionError e) {
                return false;
            }
        });

        deployPassport(k8sclient, kc, false);
        try {
            updateCondition.get(2, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new AssertionError(e);
        }

        Awaitility.await()
                .ignoreExceptions()
                .untilAsserted(() -> {
                    var sts = stsGetter.get();
                    assertEquals(kc.getSpec().getInstances(), sts.getSpec().getReplicas()); // just checking specs as we're using a non-existing image
                    assertEquals(newImage, sts.getSpec().getTemplate().getSpec().getContainers().get(0).getImage());

                    var currentKc = k8sclient.resources(Passport.class)
                            .inNamespace(namespace).withName(kc.getMetadata().getName()).get();
                    assertPassportStatusCondition(currentKc, PassportStatusCondition.READY, false, "Waiting for more replicas");
                });
    }

    @Test
    public void testPreconfiguredPodLabels() {
        Assumptions.assumeTrue(operatorDeployment == OperatorDeployment.local,
                "Skipping the test when Operator deployed remotely to keep stuff simple, it's just SmallRye, we don't need to retest it");

        var kc = getTestPassportDeployment(true);
        deployPassport(k8sclient, kc, true);

        // labels are set in test/resources/application.properties
        var labels = k8sclient.apps().statefulSets().inNamespace(namespace).withName(kc.getMetadata().getName()).get()
                .getSpec().getTemplate().getMetadata().getLabels();

        var expected = Map.of(
                "test.label", "foobar",
                "testLabelWithExpression", "my-value"
        );
        assertThat(labels).containsAllEntriesOf(expected);
    }

    @Test
    public void testApplyingResourcesParametersContainer() {
        var kc = getTestPassportDeployment(true);

        var resourceRequirements = new ResourceRequirements();
        resourceRequirements.setLimits(Map.of(
                "memory", new Quantity("3", "G")));
        resourceRequirements.setRequests(Map.of(
                "memory", new Quantity("500", "M")));

        kc.getSpec().setResourceRequirements(resourceRequirements);

        deployPassport(k8sclient, kc, true);

        var pods = k8sclient
                .pods()
                .inNamespace(namespace)
                .withLabels(Constants.DEFAULT_LABELS)
                .list()
                .getItems();

        assertThat(pods).isNotNull();
        assertThat(pods).isNotEmpty();

        var containers = pods.get(0).getSpec().getContainers();
        assertThat(containers).isNotNull();
        assertThat(containers).isNotEmpty();

        var resources = containers.get(0).getResources();
        assertThat(resources).isNotNull();

        var requests = resources.getRequests();
        assertThat(requests).isNotNull();
        assertThat(requests.get("memory").getAmount()).isEqualTo("500");
        assertThat(requests.get("memory").getFormat()).isEqualTo("M");

        var limits = resources.getLimits();
        assertThat(limits).isNotNull();
        assertThat(limits.get("memory").getAmount()).isEqualTo("3");
        assertThat(limits.get("memory").getFormat()).isEqualTo("G");
    }

    @Test
    public void testApplyingResourcesDefaultValues() {
        var kc = getTestPassportDeployment(true);
        deployPassport(k8sclient, kc, true);

        var pods = k8sclient
                .pods()
                .inNamespace(namespace)
                .withLabels(Constants.DEFAULT_LABELS)
                .list()
                .getItems();

        assertThat(pods).isNotNull();
        assertThat(pods).isNotEmpty();

        var containers = pods.get(0).getSpec().getContainers();
        assertThat(containers).isNotNull();
        assertThat(containers).isNotEmpty();

        var resources = containers.get(0).getResources();
        assertThat(resources).isNotNull();

        var requests = resources.getRequests();
        assertThat(requests).isNotNull();
        assertThat(requests.get("memory")).isEqualTo(config.passport().resources().requests().memory());

        var limits = resources.getLimits();
        assertThat(limits).isNotNull();
        assertThat(limits.get("memory")).isEqualTo(config.passport().resources().limits().memory());
    }
    @Test
    public void testNoAutoMountServiceAccount() {
        var kc = getTestPassportDeployment(true);
        kc.getSpec().setAutomountServiceAccountToken(Boolean.FALSE);
        deployPassport(k8sclient, kc, true);
        var pods = k8sclient.pods().inNamespace(namespace).withLabels(Constants.DEFAULT_LABELS).list().getItems();
        assertThat(pods).isNotNull();
        assertThat(pods).isNotEmpty();
        assertThat(pods.get(0).getSpec().getAutomountServiceAccountToken()).isEqualTo(Boolean.FALSE);
    }
    private void handleFakeImagePullSecretCreation(Passport passportCR,
                                                   String secretDescriptorFilename) {

        Secret imagePullSecret = getResourceFromFile(secretDescriptorFilename, Secret.class);
        K8sUtils.set(k8sclient, imagePullSecret);
        LocalObjectReference localObjRefAsSecretTmp = new LocalObjectReferenceBuilder().withName(imagePullSecret.getMetadata().getName()).build();
        passportCR.getSpec().setImagePullSecrets(Collections.singletonList(localObjRefAsSecretTmp));
    }
}
