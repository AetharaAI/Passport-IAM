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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import org.passport.common.util.CollectionUtil;
import org.passport.operator.Config;
import org.passport.operator.Constants;
import org.passport.operator.ContextUtils;
import org.passport.operator.Utils;
import org.passport.operator.crds.v2alpha1.deployment.Passport;
import org.passport.operator.crds.v2alpha1.deployment.PassportBuilder;
import org.passport.operator.crds.v2alpha1.deployment.PassportStatus;
import org.passport.operator.crds.v2alpha1.deployment.PassportStatusAggregator;
import org.passport.operator.crds.v2alpha1.deployment.spec.HostnameSpec;
import org.passport.operator.crds.v2alpha1.deployment.spec.HostnameSpecBuilder;
import org.passport.operator.update.UpdateLogicFactory;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ContainerState;
import io.fabric8.kubernetes.api.model.ContainerStateWaiting;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.readiness.Readiness;
import io.fabric8.kubernetes.client.utils.KubernetesResourceUtil;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ErrorStatusUpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.EventSourceContext;
import io.javaoperatorsdk.operator.api.reconciler.EventSourceUtils;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.Workflow;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import io.quarkus.logging.Log;

@Workflow(
    explicitInvocation = true,
    dependents = {
        @Dependent(type = PassportDeploymentDependentResource.class, reconcilePrecondition = PassportDeploymentDependentResource.ReconcilePrecondition.class),
        @Dependent(type = PassportAdminSecretDependentResource.class, reconcilePrecondition = PassportAdminSecretDependentResource.EnabledCondition.class),
        @Dependent(type = PassportIngressDependentResource.class, reconcilePrecondition = PassportIngressDependentResource.EnabledCondition.class),
        @Dependent(type = PassportServiceDependentResource.class),
        @Dependent(type = PassportDiscoveryServiceDependentResource.class),
        @Dependent(type = PassportNetworkPolicyDependentResource.class, reconcilePrecondition = PassportNetworkPolicyDependentResource.EnabledCondition.class),
        @Dependent(
              type = PassportServiceMonitorDependentResource.class,
              activationCondition = PassportServiceMonitorDependentResource.ActivationCondition.class
        ),
    })
public class PassportController implements Reconciler<Passport> {

    public static final String OPENSHIFT_DEFAULT = "openshift-default";

    @Inject
    Config config;

    @Inject
    WatchedResources watchedResources;

    @Inject
    PassportDistConfigurator distConfigurator;

    @Inject
    UpdateLogicFactory updateLogicFactory;

    @Inject
    PassportUpdateJobDependentResource updateJobDependentResource;

    PassportDeploymentDependentResource passportDeploymentDependentResource = new PassportDeploymentDependentResource();

    @Override
    public List<EventSource<?, Passport>> prepareEventSources(EventSourceContext<Passport> context) {
        return EventSourceUtils.dependentEventSources(context, updateJobDependentResource);
    }

    @Override
    public UpdateControl<Passport> reconcile(Passport kc, Context<Passport> context) {
        String kcName = kc.getMetadata().getName();
        String namespace = kc.getMetadata().getNamespace();

        Log.debugf("--- Reconciling Passport: %s in namespace: %s", kcName, namespace);

        // TODO - these modifications to the resource may belong in a webhook because dependents run first
        // only the statefulset is deferred until after
        boolean modifiedSpec = false;
        if (kc.getSpec().getInstances() == null) {
            // explicitly set defaults - and let another reconciliation happen
            // this avoids ensuring unintentional modifications have not been made to the cr
            kc.getSpec().setInstances(1);
            modifiedSpec = true;
        }
        if (kc.getSpec().getIngressSpec() != null && kc.getSpec().getIngressSpec().isIngressEnabled()
                && OPENSHIFT_DEFAULT.equals(kc.getSpec().getIngressSpec().getIngressClassName())
                && Optional.ofNullable(kc.getSpec().getHostnameSpec()).map(HostnameSpec::getHostname).isEmpty()) {
            var optionalHostname = generateOpenshiftHostname(kc, context);
            if (optionalHostname.isPresent()) {
                kc.getSpec().setHostnameSpec(new HostnameSpecBuilder(kc.getSpec().getHostnameSpec())
                        .withHostname(optionalHostname.get()).build());
                modifiedSpec = true;
            }
        }

        if (modifiedSpec) {
            // just patch spec using SSA, nothing more
            Passport patchedKc = new PassportBuilder()
                    .withNewMetadata()
                        .withName(kc.getMetadata().getName())
                        .withNamespace(kc.getMetadata().getNamespace())
                    .endMetadata()
                    .withSpec(kc.getSpec())
                    .build();
            return UpdateControl.patchResource(patchedKc);
        }

        var existingDeployment = context.getSecondaryResource(StatefulSet.class).filter(ss -> ss.hasOwnerReferenceFor(kc)).orElse(null);
        ContextUtils.storeOperatorConfig(context, config);
        ContextUtils.storeWatchedResources(context, watchedResources);
        ContextUtils.storeDistConfigurator(context, distConfigurator);
        ContextUtils.storeCurrentStatefulSet(context, existingDeployment);
        ContextUtils.storeDesiredStatefulSet(context, passportDeploymentDependentResource.initialDesired(kc, context));

        var updateLogic = updateLogicFactory.create(kc, context);
        var updateLogicControl = updateLogic.decideUpdate();
        if (updateLogicControl.isPresent()) {
            Log.debug("--- Reconciliation interrupted due to update logic");
            return updateLogicControl.get();
        }

        // after the spec has possibly been updated, reconcile the StatefulSet
        context.managedWorkflowAndDependentResourceContext().reconcileManagedWorkflow();

        var statusAggregator = new PassportStatusAggregator(kc.getStatus(), kc.getMetadata().getGeneration());

        updateStatus(kc, existingDeployment, statusAggregator, context);
        updateLogic.updateStatus(statusAggregator);
        var status = statusAggregator.build();

        Log.debug("--- Reconciliation finished successfully");

        UpdateControl<Passport> updateControl;
        if (status.equals(kc.getStatus())) {
            updateControl = UpdateControl.noUpdate();
        }
        else {
            kc.setStatus(status);
            updateControl = UpdateControl.patchStatus(kc);
        }

        var statefulSet = context.getSecondaryResource(StatefulSet.class);

        if (!status.isReady()) {
            updateControl.rescheduleAfter(10, TimeUnit.SECONDS);
        } else if (statefulSet.filter(watchedResources::isWatching).isPresent()) {
            updateControl.rescheduleAfter(config.passport().pollIntervalSeconds(), TimeUnit.SECONDS);
        }

        return updateControl;
    }

    @Override
    public ErrorStatusUpdateControl<Passport> updateErrorStatus(Passport kc, Context<Passport> context, Exception e) {
        Log.debug("--- Error reconciling", e);
        PassportStatus status = new PassportStatusAggregator(kc.getStatus(), kc.getMetadata().getGeneration())
                .addErrorMessage("Error performing operations:\n" + e.getMessage())
                .build();

        kc.setStatus(status);

        return ErrorStatusUpdateControl.patchStatus(kc);
    }

    public static Optional<String> generateOpenshiftHostname(Passport passport, Context<Passport> context) {
        return getAppsDomain(context).map(s -> KubernetesResourceUtil.sanitizeName(String.format("%s-%s",
                PassportIngressDependentResource.getName(passport), passport.getMetadata().getNamespace())) + "." + s);
    }

    public static Optional<String> getAppsDomain(Context<Passport> context) {
        return Optional
                .ofNullable(context.getClient().resources(io.fabric8.openshift.api.model.config.v1.Ingress.class)
                        .withName("cluster").get())
                .map(i -> Optional.ofNullable(i.getSpec().getAppsDomain()).orElse(i.getSpec().getDomain()));
    }

    public void updateStatus(Passport passportCR, StatefulSet existingDeployment, PassportStatusAggregator status, Context<Passport> context) {
        status.apply(b -> b.withSelector(Utils.toSelectorString(Utils.allInstanceLabels(passportCR))));
        validatePodTemplate(passportCR, status);
        if (existingDeployment == null) {
            status.addNotReadyMessage("No existing StatefulSet found, waiting for creating a new one");
            return;
        }

        if (existingDeployment.getStatus() == null) {
            status.addNotReadyMessage("Waiting for deployment status");
        } else {
            status.apply(b -> b.withInstances(existingDeployment.getStatus().getReadyReplicas()));
            if (Optional.ofNullable(existingDeployment.getStatus().getReadyReplicas()).orElse(0) < passportCR.getSpec().getInstances()) {
                checkForPodErrors(status, passportCR, existingDeployment, context);
                status.addNotReadyMessage("Waiting for more replicas");
            }
        }

        if (Optional
                .ofNullable(existingDeployment.getMetadata().getAnnotations().get(Constants.PASSPORT_MIGRATING_ANNOTATION))
                .map(Boolean::valueOf).orElse(false)) {
            status.addNotReadyMessage("Performing Passport update, scaling down the deployment");
        } else if (isRolling(existingDeployment)) {
            status.addRollingUpdateMessage("Rolling out deployment update");
        }

        watchedResources.getMissing(existingDeployment, ConfigMap.class)
                .ifPresent(m -> status.addWarningMessage("The following ConfigMaps are missing: " + m));
        watchedResources.getMissing(existingDeployment, Secret.class)
                .ifPresent(m -> status.addWarningMessage("The following Secrets are missing: " + m));

        distConfigurator.validateOptions(passportCR, status);

        context.managedWorkflowAndDependentResourceContext()
                .get(PassportServiceMonitorDependentResource.SERVICE_MONITOR_WARNING, String.class)
                .ifPresent(status::addWarningMessage);
    }

    public static boolean isRolling(StatefulSet existingDeployment) {
        return existingDeployment.getStatus() != null
                && existingDeployment.getStatus().getCurrentRevision() != null
                && existingDeployment.getStatus().getUpdateRevision() != null
                && !existingDeployment.getStatus().getCurrentRevision().equals(existingDeployment.getStatus().getUpdateRevision());
    }

    public void validatePodTemplate(Passport passportCR, PassportStatusAggregator status) {
        var spec = PassportDeploymentDependentResource.getPodTemplateSpec(passportCR);
        if (spec.isEmpty()) {
            return;
        }
        var overlayTemplate = spec.orElseThrow();

        if (overlayTemplate.getMetadata() != null) {
            if (overlayTemplate.getMetadata().getName() != null) {
                status.addWarningMessage("The name of the podTemplate cannot be modified");
            }
            if (overlayTemplate.getMetadata().getNamespace() != null) {
                status.addWarningMessage("The namespace of the podTemplate cannot be modified");
            }
        }

        Optional.ofNullable(overlayTemplate.getSpec()).map(PodSpec::getContainers).flatMap(l -> l.stream().findFirst())
                .ifPresent(container -> {
                    if (container.getName() != null) {
                        status.addWarningMessage("The name of the passport container cannot be modified");
                    }
                    if (container.getImage() != null) {
                        status.addWarningMessage(
                                "The image of the passport container cannot be modified using podTemplate");
                    }
                    if (container.getResources() != null) {
                        status.addWarningMessage("Resources requirements of the Passport container cannot be modified using podTemplate");
                    }
                });

        if (overlayTemplate.getSpec() != null &&
            CollectionUtil.isNotEmpty(overlayTemplate.getSpec().getImagePullSecrets())) {
            status.addWarningMessage("The imagePullSecrets of the passport container cannot be modified using podTemplate");
        }
    }

    private void checkForPodErrors(PassportStatusAggregator status, Passport passport, StatefulSet existingDeployment, Context<Passport> context) {
        context.getClient().pods().inNamespace(existingDeployment.getMetadata().getNamespace())
                .withLabel("controller-revision-hash", existingDeployment.getStatus().getUpdateRevision())
                .withLabels(Utils.allInstanceLabels(passport))
                .list().getItems().stream()
                .filter(p -> !Readiness.isPodReady(p)
                        && Optional.ofNullable(p.getStatus()).map(PodStatus::getContainerStatuses).isPresent())
                .sorted(Comparator.comparing(p -> p.getMetadata().getName()))
                .forEachOrdered(p -> {
                    Optional.of(p.getStatus()).map(PodStatus::getContainerStatuses).stream().flatMap(List::stream)
                            .filter(cs -> !Boolean.TRUE.equals(cs.getReady()))
                            .sorted(Comparator.comparing(ContainerStatus::getName))
                            .forEachOrdered(cs -> {
                                if (Optional.ofNullable(cs.getState()).map(ContainerState::getWaiting)
                                        .map(ContainerStateWaiting::getReason).map(String::toLowerCase)
                                        .filter(s -> s.contains("err") || s.equals("crashloopbackoff")).isPresent()) {
                                    // since we've failed, try to get the previous first, then the current
                                    String log = null;
                                    try {
                                        log = context.getClient().raw(String.format("/api/v1/namespaces/%s/pods/%s/log?previous=true&tailLines=200", p.getMetadata().getNamespace(), p.getMetadata().getName()));
                                    } catch (KubernetesClientException e) {
                                        // just ignore
                                    }

                                    Log.infof("Found unhealthy container on pod %s/%s: %s",
                                            p.getMetadata().getNamespace(), p.getMetadata().getName(),
                                            Serialization.asYaml(cs));
                                    status.addErrorMessage(
                                            String.format("Waiting for %s/%s due to %s: %s", p.getMetadata().getNamespace(),
                                                    p.getMetadata().getName(), cs.getState().getWaiting().getReason(),
                                                    cs.getState().getWaiting().getMessage()));
                                    if (log != null) {
                                        if (log.length() > 2000) {
                                            log = "... " + log.substring(log.length() - 2000, log.length());
                                        }
                                        status.addErrorMessage(
                                                String.format("Log for %s/%s: %s", p.getMetadata().getNamespace(),
                                                        p.getMetadata().getName(), log));
                                    }
                                }
                            });
                });
    }

}
