package org.passport.testsuite.util.oauth.oid4vc;

import java.io.IOException;

import org.passport.protocol.oid4vc.model.CredentialResponse;
import org.passport.testsuite.util.oauth.AbstractHttpResponse;

import org.apache.http.client.methods.CloseableHttpResponse;

public class Oid4vcCredentialResponse extends AbstractHttpResponse {

    private CredentialResponse credentialResponse;

    public Oid4vcCredentialResponse(CloseableHttpResponse response) throws IOException {
        super(response);
    }

    @Override
    protected void parseContent() throws IOException {
        credentialResponse = asJson(CredentialResponse.class);
    }

    public CredentialResponse getCredentialResponse() {
        return credentialResponse;
    }

}
