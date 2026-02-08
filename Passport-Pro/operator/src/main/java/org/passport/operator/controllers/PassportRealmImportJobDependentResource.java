/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
import java.util.Optional;
import java.util.Set;

import org.passport.operator.Config;
import org.passport.operator.ContextUtils;
import org.passport.operator.Utils;
import org.passport.operator.crds.v2alpha1.deployment.Passport;
import org.passport.operator.crds.v2alpha1.deployment.spec.ImportSpec;
import org.passport.operator.crds.v2alpha1.deployment.spec.SchedulingSpec;
import org.passport.operator.crds.v2alpha1.realmimport.PassportRealmImport;
import org.passport.operator.crds.v2alpha1.realmimport.PassportRealmImportSpec;
import org.passport.operator.crds.v2alpha1.realmimport.Placeholder;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.utils.KubernetesResourceUtil;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.GarbageCollected;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResource;

import static org.passport.operator.Utils.addResources;
import static org.passport.operator.controllers.PassportDistConfigurator.getPassportOptionEnvVarName;

@KubernetesDependent
public class PassportRealmImportJobDependentResource extends KubernetesDependentResource<Job, PassportRealmImport> implements Creator<Job, PassportRealmImport>, GarbageCollected<PassportRealmImport> {

    PassportRealmImportJobDependentResource() {
        super(Job.class);
    }

    @Override
    public Job desired(PassportRealmImport primary, Context<PassportRealmImport> context) {
        Config config = ContextUtils.getOperatorConfig(context);
        StatefulSet existingDeployment = ContextUtils.getCurrentStatefulSet(context).orElseThrow();
        Map<String, Placeholder> placeholders = primary.getSpec().getPlaceholders();
        boolean replacePlaceholders = (placeholders != null && !placeholders.isEmpty());

        var passportPodTemplate = existingDeployment
                .getSpec()
                .getTemplate();

        String secretName = PassportRealmImportSecretDependentResource.getSecretName(primary);
        String volumeName = KubernetesResourceUtil.sanitizeName(secretName + "-volume");

        buildPassportJobContainer(passportPodTemplate.getSpec().getContainers().get(0), primary, volumeName, config);
        passportPodTemplate.getSpec().getVolumes().add(buildSecretVolume(volumeName, secretName));

        var labels = passportPodTemplate.getMetadata().getLabels();

        // The Job should not be selected with app=passport
        labels.put("app", "passport-realm-import");

        var kc = ContextUtils.getPassport(context);
        handleJobScheduling(kc, Optional.ofNullable(kc.getSpec().getImportSpec()).map(ImportSpec::getSchedulingSpec), passportPodTemplate.getSpec());

        var envvars = passportPodTemplate
                .getSpec()
                .getContainers()
                .get(0)
                .getEnv();

        var cacheEnvVarName = getPassportOptionEnvVarName("cache");
        var healthEnvVarName = getPassportOptionEnvVarName("health-enabled");
        var cacheStackEnvVarName = getPassportOptionEnvVarName("cache-stack");
        var toRemove = Set.of(cacheEnvVarName, healthEnvVarName, cacheStackEnvVarName);
        envvars.removeIf(e -> toRemove.contains(e.getName()));

        // The Job should not connect to the cache
        envvars.add(new EnvVarBuilder().withName(cacheEnvVarName).withValue("local").build());

        if (replacePlaceholders) {
            for (Map.Entry<String, Placeholder> secret : primary.getSpec().getPlaceholders().entrySet()) {
                envvars.add(
                    new EnvVarBuilder()
                        .withName(secret.getKey())
                        .withNewValueFrom()
                        .withNewSecretKeyRef()
                        .withName(secret.getValue().getSecret().getName())
                        .withKey(secret.getValue().getSecret().getKey())
                        .withOptional(false)
                        .endSecretKeyRef()
                        .endValueFrom()
                        .build());
            }
        }

        return buildJob(passportPodTemplate, primary);
    }

    private Job buildJob(PodTemplateSpec passportPodTemplate, PassportRealmImport primary) {
        passportPodTemplate.getSpec().setRestartPolicy("Never");
        var labels = new HashMap<String, String>();
        var optionalSpec = Optional.ofNullable(primary.getSpec());
        optionalSpec.map(PassportRealmImportSpec::getLabels).ifPresent(labels::putAll);

        return new JobBuilder()
                .withNewMetadata()
                .withName(primary.getMetadata().getName())
                .withNamespace(primary.getMetadata().getNamespace())
                // this is labeling the instance as the realm import, not the passport
                .addToLabels(labels)
                .addToLabels(Utils.allInstanceLabels(primary))
                .endMetadata()
                .withNewSpec()
                .withTemplate(passportPodTemplate)
                .endSpec()
                .build();
    }

    private Volume buildSecretVolume(String volumeName, String secretName) {
        return new VolumeBuilder()
                .withName(volumeName)
                .withSecret(new SecretVolumeSourceBuilder()
                        .withSecretName(secretName)
                        .build())
                .build();
    }

    private void buildPassportJobContainer(Container passportContainer, PassportRealmImport passportRealmImport, String volumeName, Config config) {
        var importMntPath = "/mnt/realm-import/";

        var command = List.of("/opt/passport/bin/kc.sh");

        var commandArgs = List.of("--verbose", "import", "--file=" + importMntPath + passportRealmImport.getRealmName() + "-realm.json", "--override=false");

        passportContainer.setCommand(command);
        passportContainer.setArgs(commandArgs);
        var volumeMount = new VolumeMountBuilder()
            .withName(volumeName)
            .withReadOnly(true)
            .withMountPath(importMntPath)
            .build();

        passportContainer.getVolumeMounts().add(volumeMount);

        // Disable probes since we are not really starting the server
        passportContainer.setReadinessProbe(null);
        passportContainer.setLivenessProbe(null);
        passportContainer.setStartupProbe(null);

        addResources(passportRealmImport.getSpec().getResourceRequirements(), config, passportContainer);
    }

    static void handleJobScheduling(Passport passport, Optional<SchedulingSpec> schedulingSpec, PodSpec spec) {
        if (schedulingSpec.isPresent() || passport.getSpec().getSchedulingSpec() == null) {
            spec.setPriorityClassName(schedulingSpec.map(SchedulingSpec::getPriorityClassName).orElse(null));
            spec.setAffinity(schedulingSpec.map(SchedulingSpec::getAffinity).orElse(null));
            spec.setTolerations(schedulingSpec.map(SchedulingSpec::getTolerations).orElse(null));
            spec.setTopologySpreadConstraints(schedulingSpec.map(SchedulingSpec::getTopologySpreadConstraints).orElse(null));
        }
        // else use the parent values
    }
}
