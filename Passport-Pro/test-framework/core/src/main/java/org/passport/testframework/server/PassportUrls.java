package org.passport.testframework.server;

import java.net.MalformedURLException;
import java.net.URL;

import org.passport.common.util.PassportUriBuilder;
import org.passport.protocol.oidc.OIDCLoginProtocol;

public class PassportUrls {

    private final String baseUrl;
    private final String managementBaseUrl;

    public PassportUrls(String baseUrl, String managementBaseUrl) {
        this.baseUrl = baseUrl;
        this.managementBaseUrl = managementBaseUrl;
    }

    public String getBase() {
        return baseUrl;
    }

    public URL getBaseUrl() {
        return toUrl(getBase());
    }

    public String getMasterRealm() {
        return baseUrl + "/realms/master";
    }

    public URL getMasterRealmUrl() {
        return toUrl(getMasterRealm());
    }

    public String getAdmin() {
        return baseUrl + "/admin";
    }

    public URL getAdminUrl() {
        return toUrl(getAdmin());
    }

    public PassportUriBuilder getBaseBuilder() {
        return toBuilder(getBase());
    }

    public PassportUriBuilder getAdminBuilder() {
        return toBuilder(getAdmin());
    }

    public String getMetric() {
        return managementBaseUrl + "/metrics";
    }

    private URL toUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private PassportUriBuilder toBuilder(String url) {
        return PassportUriBuilder.fromUri(url);
    }

    public String getToken(String realm) {
        return baseUrl + "/realms/" + realm + "/protocol/" + OIDCLoginProtocol.LOGIN_PROTOCOL + "/token";
    }
}
