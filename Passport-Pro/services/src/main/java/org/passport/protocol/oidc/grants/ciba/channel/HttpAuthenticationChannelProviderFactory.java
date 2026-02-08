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
 *
 */
package org.passport.protocol.oidc.grants.ciba.channel;

import java.util.List;

import org.passport.Config.Scope;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.provider.ProviderConfigProperty;
import org.passport.provider.ProviderConfigurationBuilder;

/**
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class HttpAuthenticationChannelProviderFactory implements AuthenticationChannelProviderFactory {

    public static final String PROVIDER_ID = "ciba-http-auth-channel";

    protected String httpAuthenticationChannelUri;

    @Override
    public AuthenticationChannelProvider create(PassportSession session) {
        return new HttpAuthenticationChannelProvider(session, httpAuthenticationChannelUri);
    }

    @Override
    public void init(Scope config) {
        httpAuthenticationChannelUri = config.get("httpAuthenticationChannelUri");
    }

    @Override
    public void postInit(PassportSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name("httpAuthenticationChannelUri")
                .type("string")
                .helpText("The HTTP(S) URI of the authentication channel.")
                .add()
                .build();
    }
}
