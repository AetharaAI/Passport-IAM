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

package org.passport.models;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.passport.component.ComponentModel;
import org.passport.provider.InvalidationHandler;
import org.passport.provider.Provider;
import org.passport.provider.ProviderEventManager;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface PassportSessionFactory extends ProviderEventManager, InvalidationHandler {

    PassportSession create();

    Set<Spi> getSpis();

    Spi getSpi(Class<? extends Provider> providerClass);

    <T extends Provider> ProviderFactory<T> getProviderFactory(Class<T> clazz);

    <T extends Provider> ProviderFactory<T> getProviderFactory(Class<T> clazz, String id);

    <T extends Provider> ProviderFactory<T> getProviderFactory(Class<T> clazz, String realmId, String componentId, Function<PassportSessionFactory, ComponentModel> modelGetter);

    /**
     * Returns stream of provider factories for the given provider.
     * @param clazz {@code Class<? extends Provider>}
     * @return {@code Stream<ProviderFactory>} Stream of provider factories. Never returns {@code null}.
     */
    Stream<ProviderFactory> getProviderFactoriesStream(Class<? extends Provider> clazz);
    
    long getServerStartupTimestamp();

    void close();
}
