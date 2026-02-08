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
package org.passport.services.resources.admin;

import java.util.HashMap;
import java.util.Map;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.passport.common.ClientConnection;
import org.passport.component.ComponentModel;
import org.passport.events.admin.OperationType;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RealmModel;
import org.passport.services.ErrorResponse;
import org.passport.services.ServicesLogger;
import org.passport.services.managers.LDAPServerCapabilitiesManager;
import org.passport.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.passport.storage.UserStoragePrivateUtil;
import org.passport.storage.UserStorageProvider;
import org.passport.storage.UserStorageProviderModel;
import org.passport.storage.ldap.LDAPStorageProvider;
import org.passport.storage.ldap.mappers.LDAPStorageMapper;
import org.passport.storage.user.SynchronizationResult;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

/**
 * @resource User Storage Provider
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UserStorageProviderResource {
    private static final Logger logger = Logger.getLogger(UserStorageProviderResource.class);

    protected final RealmModel realm;

    protected final AdminPermissionEvaluator auth;

    protected final AdminEventBuilder adminEvent;

    protected final ClientConnection clientConnection;

    protected final PassportSession session;

    protected final HttpHeaders headers;

    public UserStorageProviderResource(PassportSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.session = session;
        this.auth = auth;
        this.realm = session.getContext().getRealm();
        this.adminEvent = adminEvent;
        this.clientConnection = session.getContext().getConnection();
        this.headers = session.getContext().getRequestHeaders();
    }

    public static String getErrorCode(Throwable throwable) {
        if (throwable instanceof org.passport.models.ModelException) {
           if (throwable.getCause() != null) {
                return getErrorCode(throwable.getCause());
            }
        }
        return LDAPServerCapabilitiesManager.getErrorCode(throwable);
    }

    /**
     * Need this for admin console to display simple name of provider when displaying user detail
     *
     * PASSPORT-4328
     *
     * @param id
     * @return
     */
    @GET
    @Path("{id}/name")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getSimpleName(@PathParam("id") String id) {
        auth.users().requireQuery();

        ComponentModel model = realm.getComponent(id);
        if (model == null) {
            throw new NotFoundException("Could not find component");
        }
        if (!model.getProviderType().equals(UserStorageProvider.class.getName())) {
            throw new NotFoundException("found, but not a UserStorageProvider");
        }

        Map<String, String> data = new HashMap<>();
        data.put("id", model.getId());
        data.put("name", model.getName());
        return data;
    }


    /**
     * Trigger sync of users
     *
     * Action can be "triggerFullSync" or "triggerChangedUsersSync"
     *
     * @param id
     * @param action
     * @return
     */
    @POST
    @Path("{id}/sync")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public SynchronizationResult syncUsers(@PathParam("id") String id,
                                           @QueryParam("action") String action) {
        auth.users().requireManage();

        ComponentModel model = realm.getComponent(id);
        if (model == null) {
            throw new NotFoundException("Could not find component");
        }
        if (!model.getProviderType().equals(UserStorageProvider.class.getName())) {
            throw new NotFoundException("found, but not a UserStorageProvider");
        }

        UserStorageProviderModel providerModel = new UserStorageProviderModel(model);



        logger.debug("Syncing users");

        SynchronizationResult syncResult;
        PassportSessionFactory sessionFactory = session.getPassportSessionFactory();

        if ("triggerFullSync".equals(action)) {
            try {
                syncResult = UserStoragePrivateUtil.runFullSync(sessionFactory, providerModel);
            } catch(Exception e) {
                String errorMsg = getErrorCode(e);
                throw ErrorResponse.error(errorMsg, Response.Status.BAD_REQUEST);
            }
        } else if ("triggerChangedUsersSync".equals(action)) {
            try {
                syncResult = UserStoragePrivateUtil.runPeriodicSync(sessionFactory, providerModel);
            } catch(Exception e) {
                String errorMsg = getErrorCode(e);
                throw ErrorResponse.error(errorMsg, Response.Status.BAD_REQUEST);
            }
        } else if (action == null || action.isEmpty()) {
            logger.debug("Missing action");
            throw new BadRequestException("Missing action");
        } else {
            logger.debug("Unknown action: " + action);
            throw new BadRequestException("Unknown action: " + action);
        }

        Map<String, Object> eventRep = new HashMap<>();
        eventRep.put("action", action);
        eventRep.put("result", syncResult);
        adminEvent.operation(OperationType.ACTION).resourcePath(session.getContext().getUri()).representation(eventRep).success();

        return syncResult;
    }

    /**
     * Remove imported users
     *
     *
     * @param id
     * @return
     */
    @POST
    @Path("{id}/remove-imported-users")
    @NoCache
    public void removeImportedUsers(@PathParam("id") String id) {
        auth.users().requireManage();

        ComponentModel model = realm.getComponent(id);
        if (model == null) {
            throw new NotFoundException("Could not find component");
        }
        if (!model.getProviderType().equals(UserStorageProvider.class.getName())) {
            throw new NotFoundException("found, but not a UserStorageProvider");
        }

        session.users().removeImportedUsers(realm, id);
    }
    /**
     * Unlink imported users from a storage provider
     *
     *
     * @param id
     * @return
     */
    @POST
    @Path("{id}/unlink-users")
    @NoCache
    public void unlinkUsers(@PathParam("id") String id) {
        auth.users().requireManage();

        ComponentModel model = realm.getComponent(id);
        if (model == null) {
            throw new NotFoundException("Could not find component");
        }
        if (!model.getProviderType().equals(UserStorageProvider.class.getName())) {
            throw new NotFoundException("found, but not a UserStorageProvider");
        }

        session.users().unlinkUsers(realm, id);
    }

    /**
     * Trigger sync of mapper data related to ldap mapper (roles, groups, ...)
     *
     * direction is "fedToPassport" or "passportToFed"
     *
     * @return
     */
    @POST
    @Path("{parentId}/mappers/{id}/sync")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public SynchronizationResult syncMapperData(@PathParam("parentId") String parentId, @PathParam("id") String mapperId, @QueryParam("direction") String direction) {
        auth.users().requireManage();

        ComponentModel parentModel = realm.getComponent(parentId);
        if (parentModel == null) throw new NotFoundException("Parent model not found");
        ComponentModel mapperModel = realm.getComponent(mapperId);
        if (mapperModel == null) throw new NotFoundException("Mapper model not found");

        LDAPStorageProvider ldapProvider = (LDAPStorageProvider) session.getProvider(UserStorageProvider.class, parentModel);
        LDAPStorageMapper mapper = session.getProvider(LDAPStorageMapper.class, mapperModel);

        ServicesLogger.LOGGER.syncingDataForMapper(mapperModel.getName(), mapperModel.getProviderId(), direction);

        SynchronizationResult syncResult;
        if ("fedToPassport".equals(direction)) {
            try {
                syncResult = mapper.syncDataFromFederationProviderToPassport(realm);
            } catch(Exception e) {
                String errorMsg = getErrorCode(e);
                throw ErrorResponse.error(errorMsg, Response.Status.BAD_REQUEST);
            }
        } else if ("passportToFed".equals(direction)) {
            try {
                syncResult = mapper.syncDataFromPassportToFederationProvider(realm);
            } catch(Exception e) {
                String errorMsg = getErrorCode(e);
                throw ErrorResponse.error(errorMsg, Response.Status.BAD_REQUEST);
            }
        } else {
            throw new BadRequestException("Unknown direction: " + direction);
        }

        Map<String, Object> eventRep = new HashMap<>();
        eventRep.put("action", direction);
        eventRep.put("result", syncResult);
        adminEvent.operation(OperationType.ACTION).resourcePath(session.getContext().getUri()).representation(eventRep).success();
        return syncResult;
    }
}
