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

package org.passport.services.resources.admin;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.passport.http.HttpRequest;
import org.passport.http.HttpResponse;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.representations.idm.ClientPoliciesRepresentation;
import org.passport.services.ErrorResponse;
import org.passport.services.clientpolicy.ClientPolicyException;
import org.passport.services.resources.PassportOpenAPI;
import org.passport.services.resources.admin.fgap.AdminPermissionEvaluator;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

@Extension(name = PassportOpenAPI.Profiles.ADMIN, value = "")
public class ClientPoliciesResource {
    protected static final Logger logger = Logger.getLogger(ClientPoliciesResource.class);

    protected final HttpRequest request;

    protected final HttpResponse response;

    protected final PassportSession session;

    protected final RealmModel realm;
    private final AdminPermissionEvaluator auth;

    public ClientPoliciesResource(PassportSession session, AdminPermissionEvaluator auth) {
        this.session = session;
        this.realm = session.getContext().getRealm();
        this.auth = auth;
        this.request = session.getContext().getHttpRequest();
        this.response = session.getContext().getHttpResponse();
    }

    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = PassportOpenAPI.Admin.Tags.REALMS_ADMIN)
    @Operation()
    public ClientPoliciesRepresentation getPolicies(@QueryParam("include-global-policies") boolean includeGlobalPolicies) {
        auth.realm().requireViewRealm();

        try {
            return session.clientPolicy().getClientPolicies(realm, includeGlobalPolicies);
        } catch (ClientPolicyException e) {
            throw ErrorResponse.error(e.getError(), Response.Status.BAD_REQUEST);
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = PassportOpenAPI.Admin.Tags.REALMS_ADMIN)
    @Operation()
    public Response updatePolicies(final ClientPoliciesRepresentation clientPolicies) {
        auth.realm().requireManageRealm();

        try {
            session.clientPolicy().updateClientPolicies(realm, clientPolicies);
        } catch (ClientPolicyException e) {
            throw ErrorResponse.error(e.getError(), Response.Status.BAD_REQUEST);
        }
        return Response.noContent().build();
    }
}
