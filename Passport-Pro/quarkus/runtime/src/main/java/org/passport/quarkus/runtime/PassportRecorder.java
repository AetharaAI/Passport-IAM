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
 */

package org.passport.quarkus.runtime;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.passport.Config;
import org.passport.common.Profile;
import org.passport.common.crypto.CryptoIntegration;
import org.passport.common.crypto.CryptoProvider;
import org.passport.common.crypto.FipsMode;
import org.passport.config.DatabaseOptions;
import org.passport.config.HealthOptions;
import org.passport.config.HttpOptions;
import org.passport.config.MetricsOptions;
import org.passport.config.OpenApiOptions;
import org.passport.config.TruststoreOptions;
import org.passport.marshalling.Marshalling;
import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;
import org.passport.quarkus.runtime.configuration.Configuration;
import org.passport.quarkus.runtime.configuration.MicroProfileConfigProvider;
import org.passport.quarkus.runtime.integration.QuarkusPassportSessionFactory;
import org.passport.quarkus.runtime.services.RejectNonNormalizedPathFilter;
import org.passport.quarkus.runtime.storage.database.liquibase.FastServiceLocator;
import org.passport.representations.userprofile.config.UPConfig;
import org.passport.theme.ClasspathThemeProviderFactory;
import org.passport.truststore.TruststoreBuilder;
import org.passport.userprofile.DeclarativeUserProfileProviderFactory;

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.hibernate.orm.runtime.integration.HibernateOrmIntegrationRuntimeInitListener;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import liquibase.Scope;
import liquibase.servicelocator.ServiceLocator;
import org.hibernate.cfg.AvailableSettings;
import org.infinispan.protostream.SerializationContextInitializer;

@Recorder
public class PassportRecorder {

    public void initConfig() {
        Config.init(new MicroProfileConfigProvider());
    }

    public void configureProfile(Profile.ProfileName profileName, Map<Profile.Feature, Boolean> features) {
        Profile.init(profileName, features);
    }

    // default handler for redirecting to specific path
    public Handler<RoutingContext> getRedirectHandler(String redirectPath) {
        return routingContext -> routingContext.redirect(redirectPath);
    }

    private static final List<ManagementInterfaceItem> MANAGEMENT_INTERFACE_ENDPOINTS = List.of(
            new ManagementInterfaceItem("/health", "Health endpoint", () -> Configuration.isTrue(HealthOptions.HEALTH_ENABLED)),
            new ManagementInterfaceItem("/metrics", "Metrics endpoint", () -> Configuration.isTrue(MetricsOptions.METRICS_ENABLED)),
            new ManagementInterfaceItem("/openapi", "OpenAPI specification", () -> Configuration.isTrue(OpenApiOptions.OPENAPI_ENABLED)),
            new ManagementInterfaceItem("/openapi/ui", "OpenAPI UI specification (Swagger)", () -> Configuration.isTrue(OpenApiOptions.OPENAPI_UI_ENABLED))
    );

    // default handler for the management interface
    public Handler<RoutingContext> getManagementHandler() {
        String itemsHtml = "<ul>%s</ul>".formatted(MANAGEMENT_INTERFACE_ENDPOINTS.stream()
                .filter(f -> f.isEnabled.getAsBoolean())
                .map(ManagementInterfaceItem::getListItem)
                .collect(Collectors.joining("\n")));

        return routingContext -> routingContext.response().end("""
                <html>
                <h2>Passport Management Interface</h2>
                %s
                </html>
                """.formatted(itemsHtml));
    }

    private record ManagementInterfaceItem(String path, String description, BooleanSupplier isEnabled) {
        String getListItem() {
            return "<li><a href=\"%s\">%s</a> - %s</li>".formatted(path, path, description);
        }
    }

    public Handler<RoutingContext> getRejectNonNormalizedPathFilter() {
        return !Configuration.isTrue(HttpOptions.HTTP_ACCEPT_NON_NORMALIZED_PATHS) ? new RejectNonNormalizedPathFilter() : null;
    }

    public void configureTruststore() {
        String[] truststores = Configuration.getOptionalKcValue(TruststoreOptions.TRUSTSTORE_PATHS.getKey())
                .map(s -> s.split(",")).orElse(new String[0]);

        Optional<String> dataDir = Environment.getDataDir();

        File truststoresDir = Environment.getHomePath().map(p -> p.resolve("conf").resolve("truststores").toFile()).orElse(null);

        if (truststoresDir != null && truststoresDir.exists() && Optional.ofNullable(truststoresDir.list()).map(a -> a.length).orElse(0) > 0) {
            truststores = Stream.concat(Stream.of(truststoresDir.getAbsolutePath()), Stream.of(truststores)).toArray(String[]::new);
        } else if (truststores.length == 0) {
            return; // nothing to configure, we'll just use the system default
        }

        TruststoreBuilder.setSystemTruststore(truststores, true, dataDir.orElseThrow());
    }

    public void configureLiquibase(Map<String, List<String>> services) {
        ServiceLocator locator = Scope.getCurrentScope().getServiceLocator();
        if (locator instanceof FastServiceLocator) {
            ((FastServiceLocator) locator).initServices(services);
        }
    }

    public void configSessionFactory(
            Map<Spi, Map<Class<? extends Provider>, Map<String, Class<? extends ProviderFactory>>>> factories,
            Map<Class<? extends Provider>, String> defaultProviders,
            Map<String, ProviderFactory> preConfiguredProviders,
            List<ClasspathThemeProviderFactory.ThemesRepresentation> themes) {
        QuarkusPassportSessionFactory.setInstance(new QuarkusPassportSessionFactory(factories, defaultProviders, preConfiguredProviders, themes));
    }

    public void setDefaultUserProfileConfiguration(UPConfig configuration) {
        DeclarativeUserProfileProviderFactory.setDefaultConfig(configuration);
    }

    public HibernateOrmIntegrationRuntimeInitListener createUserDefinedUnitListener(String name) {
        return propertyCollector -> {
            try (InstanceHandle<AgroalDataSource> instance = Arc.container().instance(
                    AgroalDataSource.class, new DataSource() {
                        @Override public Class<? extends Annotation> annotationType() {
                            return DataSource.class;
                        }

                        @Override public String value() {
                            return name;
                        }
                    })) {
                propertyCollector.accept(AvailableSettings.DATASOURCE, instance.get());
            }
        };
    }

    public HibernateOrmIntegrationRuntimeInitListener createDefaultUnitListener() {
        return propertyCollector -> propertyCollector.accept(AvailableSettings.DEFAULT_SCHEMA, Configuration.getConfigValue(DatabaseOptions.DB_SCHEMA).getValue());
    }

    public void setCryptoProvider(FipsMode fipsMode) {
        String cryptoProvider = fipsMode.getProviderClassName();

        try {
            CryptoIntegration.setProvider(
                    (CryptoProvider) Thread.currentThread().getContextClassLoader().loadClass(cryptoProvider).getDeclaredConstructor().newInstance());
        } catch (ClassNotFoundException | NoClassDefFoundError cause) {
            if (fipsMode.isFipsEnabled()) {
                throw new RuntimeException("Failed to configure FIPS. Make sure you have added the Bouncy Castle FIPS dependencies to the 'providers' directory.");
            }
            throw new RuntimeException("Unexpected error when configuring the crypto provider: " + cryptoProvider, cause);
        } catch (Exception cause) {
            throw new RuntimeException("Unexpected error when configuring the crypto provider: " + cryptoProvider, cause);
        }
    }

    public void configureProtoStreamSchemas(List<SerializationContextInitializer> schemas) {
        Marshalling.setSchemas(schemas);
    }
}
