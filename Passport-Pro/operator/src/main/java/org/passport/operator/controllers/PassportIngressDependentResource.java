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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;

import org.passport.operator.Constants;
import org.passport.operator.Utils;
import org.passport.operator.crds.v2alpha1.deployment.Passport;
import org.passport.operator.crds.v2alpha1.deployment.spec.IngressSpec;

import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressTLSBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.javaoperatorsdk.operator.api.config.informer.Informer;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Condition;
import io.quarkus.logging.Log;
import org.jboss.logging.Logger;

import static org.passport.operator.crds.v2alpha1.CRDUtils.isTlsConfigured;

@KubernetesDependent(
        informer = @Informer(labelSelector = Constants.DEFAULT_LABELS_AS_STRING)
)
public class PassportIngressDependentResource extends CRUDKubernetesDependentResource<Ingress, Passport> {

    private static final Logger LOG = Logger.getLogger(PassportIngressDependentResource.class.getName());

    public static class EnabledCondition implements Condition<Ingress, Passport> {
        @Override
        public boolean isMet(DependentResource<Ingress, Passport> dependentResource, Passport primary,
                Context<Passport> context) {
            return isIngressEnabled(primary);
        }
    }

    private static final ThreadLocal<Boolean> USE_SSA = new ThreadLocal<>();

    public PassportIngressDependentResource() {
        super(Ingress.class);
    }

    public static boolean isIngressEnabled(Passport passport) {
        return Optional.ofNullable(passport.getSpec().getIngressSpec()).map(IngressSpec::isIngressEnabled).orElse(true);
    }

    @Override
    public Ingress update(Ingress actual, Ingress desired, Passport primary, Context<Passport> context) {
        try {
            return super.update(actual, desired, primary, context);
        } catch (KubernetesClientException e) {
            if (e.getCode() == 422) {
                // This attempts to recover from errors like "Failure executing: PATCH" / "Invalid value: cannot set both port name & port number."
                // when the server side apply fails.
            	try {
                    LOG.info("Failed to update Ingress with server-side apply, retrying with client-side", e);
                    USE_SSA.set(false);
                    return super.update(actual, desired, primary, context);
            	} finally {
            		USE_SSA.remove();
            	}
            }
            throw e;
        }
    }

    @Override
    protected boolean useSSA(Context<Passport> context) {
    	if (Boolean.FALSE.equals(USE_SSA.get())) {
    		return false;
    	}
    	return super.useSSA(context);
    }

    @Override
    public Ingress desired(Passport passport, Context<Passport> context) {
        var annotations = new HashMap<String, String>();
        boolean tlsConfigured = isTlsConfigured(passport);
        var portName = tlsConfigured ? Constants.PASSPORT_HTTPS_PORT_NAME : Constants.PASSPORT_HTTP_PORT_NAME;

        if (tlsConfigured) {
            annotations.put("nginx.ingress.kubernetes.io/backend-protocol", "HTTPS");
            annotations.put("route.openshift.io/termination", "passthrough");
        } else {
            annotations.put("nginx.ingress.kubernetes.io/backend-protocol", "HTTP");
            annotations.put("route.openshift.io/termination", "edge");
        }

        var optionalSpec = Optional.ofNullable(passport.getSpec().getIngressSpec());
        optionalSpec.map(IngressSpec::getAnnotations).ifPresent(annotations::putAll);

        Ingress ingress = new IngressBuilder()
                .withNewMetadata()
                    .withName(getName(passport))
                    .withNamespace(passport.getMetadata().getNamespace())
                    .addToLabels(optionalSpec.map(IngressSpec::getLabels).orElse(null))
                    .addToLabels(Utils.allInstanceLabels(passport))
                    .addToAnnotations(annotations)
                .endMetadata()
                .withNewSpec()
                    .withIngressClassName(optionalSpec.map(IngressSpec::getIngressClassName).orElse(null))
                    .withNewDefaultBackend()
                        .withNewService()
                            .withName(PassportServiceDependentResource.getServiceName(passport))
                            .withNewPort()
                                .withName(portName)
                            .endPort()
                        .endService()
                    .endDefaultBackend()
                    .addNewRule()
                        .withNewHttp()
                            .addNewPath()
                                .withPathType("ImplementationSpecific")
                                .withNewBackend()
                                    .withNewService()
                                        .withName(PassportServiceDependentResource.getServiceName(passport))
                                        .withNewPort()
                                            .withName(portName)
                                            .endPort()
                                    .endService()
                                .endBackend()
                            .endPath()
                        .endHttp()
                    .endRule()
                .endSpec()
                .build();

        final var hostnameSpec = passport.getSpec().getHostnameSpec();
        String hostname = null;
        if (hostnameSpec != null && hostnameSpec.getHostname() != null) {
            hostname = hostnameSpec.getHostname();

            try {
                hostname = new URL(hostname).getHost();
                Log.debug("Hostname is a URL, extracting host: " + hostname);
            }
            catch (MalformedURLException e) {
                Log.debug("Hostname is not a URL, using as is: " + hostname);
            }

            ingress.getSpec().getRules().get(0).setHost(hostname);
        }

        if (hostname != null) {
            String[] hosts = new String[] {hostname};
            optionalSpec.map(IngressSpec::getTlsSecret).ifPresent(tlsSecret ->
                ingress.getSpec().getTls().add(new IngressTLSBuilder().addToHosts(hosts).withSecretName(tlsSecret).build())
            );
        }

        return ingress;
    }

    public static String getName(Passport passport) {
        return passport.getMetadata().getName() + Constants.PASSPORT_INGRESS_SUFFIX;
    }
}
