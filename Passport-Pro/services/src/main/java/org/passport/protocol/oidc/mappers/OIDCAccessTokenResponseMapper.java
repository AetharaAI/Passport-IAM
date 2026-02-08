package org.passport.protocol.oidc.mappers;

import org.passport.models.ClientSessionContext;
import org.passport.models.PassportSession;
import org.passport.models.ProtocolMapperModel;
import org.passport.models.UserSessionModel;
import org.passport.representations.AccessTokenResponse;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface OIDCAccessTokenResponseMapper {

    AccessTokenResponse transformAccessTokenResponse(AccessTokenResponse accessTokenResponse, ProtocolMapperModel mappingModel,
                                                     PassportSession session, UserSessionModel userSession,
                                                     ClientSessionContext clientSessionCtx);
}
