package org.passport.admin.api;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import org.passport.Config;
import org.passport.admin.api.client.ClientsApi;
import org.passport.admin.api.client.DefaultClientsApi;
import org.passport.models.AdminRoles;
import org.passport.models.PassportSession;
import org.passport.protocol.oidc.TokenManager;
import org.passport.services.resources.admin.AdminAuth;
import org.passport.services.resources.admin.AdminRoot;
import org.passport.services.resources.admin.RealmAdminResource;
import org.passport.services.resources.admin.RealmsAdminResource;

public class DefaultAdminApi implements AdminApi {
    private final PassportSession session;
    private final RealmsAdminResource realmsAdminResource;
    private final RealmAdminResource realmAdminResource;
    private final AdminAuth auth;

    public DefaultAdminApi(PassportSession session, String realmName) {
        this.session = session;
        this.auth = AdminRoot.authenticateRealmAdminRequest(session);

        // TODO: refine permissions
        if (!auth.getRealm().getName().equals(Config.getAdminRealm()) || !auth.hasRealmRole(AdminRoles.ADMIN)) {
            throw new NotAuthorizedException("Wrong permissions");
        }
        this.realmsAdminResource = new RealmsAdminResource(session, auth, new TokenManager());
        this.realmAdminResource = realmsAdminResource.getRealmAdmin(realmName);
    }

    @Path("clients/{version:v\\d+}")
    @Override
    public ClientsApi clients(@PathParam("version") String version) {
        return switch (version) {
            case "v2" -> new DefaultClientsApi(session, realmAdminResource);
            default -> throw new NotFoundException();
        };
    }
}
