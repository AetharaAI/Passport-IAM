package org.passport.providers.example;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.passport.models.PassportSession;
import org.passport.services.resource.RealmResourceProvider;

/**
 *
 * @author <a href="mailto:svacek@redhat.com">Simon Vacek</a>
 */
public class MyCustomRealmResourceProvider  implements RealmResourceProvider {

    private final PassportSession session;

    public MyCustomRealmResourceProvider(PassportSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return this;
    }

    @Override
    public void close() {

    }

    @GET
    @Path("hello")
    @Produces(MediaType.TEXT_PLAIN)
    public Response hello() {
        return Response.ok("Hello World!").type(MediaType.TEXT_PLAIN).build();
    }
}
