package org.passport.broker.kubernetes;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.passport.crypto.PublicKeysWrapper;
import org.passport.http.simple.SimpleHttp;
import org.passport.http.simple.SimpleHttpRequest;
import org.passport.jose.jwk.JSONWebKeySet;
import org.passport.jose.jwk.JWK;
import org.passport.jose.jws.JWSInput;
import org.passport.keys.PublicKeyLoader;
import org.passport.models.PassportSession;
import org.passport.protocol.oidc.representations.OIDCConfigurationRepresentation;
import org.passport.representations.JsonWebToken;
import org.passport.util.JWKSUtils;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHeaders;
import org.jboss.logging.Logger;

import static org.passport.broker.kubernetes.KubernetesConstants.SERVICE_ACCOUNT_TOKEN_PATH;

public class KubernetesJwksEndpointLoader implements PublicKeyLoader {

    private static final Logger logger = Logger.getLogger(KubernetesJwksEndpointLoader.class);

    private final PassportSession session;
    private final String issuer;

    public KubernetesJwksEndpointLoader(PassportSession session, String issuer) {
        this.session = session;
        this.issuer = issuer;
    }

    @Override
    public PublicKeysWrapper loadKeys() throws Exception {
        SimpleHttp simpleHttp = SimpleHttp.create(session);

        String token = getToken(issuer);

        String wellKnownEndpoint = issuer + "/.well-known/openid-configuration";

        SimpleHttpRequest wellKnownReqest = simpleHttp.doGet(wellKnownEndpoint).acceptJson();
        if (token != null) {
            wellKnownReqest.auth(token);
        }
        String jwksUri = wellKnownReqest.asJson(OIDCConfigurationRepresentation.class).getJwksUri();

        SimpleHttpRequest jwksRequest = simpleHttp.doGet(jwksUri).header(HttpHeaders.ACCEPT, "application/jwk-set+json");
        if (token != null) {
            jwksRequest.auth(token);
        }

        JSONWebKeySet jwks = jwksRequest.asJson(JSONWebKeySet.class);
        return JWKSUtils.getKeyWrappersForUse(jwks, JWK.Use.SIG);
    }

    private String getToken(String issuer) {
        try {
            File file = new File(SERVICE_ACCOUNT_TOKEN_PATH);
            if (!file.exists()) {
                return null;
            }

            String token = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            JsonWebToken jwt = new JWSInput(token).readJsonContent(JsonWebToken.class);
            if (jwt.getIssuer().equals(issuer)) {
                logger.trace("Including service account token in request");
                return token;
            } else {
                logger.debug("Not including service account token due to issuer missmatch");
            }
        } catch (Exception e) {
            logger.warn("Failed to read service account token file", e);
        }
        return null;
    }
}
