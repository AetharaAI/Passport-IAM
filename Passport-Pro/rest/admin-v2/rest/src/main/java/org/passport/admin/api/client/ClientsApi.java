package org.passport.admin.api.client;

import java.util.stream.Stream;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.passport.representations.admin.v2.BaseClientRepresentation;
import org.passport.services.resources.PassportOpenAPI;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = PassportOpenAPI.Admin.Tags.CLIENTS_V2)
@Extension(name = PassportOpenAPI.Profiles.ADMIN, value = "")
public interface ClientsApi {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all clients", description = "Returns a list of all clients in the realm")
    Stream<BaseClientRepresentation> getClients();

    /**
     * @return {@link BaseClientRepresentation} of created client
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new client", description = "Creates a new client in the realm")
    Response createClient(@Valid BaseClientRepresentation client);

    @Path("{id}")
    ClientApi client(@PathParam("id") String id);
}
