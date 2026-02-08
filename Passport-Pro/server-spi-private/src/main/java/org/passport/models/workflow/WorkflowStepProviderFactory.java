/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.passport.models.workflow;

import java.util.List;
import java.util.Set;

import org.passport.Config;
import org.passport.common.Profile;
import org.passport.component.ComponentFactory;
import org.passport.models.PassportSessionFactory;
import org.passport.provider.EnvironmentDependentProviderFactory;
import org.passport.provider.ProviderConfigProperty;

public interface WorkflowStepProviderFactory<P extends WorkflowStepProvider> extends ComponentFactory<P, WorkflowStepProvider>, EnvironmentDependentProviderFactory {

    /**
     * Supported types, usually one type but could be more (RestartStep for example)
     */
    Set<ResourceType> getSupportedResourceTypes();

    @Override
    default void init(Config.Scope config) {
        // no-op default
    }

    @Override
    default void postInit(PassportSessionFactory factory) {
        // no-op default
    }

    @Override
    default void close() {
        // no-op default
    }

    @Override
    default List<ProviderConfigProperty> getConfigProperties() {
        return List.of();
    }

    @Override
    default boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.WORKFLOWS);
    }
}
