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
package org.passport.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.passport.common.util.StackUtil;
import org.passport.component.ComponentFactory;
import org.passport.component.ComponentModel;
import org.passport.jose.jws.DefaultTokenManager;
import org.passport.keys.DefaultKeyManager;
import org.passport.models.ClientProvider;
import org.passport.models.ClientScopeProvider;
import org.passport.models.GroupProvider;
import org.passport.models.IdentityProviderStorageProvider;
import org.passport.models.KeyManager;
import org.passport.models.PassportContext;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.PassportTransactionManager;
import org.passport.models.RealmModel;
import org.passport.models.RealmProvider;
import org.passport.models.RoleProvider;
import org.passport.models.SingleUseObjectProvider;
import org.passport.models.ThemeManager;
import org.passport.models.TokenManager;
import org.passport.models.UserLoginFailureProvider;
import org.passport.models.UserProvider;
import org.passport.models.UserSessionProvider;
import org.passport.models.utils.PassportModelUtils;
import org.passport.provider.InvalidationHandler.InvalidableObjectType;
import org.passport.provider.InvalidationHandler.ObjectType;
import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;
import org.passport.services.clientpolicy.ClientPolicyManager;
import org.passport.sessions.AuthenticationSessionProvider;
import org.passport.storage.DatastoreProvider;
import org.passport.vault.DefaultVaultTranscriber;
import org.passport.vault.VaultProvider;
import org.passport.vault.VaultTranscriber;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public abstract class DefaultPassportSession implements PassportSession {

    private final DefaultPassportSessionFactory factory;
    private final Map<List<String>, Provider> providers = new HashMap<>();
    private final List<Provider> closable = new LinkedList<>();
    private final DefaultPassportTransactionManager transactionManager;
    private final Map<String, Object> attributes = new HashMap<>();
    private final Map<InvalidableObjectType, Set<Object>> invalidationMap = new HashMap<>();
    private DatastoreProvider datastoreProvider;
    private final PassportContext context;
    private KeyManager keyManager;
    private TokenManager tokenManager;
    private VaultTranscriber vaultTranscriber;
    private ClientPolicyManager clientPolicyManager;
    private boolean closed = false;

    public DefaultPassportSession(DefaultPassportSessionFactory factory) {
        this.factory = factory;
        this.transactionManager = new DefaultPassportTransactionManager(this);
        context = createPassportContext(this);
        LOG.tracef("Created %s%s", this, StackUtil.getShortStackTrace());
    }

    @Override
    public PassportContext getContext() {
        return context;
    }

    private DatastoreProvider getDatastoreProvider() {
        if (this.datastoreProvider == null) {
            this.datastoreProvider = getProvider(DatastoreProvider.class);
        }
        return this.datastoreProvider;
    }

    @Override
    public void invalidate(InvalidableObjectType type, Object... ids) {
        factory.invalidate(this, type, ids);
        if (type == ObjectType.PROVIDER_FACTORY) {
            invalidationMap.computeIfAbsent(type, o -> new HashSet<>()).addAll(Arrays.asList(ids));
        }
    }

    @Override
    public void enlistForClose(Provider provider) {
        for (Provider p : closable) {
            if (p == provider) {    // Do not add the same provider twice
                return;
            }
        }
        closable.add(provider);
    }

    @Override
    public Object getAttribute(String attribute) {
        return attributes.get(attribute);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String attribute, Class<T> clazz) {
        Object value = getAttribute(attribute);
        return clazz.isInstance(value) ? (T) value : null;
    }

    @Override
    public Object removeAttribute(String attribute) {
        return attributes.remove(attribute);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @Override
    public PassportTransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public PassportSessionFactory getPassportSessionFactory() {
        return factory;
    }

    @Override
    public UserProvider users() {
        return getDatastoreProvider().users();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Provider> T getProvider(Class<T> clazz) {
        List<String> key = List.of(clazz.getName());
        return getOrCreateProvider(key, () -> factory.getProviderFactory(clazz));
    }

    private <T extends Provider> T getOrCreateProvider(List<String> key, Supplier<ProviderFactory<T>> supplier) {
        T provider = (T) providers.get(key);
        // PASSPORT-11890 - Avoid using HashMap.computeIfAbsent() to implement logic in outer if() block below,
        // since per JDK-8071667 the remapping function should not modify the map during computation. While
        // allowed on JDK 1.8, attempt of such a modification throws ConcurrentModificationException with JDK 9+
        if (provider == null) {
            ProviderFactory<T> providerFactory = supplier.get();
            if (providerFactory != null) {
                provider = providerFactory.create(DefaultPassportSession.this);
                providers.put(key, provider);
            }
        }
        return provider;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Provider> T getProvider(Class<T> clazz, String id) {
        List<String> key = List.of(clazz.getName(), id);
        return getOrCreateProvider(key, () -> factory.getProviderFactory(clazz, id));
    }

    @Override
    public <T extends Provider> T getComponentProvider(Class<T> clazz, String componentId) {
        final RealmModel realm = getContext().getRealm();
        if (realm == null) {
            throw new IllegalArgumentException("Realm not set in the context.");
        }

        // Loads componentModel from the realm
        return this.getComponentProvider(clazz, componentId, PassportModelUtils.componentModelGetter(realm.getId(), componentId));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Provider> T getComponentProvider(Class<T> clazz, String componentId, Function<PassportSessionFactory, ComponentModel> modelGetter) {
        List<String> key = List.of("component", clazz.getName(), componentId);
        final RealmModel realm = getContext().getRealm();
        return getOrCreateProvider(key, () -> factory.getProviderFactory(clazz, Optional.ofNullable(realm.getId()).orElse(null), componentId, modelGetter));
    }

    @Override
    public <T extends Provider> T getProvider(Class<T> clazz, ComponentModel componentModel) {
        String modelId = componentModel.getId();

        Object found = getAttribute(modelId);
        if (found != null) {
            return clazz.cast(found);
        }

        ProviderFactory<T> providerFactory = factory.getProviderFactory(clazz, componentModel.getProviderId());
        if (providerFactory == null) {
            return null;
        }

        ComponentFactory<T, T> componentFactory = (ComponentFactory<T, T>) providerFactory;
        T provider = componentFactory.create(this, componentModel);
        enlistForClose(provider);
        setAttribute(modelId, provider);

        return provider;
    }

    @Override
    public <T extends Provider> Set<String> listProviderIds(Class<T> clazz) {
        return factory.getAllProviderIds(clazz);
    }

    @Override
    public <T extends Provider> Set<T> getAllProviders(Class<T> clazz) {
        return listProviderIds(clazz).stream()
            .map(id -> getProvider(clazz, id))
            .collect(Collectors.toSet());
    }

    @Override
    public Class<? extends Provider> getProviderClass(String providerClassName) {
        return factory.getProviderClass(providerClassName);
    }

    @Override
    public RealmProvider realms() {
        return getDatastoreProvider().realms();
    }

    @Override
    public ClientProvider clients() {
        return getDatastoreProvider().clients();
    }

    @Override
    public ClientScopeProvider clientScopes() {
        return getDatastoreProvider().clientScopes();
    }

    @Override
    public GroupProvider groups() {
        return getDatastoreProvider().groups();
    }

    @Override
    public RoleProvider roles() {
        return getDatastoreProvider().roles();
    }


    @Override
    public UserSessionProvider sessions() {
        return getDatastoreProvider().userSessions();
    }

    @Override
    public UserLoginFailureProvider loginFailures() {
        return getDatastoreProvider().loginFailures();
    }

    @Override
    public AuthenticationSessionProvider authenticationSessions() {
        return getDatastoreProvider().authSessions();
    }

    @Override
    public SingleUseObjectProvider singleUseObjects() {
        return getDatastoreProvider().singleUseObjects();
    }

    @Override
    public IdentityProviderStorageProvider identityProviders() {
        return getDatastoreProvider().identityProviders();
    }

    @Override
    public KeyManager keys() {
        if (keyManager == null) {
            keyManager = new DefaultKeyManager(this);
        }
        return keyManager;
    }

    @Override
    public ThemeManager theme() {
        return this.getProvider(ThemeManager.class);
    }

    @Override
    public TokenManager tokens() {
        if (tokenManager == null) {
            tokenManager = new DefaultTokenManager(this);
        }
        return tokenManager;
    }

    @Override
    public VaultTranscriber vault() {
        if (this.vaultTranscriber == null) {
            this.vaultTranscriber = new DefaultVaultTranscriber(this.getProvider(VaultProvider.class));
        }
        return this.vaultTranscriber;
    }

    @Override
    public ClientPolicyManager clientPolicy() {
        if (clientPolicyManager == null) {
            clientPolicyManager = getProvider(ClientPolicyManager.class);
        }
        return clientPolicyManager;
    }

    private static final Logger LOG = Logger.getLogger(DefaultPassportSession.class);

    @Override
    public void close() {
        if (LOG.isTraceEnabled()) {
            LOG.tracef("Closing %s%s%s", this,
              getTransactionManager().isActive() ? " (transaction active" + (getTransactionManager().getRollbackOnly() ? ", ROLLBACK-ONLY" : "") + ")" : "",
              StackUtil.getShortStackTrace());
        }

        if (closed) {
            throw new IllegalStateException("Illegal call to #close() on already closed " + this);
        }

        RuntimeException re = closeTransactionManager();

        try {
            Consumer<? super Provider> safeClose = p -> {
                try {
                    if (p != null) {
                        p.close();
                    }
                } catch (Exception e) {
                    LOG.warnf(e, "Unable to close provider %s", p.getClass().getName());
                }
            };
            providers.values().forEach(safeClose);
            closable.forEach(safeClose);
            for (Entry<InvalidableObjectType, Set<Object>> me : invalidationMap.entrySet()) {
                factory.invalidate(this, me.getKey(), me.getValue().toArray());
            }
        } finally {
            this.closed = true;
        }

        if (re != null) {
            throw re;
        }
    }

    protected RuntimeException closeTransactionManager() {
        if (! this.transactionManager.isActive()) {
            return null;
        }

        try {
            if (this.transactionManager.getRollbackOnly()) {
                this.transactionManager.rollback();
            } else {
                this.transactionManager.commit();
            }
        } catch (RuntimeException re) {
            return re;
        }

        return null;
    }

    @Override
    public String toString() {
        return String.format("session @ %08x", System.identityHashCode(this));
    }

    protected abstract DefaultPassportContext createPassportContext(PassportSession session);

    public boolean isClosed() {
        return closed;
    }
}
