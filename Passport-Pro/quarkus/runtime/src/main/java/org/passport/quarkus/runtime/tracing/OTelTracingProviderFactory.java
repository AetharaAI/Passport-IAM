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

package org.passport.quarkus.runtime.tracing;

import jakarta.enterprise.inject.spi.CDI;

import org.passport.Config;
import org.passport.common.Profile;
import org.passport.config.TracingOptions;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.quarkus.runtime.configuration.Configuration;
import org.passport.tracing.TracingProvider;
import org.passport.tracing.TracingProviderFactory;

import io.opentelemetry.api.OpenTelemetry;

public class OTelTracingProviderFactory implements TracingProviderFactory {
    public static final String PROVIDER_ID = "opentelemetry";
    private static OpenTelemetry OTEL_SINGLETON;

    @Override
    public TracingProvider create(PassportSession session) {
        if (OTEL_SINGLETON == null) {
            OTEL_SINGLETON = CDI.current().select(OpenTelemetry.class).get();
        }

        return new OTelTracingProvider(OTEL_SINGLETON);
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(PassportSessionFactory factory) {

    }

    @Override
    public void close() {
        if (OTEL_SINGLETON != null) {
            // explicitly remove the OpenTelemetry bean
            CDI.current().select(OpenTelemetry.class).destroy(OTEL_SINGLETON);
            OTEL_SINGLETON = null;
        }
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.OPENTELEMETRY) && Configuration.isTrue(TracingOptions.TRACING_ENABLED);
    }
}
