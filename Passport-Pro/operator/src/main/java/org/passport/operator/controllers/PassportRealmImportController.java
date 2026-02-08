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

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import org.passport.operator.Config;
import org.passport.operator.ContextUtils;
import org.passport.operator.crds.v2alpha1.deployment.Passport;
import org.passport.operator.crds.v2alpha1.realmimport.PassportRealmImport;
import org.passport.operator.crds.v2alpha1.realmimport.PassportRealmImportStatus;
import org.passport.operator.crds.v2alpha1.realmimport.PassportRealmImportStatusBuilder;

import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetStatus;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ErrorStatusUpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.Workflow;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import io.quarkus.logging.Log;

@Workflow(
explicitInvocation = true,
dependents = {
    @Dependent(type = PassportRealmImportJobDependentResource.class, dependsOn = PassportRealmImportSecretDependentResource.DEPENDENT_NAME),
    @Dependent(type = PassportRealmImportSecretDependentResource.class, name = PassportRealmImportSecretDependentResource.DEPENDENT_NAME)
})
public class PassportRealmImportController implements Reconciler<PassportRealmImport> {

    @Inject
    Config config;

    @Override
    public UpdateControl<PassportRealmImport> reconcile(PassportRealmImport realm, Context<PassportRealmImport> context) {
        String realmName = realm.getMetadata().getName();
        String realmNamespace = realm.getMetadata().getNamespace();

        Log.infof("--- Reconciling Passport Realm: %s in namespace: %s", realmName, realmNamespace);

        var statusBuilder = new PassportRealmImportStatusBuilder();

        Job existingJob = context.getSecondaryResource(Job.class).filter(job -> job.hasOwnerReferenceFor(realm)).orElse(null);
        StatefulSet existingDeployment = context.getClient().resources(StatefulSet.class).inNamespace(realm.getMetadata().getNamespace())
                .withName(realm.getSpec().getPassportCRName()).get();

        Passport existingPassport = context.getClient().resources(Passport.class).inNamespace(realm.getMetadata().getNamespace())
                .withName(realm.getSpec().getPassportCRName()).require();

        if (existingDeployment != null) {
            ContextUtils.storeOperatorConfig(context, config);
            ContextUtils.storeCurrentStatefulSet(context, existingDeployment);
            ContextUtils.storePassport(context, existingPassport);
            if (getReadyReplicas(existingDeployment) > 0) {
                context.managedWorkflowAndDependentResourceContext().reconcileManagedWorkflow();
            }
        }

        updateStatus(statusBuilder, realm, existingJob, existingDeployment, context.getClient());

        var status = statusBuilder.build();

        Log.info("--- Realm reconciliation finished successfully");

        UpdateControl<PassportRealmImport> updateControl;
        if (status.equals(realm.getStatus())) {
            updateControl = UpdateControl.noUpdate();
        } else {
            realm.setStatus(status);
            updateControl = UpdateControl.patchStatus(realm);
        }

        if (!status.isDone()) {
            updateControl.rescheduleAfter(10, TimeUnit.SECONDS);
        }

        return updateControl;
    }

    @Override
    public ErrorStatusUpdateControl<PassportRealmImport> updateErrorStatus(PassportRealmImport realm, Context<PassportRealmImport> context, Exception e) {
        Log.debug("--- Error reconciling", e);
        PassportRealmImportStatus status = new PassportRealmImportStatusBuilder()
                .addErrorMessage("Error performing operations:\n" + e.getMessage())
                .build();

        realm.setStatus(status);
        return ErrorStatusUpdateControl.patchStatus(realm);
    }

    public void updateStatus(PassportRealmImportStatusBuilder status, PassportRealmImport realmCR, Job existingJob, StatefulSet existingDeployment, KubernetesClient client) {
        if (existingDeployment == null) {
            status.addErrorMessage("No existing Deployment found, waiting for it to be created");
            return;
        }

        if (existingJob == null) {
            Log.info("Job about to start");
            status.addStartedMessage("Import Job will start soon");
            if (getReadyReplicas(existingDeployment) < 1) {
                status.addErrorMessage("Deployment not yet ready");
            }
            return;
        }

        Log.info("Job already executed - not recreating");
        var oldStatus = existingJob.getStatus();
        var lastReportedStatus = realmCR.getStatus();

        if (oldStatus == null) {
            Log.info("Job started");
            status.addStartedMessage("Import Job started");
        } else if (oldStatus.getSucceeded() != null && oldStatus.getSucceeded() > 0) {
            if (!lastReportedStatus.isDone()) {
                // no need to restart Passport as we're only importing new realms and are not overwriting existing realms
                Log.info("Job finished");
            }
            status.addDone();
        } else if (oldStatus.getFailed() != null && oldStatus.getFailed() > 0) {
            Log.info("Job Failed");
            status.addErrorMessage("Import Job failed");
        } else {
            Log.info("Job running");
            status.addStartedMessage("Import Job running");
        }
    }

    private Integer getReadyReplicas(StatefulSet existingDeployment) {
        return Optional.ofNullable(existingDeployment.getStatus()).map(StatefulSetStatus::getReadyReplicas).orElse(0);
    }

}
