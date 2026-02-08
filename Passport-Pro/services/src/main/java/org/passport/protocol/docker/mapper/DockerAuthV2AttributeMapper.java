package org.passport.protocol.docker.mapper;

import org.passport.models.AuthenticatedClientSessionModel;
import org.passport.models.PassportSession;
import org.passport.models.ProtocolMapperModel;
import org.passport.models.UserSessionModel;
import org.passport.representations.docker.DockerResponseToken;

public interface DockerAuthV2AttributeMapper {

    boolean appliesTo(DockerResponseToken responseToken);

    DockerResponseToken transformDockerResponseToken(DockerResponseToken responseToken, ProtocolMapperModel mappingModel,
                                                     PassportSession session, UserSessionModel userSession, AuthenticatedClientSessionModel clientSession);
}
