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
package org.passport.services.resources.account.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.passport.authorization.AuthorizationProvider;
import org.passport.authorization.model.PermissionTicket;
import org.passport.authorization.model.ResourceServer;
import org.passport.authorization.store.PermissionTicketStore;
import org.passport.authorization.store.ResourceStore;
import org.passport.authorization.store.ScopeStore;
import org.passport.http.HttpRequest;
import org.passport.models.ClientModel;
import org.passport.models.PassportSession;
import org.passport.models.PassportUriInfo;
import org.passport.models.UserModel;
import org.passport.representations.idm.ClientRepresentation;
import org.passport.representations.idm.UserRepresentation;
import org.passport.representations.idm.authorization.ResourceRepresentation;
import org.passport.representations.idm.authorization.ScopeRepresentation;
import org.passport.services.managers.Auth;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public abstract class AbstractResourceService {

    protected final UserModel user;
    protected final AuthorizationProvider provider;
    protected final PermissionTicketStore ticketStore;
    protected final ResourceStore resourceStore;
    protected final ScopeStore scopeStore;
    protected final PassportUriInfo uriInfo;
    protected HttpRequest request;
    protected Auth auth;

    protected AbstractResourceService(PassportSession session, UserModel user, Auth auth, HttpRequest request) {
        this.user = user;
        this.auth = auth;
        this.request = request;
        provider = session.getProvider(AuthorizationProvider.class);
        ticketStore = provider.getStoreFactory().getPermissionTicketStore();
        resourceStore = provider.getStoreFactory().getResourceStore();
        scopeStore = provider.getStoreFactory().getScopeStore();
        uriInfo = session.getContext().getUri();
    }

    public static class Resource extends ResourceRepresentation {

        private Client client;

        public Resource() {
        }

        Resource(org.passport.authorization.model.Resource resource, UserModel owner, AuthorizationProvider provider) {
            setId(resource.getId());
            setName(resource.getName());
            setDisplayName(resource.getDisplayName());
            setUris(resource.getUris());
            setIconUri(resource.getIconUri());

            setScopes(resource.getScopes().stream().map(Scope::new).collect(Collectors.toSet()));

            ResourceServer resourceServer = resource.getResourceServer();
            this.client = new Client(provider.getRealm().getClientById(resourceServer.getClientId()));
        }

        Resource(org.passport.authorization.model.Resource resource, AuthorizationProvider provider) {
            this(resource, null, provider);
        }

        public Client getClient() {
            return client;
        }
    }

    public static class ResourcePermission extends Resource {

        private Map<String, Permission> permissions;

        public ResourcePermission() {

        }

        ResourcePermission(PermissionTicket ticket, AuthorizationProvider provider) {
            super(ticket.getResource(), provider);
            setScopes(new HashSet<>());
        }

        ResourcePermission(org.passport.authorization.model.Resource resource, AuthorizationProvider provider) {
            super(resource, provider);
            setScopes(new HashSet<>());
        }

        public Collection<Permission> getPermissions() {
            if (permissions == null) {
                return null;
            }
            return permissions.values();
        }

        public void setPermissions(Collection<Permission> permissions) {
            for (Permission permission : permissions) {
                addPermission(permission.getUsername(), permission);
            }
        }

        public void addPermission(String requester, Permission permission) {
            if (permissions == null) {
                permissions = new HashMap<>();
            }
            permissions.put(requester, permission);
        }

        public Permission getPermission(String requester) {
            if (permissions == null) {
                return null;
            }
            return permissions.get(requester);
        }
    }

    public static class Permission extends UserRepresentation {

        private List<String> scopes;

        public Permission() {

        }

        Permission(String userId, AuthorizationProvider provider) {
            UserModel user = provider.getPassportSession().users().getUserById(provider.getRealm(), userId);

            setUsername(user.getUsername());
            setFirstName(user.getFirstName());
            setLastName(user.getLastName());
            setEmail(user.getEmail());
        }

        Permission(PermissionTicket ticket, AuthorizationProvider provider) {
            this(ticket.getRequester(), provider);   
        }

        public Permission(String userName, String... scopes) {
            setUsername(userName);
            for (String scope : scopes) {
                addScope(scope);
            }
        }

        public List<String> getScopes() {
            return scopes;
        }

        public void addScope(String... scope) {
            if (scopes == null) {
                scopes = new ArrayList<>();
            }
            scopes.addAll(Arrays.asList(scope));
        }
    }

    public static class Scope extends ScopeRepresentation {

        public Scope() {

        }

        Scope(org.passport.authorization.model.Scope scope) {
            setName(scope.getName());
            setDisplayName(scope.getDisplayName());
            setIconUri(scope.getIconUri());
        }
    }

    public static class Client extends ClientRepresentation {

        public Client() {

        }

        Client(ClientModel client) {
            setClientId(client.getClientId());
            setName(client.getName());
            setBaseUrl(client.getBaseUrl());
        }
    }
}
