package org.passport.testsuite.util.oauth.device;

import java.io.IOException;

import org.passport.OAuth2Constants;
import org.passport.testsuite.util.oauth.AbstractHttpPostRequest;
import org.passport.testsuite.util.oauth.AbstractOAuthClient;
import org.passport.testsuite.util.oauth.AccessTokenResponse;
import org.passport.testsuite.util.oauth.PkceGenerator;

import org.apache.http.client.methods.CloseableHttpResponse;

public class DeviceTokenRequest extends AbstractHttpPostRequest<DeviceTokenRequest, AccessTokenResponse> {

    private final String deviceCode;

    DeviceTokenRequest(String deviceCode, AbstractOAuthClient<?> client) {
        super(client);
        this.deviceCode = deviceCode;
    }

    public DeviceTokenRequest codeVerifier(PkceGenerator pkceGenerator) {
        if (pkceGenerator != null) {
            codeVerifier(pkceGenerator.getCodeVerifier());
        }
        return this;
    }

    public DeviceTokenRequest codeVerifier(String codeVerifier) {
        parameter(OAuth2Constants.CODE_VERIFIER, codeVerifier);
        return this;
    }

    @Override
    protected String getEndpoint() {
        return client.getEndpoints().getToken();
    }

    @Override
    protected void initRequest() {
        parameter(OAuth2Constants.GRANT_TYPE, OAuth2Constants.DEVICE_CODE_GRANT_TYPE);
        parameter("device_code", deviceCode);
    }

    @Override
    protected AccessTokenResponse toResponse(CloseableHttpResponse response) throws IOException {
        return new AccessTokenResponse(response);
    }

}
