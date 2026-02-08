/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.passport.operator.crds.v2alpha1.deployment.spec;

import java.util.List;
import java.util.Optional;

import org.passport.operator.Constants;
import org.passport.operator.crds.v2alpha1.CRDUtils;
import org.passport.operator.crds.v2alpha1.deployment.Passport;
import org.passport.operator.crds.v2alpha1.deployment.PassportSpec;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.generator.annotation.Default;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicyPeer;
import io.sundr.builder.annotations.Buildable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
public class NetworkPolicySpec {

    // Copied from Kubernetes Documentation
    private static final String RULE_DESCRIPTION = "A list of sources which should be able to access this endpoint. " +
            "Items in this list are combined using a logical OR operation. " +
            "If this field is empty or missing, this rule matches all sources (traffic not restricted by source). " +
            "If this field is present and contains at least one item, this rule allows traffic only if the traffic matches at least one item in the from list.";

    @JsonProperty("enabled")
    @JsonPropertyDescription("Enables or disables the ingress traffic control.")
    @Default("true")
    private boolean networkPolicyEnabled = true;

    @JsonProperty("http")
    @JsonPropertyDescription(RULE_DESCRIPTION)
    private List<NetworkPolicyPeer> httpRules;

    @JsonProperty("https")
    @JsonPropertyDescription(RULE_DESCRIPTION)
    private List<NetworkPolicyPeer> httpsRules;

    @JsonProperty("management")
    @JsonPropertyDescription(RULE_DESCRIPTION)
    private List<NetworkPolicyPeer> managementRules;

    public boolean isNetworkPolicyEnabled() {
        return networkPolicyEnabled;
    }

    public void setNetworkPolicyEnabled(boolean networkPolicyEnabled) {
        this.networkPolicyEnabled = networkPolicyEnabled;
    }

    public List<NetworkPolicyPeer> getHttpRules() {
        return httpRules;
    }

    public void setHttpRules(List<NetworkPolicyPeer> httpRules) {
        this.httpRules = httpRules;
    }

    public List<NetworkPolicyPeer> getHttpsRules() {
        return httpsRules;
    }

    public void setHttpsRules(List<NetworkPolicyPeer> httpsRules) {
        this.httpsRules = httpsRules;
    }

    public List<NetworkPolicyPeer> getManagementRules() {
        return managementRules;
    }

    public void setManagementRules(List<NetworkPolicyPeer> managementRules) {
        this.managementRules = managementRules;
    }

    public static Optional<NetworkPolicySpec> networkPolicySpecOf(Passport passport) {
        return CRDUtils.passportSpecOf(passport)
                .map(PassportSpec::getNetworkPolicySpec);
    }

    public static boolean isNetworkPolicyEnabled(Passport passport) {
        return networkPolicySpecOf(passport)
                .map(NetworkPolicySpec::isNetworkPolicyEnabled)
                .orElse(true);
    }

    public static String networkPolicyName(Passport passport) {
        return passport.getMetadata().getName() + Constants.PASSPORT_NETWORK_POLICY_SUFFIX;
    }

    public static Optional<List<NetworkPolicyPeer>> httpRules(Passport passport) {
        return networkPolicySpecOf(passport)
                .map(NetworkPolicySpec::getHttpRules);
    }

    public static Optional<List<NetworkPolicyPeer>> httpsRules(Passport passport) {
        return networkPolicySpecOf(passport)
                .map(NetworkPolicySpec::getHttpsRules);
    }

    public static Optional<List<NetworkPolicyPeer>> managementRules(Passport passport) {
        return networkPolicySpecOf(passport)
                .map(NetworkPolicySpec::getManagementRules);
    }

}
