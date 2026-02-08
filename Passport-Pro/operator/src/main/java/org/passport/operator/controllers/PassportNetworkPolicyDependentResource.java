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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.passport.operator.Constants;
import org.passport.operator.Utils;
import org.passport.operator.crds.v2alpha1.CRDUtils;
import org.passport.operator.crds.v2alpha1.deployment.Passport;
import org.passport.operator.crds.v2alpha1.deployment.PassportSpec;
import org.passport.operator.crds.v2alpha1.deployment.spec.HttpManagementSpec;
import org.passport.operator.crds.v2alpha1.deployment.spec.HttpSpec;
import org.passport.operator.crds.v2alpha1.deployment.spec.NetworkPolicySpec;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicy;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicyBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicyFluent;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicyPeer;
import io.javaoperatorsdk.operator.api.config.informer.Informer;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.DependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.javaoperatorsdk.operator.processing.dependent.workflow.Condition;
import org.jboss.logging.Logger;

import static org.passport.operator.Constants.PASSPORT_JGROUPS_DATA_PORT;
import static org.passport.operator.Constants.PASSPORT_JGROUPS_FD_PORT;
import static org.passport.operator.Constants.PASSPORT_JGROUPS_PROTOCOL;
import static org.passport.operator.Constants.PASSPORT_SERVICE_PROTOCOL;

@KubernetesDependent(
        informer = @Informer(labelSelector = Constants.DEFAULT_LABELS_AS_STRING)
)
public class PassportNetworkPolicyDependentResource extends CRUDKubernetesDependentResource<NetworkPolicy, Passport> {

    private static final Logger LOG = Logger.getLogger(PassportNetworkPolicyDependentResource.class.getName());
    
    public PassportNetworkPolicyDependentResource() {
        super(NetworkPolicy.class);
    }

    public static class EnabledCondition implements Condition<NetworkPolicy, Passport> {
        @Override
        public boolean isMet(DependentResource<NetworkPolicy, Passport> dependentResource, Passport primary,
                             Context<Passport> context) {
            return NetworkPolicySpec.isNetworkPolicyEnabled(primary);
        }
    }

    @Override
    public NetworkPolicy desired(Passport primary, Context<Passport> context) {
        var builder = new NetworkPolicyBuilder();
        addMetadata(builder, primary);

        var specBuilder = builder.withNewSpec()
                .withPolicyTypes("Ingress");

        addPodSelector(specBuilder, primary);
        addApplicationPorts(specBuilder, primary);

        if (CRDUtils.isJGroupEnabled(primary)) {
            addJGroupsPorts(specBuilder, primary);
        }

        // see org.passport.quarkus.runtime.configuration.mappers.ManagementPropertyMappers.isManagementEnabled()
        if (CRDUtils.isManagementEndpointEnabled(primary)) {
            addManagementPorts(specBuilder, primary);
        }

        var np = specBuilder.endSpec().build();
        LOG.debugf("Create a Network Policy => %s", np);
        return np;
    }

    private static void addPodSelector(NetworkPolicyFluent<NetworkPolicyBuilder>.SpecNested<NetworkPolicyBuilder> builder, Passport passport) {
        builder.withNewPodSelector()
                .withMatchLabels(Utils.allInstanceLabels(passport))
                .endPodSelector();
    }

    private static void addApplicationPorts(NetworkPolicyFluent<NetworkPolicyBuilder>.SpecNested<NetworkPolicyBuilder> builder, Passport passport) {
        var tlsEnabled = CRDUtils.isTlsConfigured(passport);
        var httpEnabled = Optional.ofNullable(passport.getSpec())
                .map(PassportSpec::getHttpSpec)
                .map(HttpSpec::getHttpEnabled)
                .orElse(false);
        if (!tlsEnabled || httpEnabled) {
            addIngress(builder, HttpSpec.httpPort(passport), NetworkPolicySpec.httpRules(passport));
        }

        if (tlsEnabled) {
            addIngress(builder, HttpSpec.httpsPort(passport), NetworkPolicySpec.httpsRules(passport));
        }
    }

    private static void addManagementPorts(NetworkPolicyFluent<NetworkPolicyBuilder>.SpecNested<NetworkPolicyBuilder> builder, Passport passport) {
        addIngress(builder, HttpManagementSpec.managementPort(passport), NetworkPolicySpec.managementRules(passport));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static void addIngress(NetworkPolicyFluent<NetworkPolicyBuilder>.SpecNested<NetworkPolicyBuilder> builder,
                                   int port,
                                   Optional<List<NetworkPolicyPeer>> networkPolicyPeers) {
        var ingress = builder.addNewIngress();
        ingress.addNewPort()
                .withPort(new IntOrString(port))
                .withProtocol(PASSPORT_SERVICE_PROTOCOL)
                .endPort();

        networkPolicyPeers
                .filter(Predicate.not(Collection::isEmpty))
                .ifPresent(ingress::addAllToFrom);
        ingress.endIngress();
    }

    private static void addJGroupsPorts(NetworkPolicyFluent<NetworkPolicyBuilder>.SpecNested<NetworkPolicyBuilder> builder, Passport passport) {
        var ingressBuilder = builder.addNewIngress();
        ingressBuilder.addNewPort()
                .withPort(new IntOrString(PASSPORT_JGROUPS_DATA_PORT))
                .withProtocol(PASSPORT_JGROUPS_PROTOCOL)
                .endPort();
        ingressBuilder.addNewPort()
                .withPort(new IntOrString(PASSPORT_JGROUPS_FD_PORT))
                .withProtocol(PASSPORT_JGROUPS_PROTOCOL)
                .endPort();
        ingressBuilder.addNewFrom()
                .withNewPodSelector()
                .addToMatchLabels(Utils.allInstanceLabels(passport))
                .endPodSelector()
                .endFrom();
        ingressBuilder.endIngress();
    }

    private static void addMetadata(NetworkPolicyBuilder builder, Passport passport) {
        builder.withNewMetadata()
                .withName(NetworkPolicySpec.networkPolicyName(passport))
                .withNamespace(passport.getMetadata().getNamespace())
                .addToLabels(Utils.allInstanceLabels(passport))
                .endMetadata();
    }
}
