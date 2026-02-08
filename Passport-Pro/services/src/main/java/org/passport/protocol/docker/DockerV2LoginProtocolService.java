package org.passport.protocol.docker;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.passport.common.Profile;
import org.passport.events.EventBuilder;
import org.passport.events.EventType;
import org.passport.models.PassportSession;
import org.passport.services.resources.RealmsResource;
import org.passport.utils.ProfileHelper;

public class DockerV2LoginProtocolService {

    private final EventBuilder event;

    private final PassportSession session;

    public DockerV2LoginProtocolService(final PassportSession session, final EventBuilder event) {
        this.session = session;
        this.event = event;
    }

    public static UriBuilder authProtocolBaseUrl(final UriInfo uriInfo) {
        final UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
        return authProtocolBaseUrl(baseUriBuilder);
    }

    public static UriBuilder authProtocolBaseUrl(final UriBuilder baseUriBuilder) {
        return baseUriBuilder.path(RealmsResource.class).path("{realm}/protocol/" + DockerAuthV2Protocol.LOGIN_PROTOCOL);
    }

    public static UriBuilder authUrl(final UriInfo uriInfo) {
        final UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
        return authUrl(baseUriBuilder);
    }

    public static UriBuilder authUrl(final UriBuilder baseUriBuilder) {
        final UriBuilder uriBuilder = authProtocolBaseUrl(baseUriBuilder);
        return uriBuilder.path(DockerV2LoginProtocolService.class, "auth");
    }

    /**
     * Authorization endpoint
     */
    @Path("auth")
    public Object auth() {
        ProfileHelper.requireFeature(Profile.Feature.DOCKER);

        return new DockerEndpoint(session, event, EventType.LOGIN);
    }
}
