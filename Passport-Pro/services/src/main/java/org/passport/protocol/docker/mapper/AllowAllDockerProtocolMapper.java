package org.passport.protocol.docker.mapper;

import org.passport.models.AuthenticatedClientSessionModel;
import org.passport.models.PassportSession;
import org.passport.models.ProtocolMapperModel;
import org.passport.models.UserSessionModel;
import org.passport.protocol.docker.DockerAuthV2Protocol;
import org.passport.representations.docker.DockerAccess;
import org.passport.representations.docker.DockerResponseToken;

/**
 * Populates token with requested scope.  If more scopes are present than what has been requested, they will be removed.
 */
public class AllowAllDockerProtocolMapper extends DockerAuthV2ProtocolMapper implements DockerAuthV2AttributeMapper {

    public static final String PROVIDER_ID = "docker-v2-allow-all-mapper";

    @Override
    public String getDisplayType() {
        return "Allow All";
    }

    @Override
    public String getHelpText() {
        return "Allows all grants, returning the full set of requested access attributes as permitted attributes.";
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public boolean appliesTo(final DockerResponseToken responseToken) {
        return true;
    }

    @Override
    public DockerResponseToken transformDockerResponseToken(final DockerResponseToken responseToken, final ProtocolMapperModel mappingModel,
                                                            final PassportSession session, final UserSessionModel userSession, final AuthenticatedClientSessionModel clientSession) {

        responseToken.getAccessItems().clear();

        final String requestedScopes = clientSession.getNote(DockerAuthV2Protocol.SCOPE_PARAM);
        if (requestedScopes != null) {
            for (String requestedScope : requestedScopes.split(" ")) {
                final DockerAccess requestedAccess = new DockerAccess(requestedScope);
                responseToken.getAccessItems().add(requestedAccess);
            }
        }

        return responseToken;
    }
}
