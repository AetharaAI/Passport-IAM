package org.passport.protocol.saml.mappers;

import org.passport.models.AuthenticatedClientSessionModel;
import org.passport.models.PassportSession;
import org.passport.models.ProtocolMapperModel;
import org.passport.models.UserSessionModel;

public interface SAMLNameIdMapper {

    String mapperNameId(String nameIdFormat, ProtocolMapperModel mappingModel, PassportSession session,
                                        UserSessionModel userSession, AuthenticatedClientSessionModel clientSession);

}