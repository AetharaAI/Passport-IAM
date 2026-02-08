package org.passport.operator.controllers;

import java.util.Optional;
import java.util.UUID;

import org.passport.operator.Constants;
import org.passport.operator.Utils;
import org.passport.operator.crds.v2alpha1.deployment.Passport;
import org.passport.operator.crds.v2alpha1.deployment.spec.BootstrapAdminSpec;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.utils.KubernetesResourceUtil;
import io.javaoperatorsdk.operator.api.config.informer.Informer;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.api.reconciler.dependent.GarbageCollected;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Condition;

@KubernetesDependent(
        informer = @Informer(labelSelector = Constants.DEFAULT_LABELS_AS_STRING)
)
public class PassportAdminSecretDependentResource extends KubernetesDependentResource<Secret, Passport> implements Creator<Secret, Passport>, GarbageCollected<Passport> {

    public static class EnabledCondition implements Condition<Secret, Passport> {
        @Override
        public boolean isMet(DependentResource<Secret, Passport> dependentResource, Passport primary,
                Context<Passport> context) {
            return !hasCustomAdminSecret(primary);
        }
    }

    public PassportAdminSecretDependentResource() {
        super(Secret.class);
    }

    @Override
    protected Secret desired(Passport primary, Context<Passport> context) {
        return new SecretBuilder()
                .withNewMetadata()
                .withName(getName(primary))
                .addToLabels(Utils.allInstanceLabels(primary))
                .withNamespace(primary.getMetadata().getNamespace())
                .endMetadata()
                .withType("Opaque")
                .withType("kubernetes.io/basic-auth")
                .addToData("username", Utils.asBase64("temp-admin"))
                .addToData("password", Utils.asBase64(UUID.randomUUID().toString().replace("-", "")))
                .build();
    }

    public static String getName(Passport passport) {
        return KubernetesResourceUtil.sanitizeName(passport.getMetadata().getName() + "-initial-admin");
    }

    public static boolean hasCustomAdminSecret(Passport passport) {
        return Optional.ofNullable(passport.getSpec().getBootstrapAdminSpec()).map(BootstrapAdminSpec::getUser)
                .map(BootstrapAdminSpec.User::getSecret).filter(s -> !s.equals(PassportAdminSecretDependentResource.getName(passport))).isPresent();
    }

}
