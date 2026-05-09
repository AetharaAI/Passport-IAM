package com.aetherpro.passport.agency.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * DTO for a signed action with three-party signature chain.
 */
public class SignedAction {

    @JsonProperty("action_data")
    private String actionData;

    @JsonProperty("delegate_kid")
    private String delegateKid;

    @JsonProperty("delegate_signature")
    private String delegateSignature;

    @JsonProperty("principal_kid")
    private String principalKid;

    @JsonProperty("principal_signature")
    private String principalSignature;

    @JsonProperty("issuer_kid")
    private String issuerKid;

    @JsonProperty("issuer_signature")
    private String issuerSignature;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    // Getters and Setters

    public String getActionData() {
        return actionData;
    }

    public void setActionData(String actionData) {
        this.actionData = actionData;
    }

    public String getDelegateKid() {
        return delegateKid;
    }

    public void setDelegateKid(String delegateKid) {
        this.delegateKid = delegateKid;
    }

    public String getDelegateSignature() {
        return delegateSignature;
    }

    public void setDelegateSignature(String delegateSignature) {
        this.delegateSignature = delegateSignature;
    }

    public String getPrincipalKid() {
        return principalKid;
    }

    public void setPrincipalKid(String principalKid) {
        this.principalKid = principalKid;
    }

    public String getPrincipalSignature() {
        return principalSignature;
    }

    public void setPrincipalSignature(String principalSignature) {
        this.principalSignature = principalSignature;
    }

    public String getIssuerKid() {
        return issuerKid;
    }

    public void setIssuerKid(String issuerKid) {
        this.issuerKid = issuerKid;
    }

    public String getIssuerSignature() {
        return issuerSignature;
    }

    public void setIssuerSignature(String issuerSignature) {
        this.issuerSignature = issuerSignature;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
