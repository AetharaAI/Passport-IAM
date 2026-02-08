package org.passport.protocol.oidc.mappers;

import org.passport.models.ClientSessionContext;
import org.passport.models.PassportSession;
import org.passport.models.ProtocolMapperModel;
import org.passport.models.UserSessionModel;
import org.passport.representations.AccessToken;

public interface TokenIntrospectionTokenMapper {
    AccessToken transformIntrospectionToken(AccessToken token, ProtocolMapperModel mappingModel, PassportSession session,
                                       UserSessionModel userSession, ClientSessionContext clientSessionCtx);
}
