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
package org.passport.component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.passport.Config.Scope;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.provider.InvalidationHandler.ObjectType;
import org.passport.provider.Provider;
import org.passport.provider.ProviderConfigProperty;
import org.passport.provider.ProviderFactory;

/**
 * Ancestor for a provider factory for both a standalone {@link ProviderFactory} and a {@link ComponentFactory}. It
 * behaves as usual for a standalone provider, and for a component creates a factory customized according to
 * configuration of this component. The component creation then behaves in the same way as if it was
 * a standalone component, i.e.:
 * <ul>
 * <li>The component-specific factory is initialized via {@link #init} method where the configuration
 *     is taken from the component configuration, converted into a {@link Scope}. The
 *     component configuration takes precedence over configuration of the provider factory.</li>
 * <li>Creation of the instances is done via standard {@link #create(PassportSession)} method even for components,
 *     since there is now a specific factory per component.</li>
 * <li>Component-specific factories are cached inside the provider factory
 *     similarly to how provider factories are cached in the session factory.</li>
 * </ul>
 *
 * @see ComponentFactoryProviderFactory
 *
 * @author hmlnarik
 */
public interface AmphibianProviderFactory<ProviderType extends Provider> extends ProviderFactory<ProviderType>, ComponentFactory<ProviderType, ProviderType> {

    @Override
    ProviderType create(PassportSession session);

    @Override
    @Deprecated
    default ProviderType create(PassportSession session, ComponentModel model) {
        throw new UnsupportedOperationException("Use create(PassportSession) instead");
    }

    @Override
    default List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    @Override
    default void onUpdate(PassportSession session, RealmModel realm, ComponentModel oldModel, ComponentModel newModel) {
        String oldId = oldModel == null ? null : oldModel.getId();
        String newId = newModel == null ? null : newModel.getId();
        if (oldId != null) {
            if (newId == null || Objects.equals(oldId, newId)) {
                session.invalidate(ObjectType.COMPONENT, oldId);
            } else {
                session.invalidate(ObjectType.COMPONENT, oldId, newId);
            }
        } else if (newId != null) {
            session.invalidate(ObjectType.COMPONENT, newId);
        }
    }

    @Override
    default void preRemove(PassportSession session, RealmModel realm, ComponentModel model) {
        if (model != null && model.getId() != null) {
            session.invalidate(ObjectType.COMPONENT, model.getId());
        }
    }

    @Override
    default void close() {
    }
}
