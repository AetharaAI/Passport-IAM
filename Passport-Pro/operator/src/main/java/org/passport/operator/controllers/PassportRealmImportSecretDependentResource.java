package org.passport.operator.controllers;

import org.passport.operator.Constants;
import org.passport.operator.Utils;
import org.passport.operator.crds.v2alpha1.realmimport.PassportRealmImport;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.utils.KubernetesResourceUtil;
import io.javaoperatorsdk.operator.api.config.informer.Informer;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

@KubernetesDependent(
        informer = @Informer(labelSelector = Constants.DEFAULT_LABELS_AS_STRING)
)
public class PassportRealmImportSecretDependentResource extends CRUDKubernetesDependentResource<Secret, PassportRealmImport> {

    public static final String DEPENDENT_NAME = "realm-import-secret";

    public PassportRealmImportSecretDependentResource() {
        super(Secret.class);
    }

    @Override
    protected Secret desired(PassportRealmImport primary, Context<PassportRealmImport> context) {
        var fileName = primary.getRealmName() + "-realm.json";
        var content = context.getClient().getKubernetesSerialization().asJson(primary.getSpec().getRealm());

        return new SecretBuilder()
                .withNewMetadata()
                .withName(getSecretName(primary))
                .withNamespace(primary.getMetadata().getNamespace())
                // this is labeling the instance as the realm import, not the passport
                .addToLabels(Utils.allInstanceLabels(primary))
                .endMetadata()
                .addToData(fileName, Utils.asBase64(content))
                .build();
    }

    public static String getSecretName(PassportRealmImport realmCR) {
        return KubernetesResourceUtil.sanitizeName(realmCR.getSpec().getPassportCRName() + "-" + realmCR.getRealmName() + "-realm");
    }

}
