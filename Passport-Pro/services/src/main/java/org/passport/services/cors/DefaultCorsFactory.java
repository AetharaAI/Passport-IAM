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

package org.passport.services.cors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.passport.Config;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.provider.ProviderConfigProperty;
import org.passport.provider.ProviderConfigurationBuilder;

/**
 * @author <a href="mailto:demetrio@carretti.pro">Dmitry Telegin</a>
 */
public class DefaultCorsFactory implements CorsFactory {

    private static final String PROVIDER_ID = "default";
    private static final String ALLOWED_HEADERS = "allowedHeaders";
    private String allowedHeaders;

    @Override
    public Cors create(PassportSession session) {
        return new DefaultCors(session, allowedHeaders);
    }

    @Override
    public void init(Config.Scope config) {
        Set<String> allowedHeaders = new HashSet<>(Cors.DEFAULT_ALLOW_HEADERS);

        String[] customAllowedHeaders = config.getArray(ALLOWED_HEADERS);
        if (customAllowedHeaders != null) {
            allowedHeaders.addAll(Arrays.asList(customAllowedHeaders));
        }

        this.allowedHeaders = String.join(", ", allowedHeaders);
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
                .name(ALLOWED_HEADERS)
                .type("string")
                .helpText("A comma-separated list of additional allowed headers for CORS requests")
                .defaultValue("")
                .add()
                .build();
    }
}
