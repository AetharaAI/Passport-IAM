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
package org.passport.testsuite.model.parameters;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.passport.common.util.MultivaluedHashMap;
import org.passport.models.LDAPConstants;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;
import org.passport.storage.UserStorageProvider;
import org.passport.storage.UserStorageProviderModel;
import org.passport.storage.UserStorageProviderSpi;
import org.passport.storage.federated.UserFederatedStorageProviderSpi;
import org.passport.storage.ldap.LDAPStorageProviderFactory;
import org.passport.storage.ldap.mappers.LDAPStorageMapperFactory;
import org.passport.storage.ldap.mappers.LDAPStorageMapperSpi;
import org.passport.testsuite.model.PassportModelParameters;
import org.passport.testsuite.util.LDAPRule;
import org.passport.util.ldap.LDAPEmbeddedServer;

import com.google.common.collect.ImmutableSet;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 *
 * @author hmlnarik
 */
public class LdapUserStorage extends PassportModelParameters {

    static final Set<Class<? extends Spi>> ALLOWED_SPIS = ImmutableSet.<Class<? extends Spi>>builder()
      .add(UserStorageProviderSpi.class)
      .add(UserFederatedStorageProviderSpi.class)
      .add(LDAPStorageMapperSpi.class)

      .build();

    static final Set<Class<? extends ProviderFactory>> ALLOWED_FACTORIES = ImmutableSet.<Class<? extends ProviderFactory>>builder()
      .add(LDAPStorageMapperFactory.class)
      .add(LDAPStorageProviderFactory.class)

      .build();

    private final AtomicInteger counter = new AtomicInteger();

    private final LDAPRule ldapRule = new LDAPRule();

    public LdapUserStorage() {
        super(ALLOWED_SPIS, ALLOWED_FACTORIES);
    }

    @Override
    public <T> Stream<T> getParameters(Class<T> clazz) {
        if (UserStorageProviderModel.class.isAssignableFrom(clazz)) {
            MultivaluedHashMap<String, String> config = new MultivaluedHashMap<>();
            for (java.util.Map.Entry<String, String> entry : ldapRule.getConfig().entrySet()) {
                config.add(entry.getKey(), entry.getValue());
            }
            config.putSingle(LDAPConstants.SYNC_REGISTRATIONS, "true");
            config.putSingle(LDAPConstants.EDIT_MODE, UserStorageProvider.EditMode.WRITABLE.toString());
            config.putSingle(LDAPConstants.CONNECTION_POOLING, "true");

            UserStorageProviderModel federatedStorage = new UserStorageProviderModel();
            federatedStorage.setName(LDAPStorageProviderFactory.PROVIDER_NAME + ":" + counter.getAndIncrement());
            federatedStorage.setProviderId(LDAPStorageProviderFactory.PROVIDER_NAME);
            federatedStorage.setProviderType(UserStorageProvider.class.getName());
            federatedStorage.setLastSync(0);
            federatedStorage.setChangedSyncPeriod(-1);
            federatedStorage.setFullSyncPeriod(-1);
            federatedStorage.setPriority(0);
            federatedStorage.setConfig(config);
            return Stream.of((T) federatedStorage);
        } else {
            return super.getParameters(clazz);
        }
    }

    static {
        System.setProperty(LDAPEmbeddedServer.PROPERTY_ENABLE_SSL, "false");
    }

    @Override
    public Statement classRule(Statement base, Description description) {
        return ldapRule.apply(base, description);
    }

}
