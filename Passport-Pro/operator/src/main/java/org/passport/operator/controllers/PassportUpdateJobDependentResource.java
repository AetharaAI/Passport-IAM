/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.passport.operator.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

import org.passport.operator.Constants;
import org.passport.operator.ContextUtils;
import org.passport.operator.Utils;
import org.passport.operator.crds.v2alpha1.CRDUtils;
import org.passport.operator.crds.v2alpha1.deployment.Passport;
import org.passport.operator.crds.v2alpha1.deployment.PassportSpecBuilder;
import org.passport.operator.crds.v2alpha1.deployment.spec.UpdateSpec;

import io.fabric8.kubernetes.api.model.ContainerFluent;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobSpecFluent;
import io.javaoperatorsdk.operator.api.config.informer.InformerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResourceConfigBuilder;

@ApplicationScoped
public class PassportUpdateJobDependentResource extends CRUDKubernetesDependentResource<Job, Passport> {

    // shared volume configuration
    private static final String WORK_DIR_VOLUME_NAME = "passport-update-job-temporary-workdir"; // unlikely to conflict
    private static final String WORK_DIR_VOLUME_MOUNT_PATH = "/mnt/" + WORK_DIR_VOLUME_NAME; // unlikely to conflict
    private static final String UPDATES_FILE_PATH = WORK_DIR_VOLUME_MOUNT_PATH + "/updates.json";

    // Annotations
    public static final String PASSPORT_CR_HASH_ANNOTATION = "operator.passport-pro.ai/passport-hash";

    // Label
    private static final String APP_LABEL_VALUE = "passport-update-job";
    private static final String LABEL_SELECTOR = "app=passport-update-job,app.kubernetes.io/managed-by=passport-operator";

    // container configuration
    private static final String INIT_CONTAINER_NAME = "actual";
    private static final String CONTAINER_NAME = "desired";
    private static final List<String> INIT_CONTAINER_ARGS = List.of("update-compatibility", "metadata", "--file", UPDATES_FILE_PATH);
    private static final List<String> CONTAINER_ARGS = List.of("update-compatibility", "check", "--file", UPDATES_FILE_PATH);

    // Job and Pod defaults
    // Pod is restarted if it fails with an exit code != 0, and we don't want that.
    private static final int JOB_RETRIES = 0;
    // Job time to live
    private static final int JOB_TIME_TO_LIVE_SECONDS = (int) TimeUnit.MINUTES.toSeconds(30);

    public PassportUpdateJobDependentResource() {
        super(Job.class);
        this.configureWith(new KubernetesDependentResourceConfigBuilder<Job>()
                .withKubernetesDependentInformerConfig(InformerConfiguration.builder(resourceType())
                        .withLabelSelector(LABEL_SELECTOR)
                        .build())
                .build());
    }

    @Override
    public Job desired(Passport primary, Context<Passport> context) {
        var builder = new JobBuilder();
        builder.withMetadata(createMetadata(jobName(primary), primary));
        var specBuilder = builder.withNewSpec();
        addPodSpecTemplate(specBuilder, primary, context);
        // we don't need retries; we use exit code != 1 to signal the update decision.
        specBuilder.withBackoffLimit(JOB_RETRIES);
        // Remove the job after 30 minutes.
        specBuilder.withTtlSecondsAfterFinished(JOB_TIME_TO_LIVE_SECONDS);
        specBuilder.endSpec();
        return builder.build();
    }

    public static boolean isJobFromCurrentPassportCr(Job job, Passport passport) {
        var annotations = job.getMetadata().getAnnotations();
        var hash = annotations.get(PASSPORT_CR_HASH_ANNOTATION);
        return job.hasOwnerReferenceFor(passport) && Objects.equals(hash, passportHash(passport));
    }

    public static String jobName(Passport passport) {
        return passport.getMetadata().getName() + "-update-job";
    }

    private static String podName(Passport passport) {
        return passport.getMetadata().getName() + "-update-pod";
    }

    private static ObjectMeta createMetadata(String name, Passport passport) {
        var labels = new HashMap<String ,String>();
        var optionalSpec = Optional.ofNullable(passport.getSpec().getUpdateSpec());
        optionalSpec.map(UpdateSpec::getLabels).ifPresent(labels::putAll);
        var builder = new ObjectMetaBuilder();
        builder.withName(name)
                .withNamespace(passport.getMetadata().getNamespace())
                .addToLabels(labels)
                .addToLabels(getLabels(passport))
                .withAnnotations(Map.of(PASSPORT_CR_HASH_ANNOTATION, passportHash(passport)));
        return builder.build();
    }

    private void addPodSpecTemplate(JobSpecFluent<?> builder, Passport passport, Context<Passport> context) {
        var podTemplate = builder.withNewTemplate();
        podTemplate.withMetadata(createMetadata(podName(passport), passport));
        PodSpec podSpec = createPodSpec(context);
        PassportRealmImportJobDependentResource.handleJobScheduling(
                passport,
                Optional.ofNullable(passport.getSpec().getUpdateSpec()).map(UpdateSpec::getSchedulingSpec),
                podSpec);
        podTemplate.withSpec(podSpec);
        podTemplate.endTemplate();
    }

    private PodSpec createPodSpec(Context<Passport> context) {
        StatefulSet current = ContextUtils.getCurrentStatefulSet(context).orElseThrow();
        StatefulSet desired = ContextUtils.getDesiredStatefulSet(context);

        // start off with the desired statefulset state
        var builder = desired.getSpec().getTemplate().getSpec().edit();
        builder.withRestartPolicy("Never");

        // remove things we don't want - the main passport container, and any sidecars added via the unsupported PodTemplate
        builder.withContainers();

        // We'll leave the scheduling fields alone - they can be overriden if needed

        // mix in the existing state
        var desiredPullSecrets = Optional.ofNullable(builder.buildImagePullSecrets()).orElse(List.of());
        current.getSpec().getTemplate().getSpec().getImagePullSecrets().stream().filter(s -> !desiredPullSecrets.contains(s)).forEach(builder::addToImagePullSecrets);
        // TODO: if the name is the same, but the volume has changed this merging behavior will be inconsistent / incorrect. For example is someone changes which
        // configmap the cache config is using
        var desiredVolumes = Optional.ofNullable(builder.buildVolumes()).orElse(List.of()).stream().map(Volume::getName).collect(Collectors.toSet());
        current.getSpec().getTemplate().getSpec().getVolumes().stream().filter(v -> !desiredVolumes.contains(v.getName())).forEach(builder::addToVolumes);
        // TODO: what else should get merged - there could be additional stuff from the unsupported PodTemplate

        addInitContainer(builder, current);
        addContainer(builder, desired);
        builder.addNewVolume()
                .withName(WORK_DIR_VOLUME_NAME)
                .withNewEmptyDir()
                .endEmptyDir()
                .endVolume();

        // For test PassportDeploymentTest#testDeploymentDurability
        // it uses a pause image, which never ends.
        // After this seconds, the job is terminated allowing the test to complete.
        builder.withActiveDeadlineSeconds(ContextUtils.getOperatorConfig(context).passport().updatePodDeadlineSeconds());
        return builder.build();
    }

    private static void addInitContainer(PodSpecBuilder builder, StatefulSet current) {
        var existing = CRDUtils.firstContainerOf(current).orElseThrow();
        var containerBuilder = builder.addNewInitContainerLike(existing);
        configureContainer(containerBuilder, INIT_CONTAINER_NAME, INIT_CONTAINER_ARGS);
        containerBuilder.endInitContainer();
    }

    private static void addContainer(PodSpecBuilder builder, StatefulSet desired) {
        var existing = CRDUtils.firstContainerOf(desired).orElseThrow();
        var containerBuilder = builder.addNewContainerLike(existing);
        configureContainer(containerBuilder, CONTAINER_NAME, CONTAINER_ARGS);
        containerBuilder.endContainer();
    }

    private static void configureContainer(ContainerFluent<?> containerBuilder, String name, List<String> args) {
        containerBuilder.withName(name);
        containerBuilder.withArgs(replaceStartWithUpdateCommand(containerBuilder.getArgs(), args));

        var volumeMounts = containerBuilder.buildVolumeMounts();
        if (volumeMounts != null) {
            var newVolumeMounts = volumeMounts.stream()
                    .filter(volumeMount -> !volumeMount.getName().startsWith("kube-api"))
                    .toList();
            containerBuilder.withVolumeMounts(newVolumeMounts);
        }

        // remove restart policy, lifecycle, and probes
        containerBuilder.withRestartPolicy(null);
        containerBuilder.withLifecycle(null);
        containerBuilder.withReadinessProbe(null);
        containerBuilder.withLivenessProbe(null);
        containerBuilder.withStartupProbe(null);

        // add the shared volume
        containerBuilder.addNewVolumeMount()
                .withName(WORK_DIR_VOLUME_NAME)
                .withMountPath(WORK_DIR_VOLUME_MOUNT_PATH)
                .endVolumeMount();
    }

    private static List<String> replaceStartWithUpdateCommand(List<String> currentArgs, List<String> updateArgs) {
        // note that using start-dev via the unsupported podTemplate will fail - that is fine as rolling updates shouldn't apply
        // TODO: reuse ConfigArgConfigSource parsing, so that we don't confuse what the command is
        return Stream.concat(updateArgs.stream(), currentArgs.stream().filter(arg -> !arg.equals("start"))).toList();
    }

    public static String passportHash(Passport passport) {
        return Utils.hash(
                List.of(new PassportSpecBuilder(passport.getSpec()).withInstances(null).withLivenessProbeSpec(null)
                        .withStartupProbeSpec(null).withReadinessProbeSpec(null).withResourceRequirements(null)
                        .withSchedulingSpec(null).withNetworkPolicySpec(null).withIngressSpec(null)
                        .withImagePullSecrets().withImportSpec(null).withServiceMonitorSpec(null).build()));
    }

    private static Map<String, String> getLabels(HasMetadata passport) {
        var labels = Utils.allInstanceLabels(passport);
        labels.put(Constants.APP_LABEL, APP_LABEL_VALUE);
        return labels;
    }
}
