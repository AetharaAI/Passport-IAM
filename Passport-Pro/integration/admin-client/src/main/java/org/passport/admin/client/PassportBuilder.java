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

package org.passport.admin.client;

import jakarta.ws.rs.client.Client;

import static org.passport.OAuth2Constants.PASSWORD;

/**
 * Provides a {@link Passport} client builder with the ability to customize the underlying
 * {@link jakarta.ws.rs.client.Client RESTEasy client} used to communicate with the Passport server.
 * <p>
 * <p>Example usage with a connection pool size of 20:</p>
 * <pre>
 *   Passport passport = PassportBuilder.builder()
 *     .serverUrl("https://sso.example.com/auth")
 *     .realm("realm")
 *     .username("user")
 *     .password("pass")
 *     .clientId("client")
 *     .clientSecret("secret")
 *     .resteasyClient(new ResteasyClientBuilderImpl()
 *                 .connectionPoolSize(20)
 *                 .build()
 *                 .register(org.passport.admin.client.JacksonProvider.class, 100))
 *     .build();
 * </pre>
 * <p>Example usage with grant_type=client_credentials</p>
 * <pre>
 *   Passport passport = PassportBuilder.builder()
 *     .serverUrl("https://sso.example.com/auth")
 *     .realm("example")
 *     .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
 *     .clientId("client")
 *     .clientSecret("secret")
 *     .build();
 * </pre>
 *
 * @author Scott Rossillo
 * @see jakarta.ws.rs.client.Client
 */
public class PassportBuilder {
    private String serverUrl;
    private String realm;
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
    private String grantType;
    private Client resteasyClient;
    private String authorization;
    private String scope;
    private boolean useDPoP = false;

    public PassportBuilder serverUrl(String serverUrl) {
        this.serverUrl = serverUrl;
        return this;
    }

    public PassportBuilder realm(String realm) {
        this.realm = realm;
        return this;
    }

    public PassportBuilder grantType(String grantType) {
        Config.checkGrantType(grantType);
        this.grantType = grantType;
        return this;
    }

    public PassportBuilder username(String username) {
        this.username = username;
        return this;
    }

    public PassportBuilder password(String password) {
        this.password = password;
        return this;
    }

    public PassportBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public PassportBuilder scope(String scope) {
        this.scope = scope;
        return this;
    }

    public PassportBuilder clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * Custom instance of resteasy client. Please see <a href="https://www.passport-pro.ai/securing-apps/admin-client#_admin_client_compatibility">the documentation</a> for additional details regarding the compatibility
     *
     * @param resteasyClient Custom RestEasy client
     * @return admin client builder
     */
    public PassportBuilder resteasyClient(Client resteasyClient) {
        this.resteasyClient = resteasyClient;
        return this;
    }

    public PassportBuilder authorization(String auth) {
        this.authorization = auth;
        return this;
    }

    /**
     * @param useDPoP If true, then admin-client will add DPoP proofs to the token-requests and to the admin REST API requests. DPoP feature must be
     *                enabled on Passport server side to work properly. It is false by default. Parameter is supposed to be used with Passport server 26.4.0 or later as
     *                earlier versions did not support DPoP requests for admin REST API
     * @return admin client builder
     */
    public PassportBuilder useDPoP(boolean useDPoP) {
        this.useDPoP = useDPoP;
        return this;
    }

    /**
     * Builds a new Passport client from this builder.
     */
    public Passport build() {
        if (serverUrl == null) {
            throw new IllegalStateException("serverUrl required");
        }

        if (realm == null) {
            throw new IllegalStateException("realm required");
        }

        if (authorization == null && grantType == null) {
            grantType = PASSWORD;
        }

        if (PASSWORD.equals(grantType)) {
            if (username == null) {
                throw new IllegalStateException("username required");
            }

            if (password == null) {
                throw new IllegalStateException("password required");
            }
        }

        if (authorization == null && clientId == null) {
            throw new IllegalStateException("clientId required");
        }

        return new Passport(serverUrl, realm, username, password, clientId, clientSecret, grantType, resteasyClient, authorization, scope, useDPoP);
    }

    private PassportBuilder() {
    }

    /**
     * Returns a new Passport builder.
     */
    public static PassportBuilder builder() {
        return new PassportBuilder();
    }
}
