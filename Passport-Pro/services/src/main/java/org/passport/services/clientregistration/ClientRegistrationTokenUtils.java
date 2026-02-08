/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.passport.services.clientregistration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.passport.TokenCategory;
import org.passport.TokenVerifier;
import org.passport.common.VerificationException;
import org.passport.common.util.SecretGenerator;
import org.passport.crypto.SignatureProvider;
import org.passport.crypto.SignatureSignerContext;
import org.passport.crypto.SignatureVerifierContext;
import org.passport.jose.jws.JWSBuilder;
import org.passport.models.ClientInitialAccessModel;
import org.passport.models.ClientModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.protocol.oidc.TokenManager.TokenRevocationCheck;
import org.passport.representations.AccessToken;
import org.passport.representations.JsonWebToken;
import org.passport.services.Urls;
import org.passport.services.clientregistration.policy.RegistrationAuth;
import org.passport.util.TokenUtil;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ClientRegistrationTokenUtils {

    public static final String TYPE_INITIAL_ACCESS_TOKEN = "InitialAccessToken";
    public static final String TYPE_REGISTRATION_ACCESS_TOKEN = "RegistrationAccessToken";

    public static String updateTokenSignature(PassportSession session, ClientRegistrationAuth auth) {
        String algorithm = session.tokens().signatureAlgorithm(TokenCategory.INTERNAL);
        SignatureSignerContext signer = session.getProvider(SignatureProvider.class, algorithm).signer();

        if (signer.getKid().equals(auth.getKid())) {
            return auth.getToken();
        } else {
            RegistrationAccessToken regToken = new RegistrationAccessToken();
            regToken.setRegistrationAuth(auth.getRegistrationAuth().toString().toLowerCase());

            regToken.type(auth.getJwt().getType());
            regToken.id(auth.getJwt().getId());
            regToken.issuedNow();
            regToken.issuer(auth.getJwt().getIssuer());
            regToken.audience(auth.getJwt().getIssuer());

            String token = new JWSBuilder().jsonContent(regToken).sign(signer);
            return token;
        }
    }

    public static String updateRegistrationAccessToken(PassportSession session, ClientModel client, RegistrationAuth registrationAuth, List<String> webOrigins) {
        return updateRegistrationAccessToken(session, session.getContext().getRealm(), client, registrationAuth, webOrigins);
    }

    public static String updateRegistrationAccessToken(PassportSession session, RealmModel realm, ClientModel client, RegistrationAuth registrationAuth, List<String> webOrigins) {
        String id = SecretGenerator.getInstance().generateSecureID();
        client.setRegistrationToken(id);

        RegistrationAccessToken regToken = new RegistrationAccessToken();
        regToken.setRegistrationAuth(registrationAuth.toString().toLowerCase());

        return setupToken(regToken, session, realm, id, TYPE_REGISTRATION_ACCESS_TOKEN, 0, webOrigins);
    }

    public static String createInitialAccessToken(PassportSession session, RealmModel realm, ClientInitialAccessModel model, List<String> webOrigins) {
        InitialAccessToken initialToken = new InitialAccessToken();
        return setupToken(initialToken, session, realm, model.getId(), TYPE_INITIAL_ACCESS_TOKEN, model.getExpiration() > 0 ? model.getTimestamp() + model.getExpiration() : 0, webOrigins);
    }

    public static TokenVerification verifyToken(PassportSession session, RealmModel realm, String token) {
        if (token == null) {
            return TokenVerification.error(new RuntimeException("Missing token"));
        }

        String kid;
        AccessToken jwt;
        try {
            TokenVerifier<AccessToken> verifier = TokenVerifier.create(token, AccessToken.class)
                    .withChecks(new TokenVerifier.RealmUrlCheck(getIssuer(session, realm)), TokenVerifier.IS_ACTIVE, new TokenRevocationCheck(session));

            SignatureVerifierContext verifierContext = session.getProvider(SignatureProvider.class, verifier.getHeader().getAlgorithm().name()).verifier(verifier.getHeader().getKeyId());
            verifier.verifierContext(verifierContext);

            kid = verifierContext.getKid();

            verifier.verify();

            jwt = verifier.getToken();
        } catch (VerificationException e) {
            return TokenVerification.error(new RuntimeException("Failed decode token", e));
        }

        if (!(TokenUtil.TOKEN_TYPE_BEARER.equals(jwt.getType()) ||
                TYPE_INITIAL_ACCESS_TOKEN.equals(jwt.getType()) ||
                TYPE_REGISTRATION_ACCESS_TOKEN.equals(jwt.getType()))) {
            return TokenVerification.error(new RuntimeException("Invalid type of token"));
        }

        return TokenVerification.success(kid, jwt);
    }

    private static String setupToken(JsonWebToken jwt, PassportSession session, RealmModel realm, String id, String type, long expiration, List<String> webOrigins) {
        String issuer = getIssuer(session, realm);

        jwt.type(type);
        jwt.id(id);
        jwt.issuedNow();
        jwt.exp(expiration);
        jwt.issuer(issuer);
        jwt.audience(issuer);

        Set<String> webOriginsSet = webOrigins != null ? new HashSet<>(webOrigins) : null;
        if (jwt instanceof InitialAccessToken) {
            ((InitialAccessToken) jwt).setAllowedOrigins(webOriginsSet);
        } else if (jwt instanceof RegistrationAccessToken) {
            ((RegistrationAccessToken) jwt).setAllowedOrigins(webOriginsSet);
        }

        return session.tokens().encode(jwt);
    }

    private static String getIssuer(PassportSession session, RealmModel realm) {
        return Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName());
    }

    protected static class TokenVerification {

        private final String kid;
        private final AccessToken jwt;
        private final RuntimeException error;

        public static TokenVerification success(String kid, AccessToken jwt) {
            return new TokenVerification(kid, jwt, null);
        }

        public static TokenVerification error(RuntimeException error) {
            return new TokenVerification(null,null, error);
        }

        private TokenVerification(String kid, AccessToken jwt, RuntimeException error) {
            this.kid = kid;
            this.jwt = jwt;
            this.error = error;
        }

        public String getKid() {
            return kid;
        }

        public AccessToken getJwt() {
            return jwt;
        }

        public RuntimeException getError() {
            return error;
        }
    }

}
