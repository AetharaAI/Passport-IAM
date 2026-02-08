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
import java.util.Map;
import java.util.Optional;

import org.passport.operator.Constants;
import org.passport.operator.Utils;
import org.passport.operator.crds.v2alpha1.deployment.Passport;
import org.passport.operator.crds.v2alpha1.deployment.spec.HttpManagementSpec;
import org.passport.operator.crds.v2alpha1.deployment.spec.HttpSpec;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import io.javaoperatorsdk.operator.api.config.informer.Informer;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

import static org.passport.operator.crds.v2alpha1.CRDUtils.isTlsConfigured;

@KubernetesDependent(
        informer = @Informer(labelSelector = Constants.DEFAULT_LABELS_AS_STRING)
)
public class PassportServiceDependentResource extends CRUDKubernetesDependentResource<Service, Passport> {

    public PassportServiceDependentResource() {
        super(Service.class);
    }

    private ServiceSpec getServiceSpec(Passport passport) {
        var builder = new ServiceSpecBuilder().withSelector(Utils.allInstanceLabels(passport));

        boolean tlsConfigured = isTlsConfigured(passport);
        Optional<HttpSpec> httpSpec = Optional.ofNullable(passport.getSpec().getHttpSpec());
        boolean httpEnabled = httpSpec.map(HttpSpec::getHttpEnabled).orElse(false);
        if (!tlsConfigured || httpEnabled) {
            builder.addNewPort()
                    .withPort(HttpSpec.httpPort(passport))
                    .withName(Constants.PASSPORT_HTTP_PORT_NAME)
                    .withProtocol(Constants.PASSPORT_SERVICE_PROTOCOL)
                    .endPort();
        }
        if (tlsConfigured) {
            builder.addNewPort()
                    .withPort(HttpSpec.httpsPort(passport))
                    .withName(Constants.PASSPORT_HTTPS_PORT_NAME)
                    .withProtocol(Constants.PASSPORT_SERVICE_PROTOCOL)
                    .endPort();
        }

        builder.addNewPort()
                .withPort(HttpManagementSpec.managementPort(passport))
                .withName(Constants.PASSPORT_MANAGEMENT_PORT_NAME)
                .withProtocol(Constants.PASSPORT_SERVICE_PROTOCOL)
                .endPort();

        return builder.build();
    }

    @Override
    protected Service desired(Passport primary, Context<Passport> context) {

        Map<String,String> labels = Utils.allInstanceLabels(primary);
        var optionalSpec = Optional.ofNullable(primary.getSpec().getHttpSpec());
        optionalSpec.map(HttpSpec::getLabels).ifPresent(labels::putAll);

        Map<String,String> annotations = optionalSpec.map(HttpSpec::getAnnotations).orElse(new HashMap<>());

        Service service = new ServiceBuilder()
                .withNewMetadata()
                .withName(getServiceName(primary))
                .withNamespace(primary.getMetadata().getNamespace())
                .addToLabels(labels)
                .addToAnnotations(annotations)
                .endMetadata()
                .withSpec(getServiceSpec(primary))
                .build();
        return service;
    }

    public static String getServiceName(HasMetadata passport) {
        return passport.getMetadata().getName() + Constants.PASSPORT_SERVICE_SUFFIX;
    }
}
