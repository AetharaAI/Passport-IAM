package org.passport.admin.api;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import org.passport.common.Profile;
import org.passport.models.PassportSession;
import org.passport.services.resources.admin.AdminCorsPreflightService;

import org.eclipse.microprofile.openapi.annotations.Operation;

@Provider
@Path("admin/api")
public class AdminRootV2 {

    @Context
    protected PassportSession session;

    @Path("{realmName}")
    public AdminApi adminApi(@PathParam("realmName") String realmName) {
        checkApiEnabled();
        return new DefaultAdminApi(session, realmName);
    }

    // TODO Fix preflights
    @Path("{realmName}/{any:.*}")
    @OPTIONS
    @Operation(hidden = true)
    public Response preFlight() {
        checkApiEnabled();
        return new AdminCorsPreflightService().preflight();
    }

    private void checkApiEnabled() {
        if (!isAdminApiV2Enabled()) {
            throw new NotFoundException();
        }
    }

    public static boolean isAdminApiV2Enabled() {
        return Profile.isFeatureEnabled(Profile.Feature.CLIENT_ADMIN_API_V2); // There's currently only Client API for the new Admin API v2
    }
}
