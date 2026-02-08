/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.passport.authorization.protection;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response.Status;

import org.passport.OAuthErrorException;
import org.passport.authorization.AuthorizationProvider;
import org.passport.authorization.admin.ResourceSetService;
import org.passport.authorization.common.PassportIdentity;
import org.passport.authorization.model.ResourceServer;
import org.passport.authorization.protection.permission.PermissionService;
import org.passport.authorization.protection.permission.PermissionTicketService;
import org.passport.authorization.protection.policy.UserManagedPermissionService;
import org.passport.authorization.protection.resource.ResourceService;
import org.passport.common.ClientConnection;
import org.passport.models.ClientModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.services.ErrorResponseException;
import org.passport.services.resources.admin.AdminAuth;
import org.passport.services.resources.admin.AdminEventBuilder;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ProtectionService {

    private final PassportSession session;
    private final AuthorizationProvider authorization;

    protected final ClientConnection clientConnection;

    public ProtectionService(AuthorizationProvider authorization) {
        this.session = authorization.getPassportSession();
        this.authorization = authorization;
        this.clientConnection = session.getContext().getConnection();
    }

    @Path("/resource_set")
    public Object resource() {
        PassportIdentity identity = createIdentity(true);
        ResourceServer resourceServer = getResourceServer(identity);
        ResourceSetService resourceManager = new ResourceSetService(this.session, resourceServer, this.authorization, null, createAdminEventBuilder(identity, resourceServer));
        return new ResourceService(this.session, resourceServer, identity, resourceManager);
    }

    private AdminEventBuilder createAdminEventBuilder(PassportIdentity identity, ResourceServer resourceServer) {
        RealmModel realm = authorization.getRealm();
        ClientModel client = realm.getClientById(resourceServer.getClientId());
        PassportSession passportSession = authorization.getPassportSession();
        UserModel serviceAccount = passportSession.users().getServiceAccount(client);
        AdminEventBuilder adminEvent = new AdminEventBuilder(realm, new AdminAuth(realm, identity.getAccessToken(), serviceAccount, client), passportSession, clientConnection);
        return adminEvent;
    }

    @Path("/permission")
    public Object permission() {
        PassportIdentity identity = createIdentity(false);

        return new PermissionService(identity, getResourceServer(identity), this.authorization);
    }
    
    @Path("/permission/ticket")
    public Object ticket() {
        PassportIdentity identity = createIdentity(false);

        return new PermissionTicketService(identity, getResourceServer(identity), this.authorization);
    }
    
    @Path("/uma-policy")
    public Object policy() {
        PassportIdentity identity = createIdentity(false);

        return new UserManagedPermissionService(identity, getResourceServer(identity), this.authorization, createAdminEventBuilder(identity, getResourceServer(identity)));
    }

    private PassportIdentity createIdentity(boolean checkProtectionScope) {
        PassportIdentity identity = new PassportIdentity(this.authorization.getPassportSession());
        ResourceServer resourceServer = getResourceServer(identity);
        PassportSession passportSession = authorization.getPassportSession();
        RealmModel realm = passportSession.getContext().getRealm();
        ClientModel client = realm.getClientById(resourceServer.getClientId());

        if (checkProtectionScope) {
            if (!identity.hasClientRole(client.getClientId(), "uma_protection")) {
                throw new ErrorResponseException(OAuthErrorException.INVALID_SCOPE, "Requires uma_protection scope.", Status.FORBIDDEN);
            }
        }

        return identity;
    }

    private ResourceServer getResourceServer(PassportIdentity identity) {
        String clientId = identity.getAccessToken().getIssuedFor();
        RealmModel realm = authorization.getPassportSession().getContext().getRealm();
        ClientModel clientModel = realm.getClientByClientId(clientId);

        if (clientModel == null) {
            clientModel = realm.getClientById(clientId);

            if (clientModel == null) {
                throw new ErrorResponseException("invalid_clientId", "Client application with id [" + clientId + "] does not exist in realm [" + realm.getName() + "]", Status.BAD_REQUEST);
            }
        }

        ResourceServer resourceServer = this.authorization.getStoreFactory().getResourceServerStore().findByClient(clientModel);

        if (resourceServer == null) {
            throw new ErrorResponseException("invalid_clientId", "Client application [" + clientModel.getClientId() + "] is not registered as a resource server.", Status.FORBIDDEN);
        }

        return resourceServer;
    }
}
