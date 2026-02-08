/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
package org.passport.quarkus.runtime.services.health;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import org.passport.connections.infinispan.InfinispanConnectionProvider;
import org.passport.connections.infinispan.InfinispanConnectionProviderFactory;
import org.passport.infinispan.util.InfinispanUtils;
import org.passport.quarkus.runtime.integration.QuarkusPassportSessionFactory;

import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.health.HealthCheckResponse;

import static org.passport.quarkus.runtime.services.health.PassportReadyHealthCheck.DATE_FORMATTER;
import static org.passport.quarkus.runtime.services.health.PassportReadyHealthCheck.FAILING_SINCE;

public class PassportClusterReadyHealthCheck implements AsyncHealthCheck {

    private final AtomicReference<Instant> failingSince = new AtomicReference<>();

    @Override
    public Uni<HealthCheckResponse> call() {
        var builder = HealthCheckResponse.named("Passport cluster health check").up();
        if (InfinispanUtils.isRemoteInfinispan()) {
            return Uni.createFrom().item(builder.build());
        }
        var sessionFactory = QuarkusPassportSessionFactory.getInstance();
        InfinispanConnectionProviderFactory factory = (InfinispanConnectionProviderFactory) sessionFactory.getProviderFactory(InfinispanConnectionProvider.class);
        if (factory.isClusterHealthy()) {
            failingSince.set(null);
        } else {
            builder.down();
            Instant failingTime = failingSince.updateAndGet(PassportReadyHealthCheck::createInstanceIfNeeded);
            builder.withData(FAILING_SINCE, DATE_FORMATTER.format(failingTime));
        }
        return Uni.createFrom().item(builder.build());
    }
}
