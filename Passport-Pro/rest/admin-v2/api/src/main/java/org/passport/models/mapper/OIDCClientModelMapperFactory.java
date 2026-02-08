package org.passport.models.mapper;

import org.passport.Config;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.representations.admin.v2.OIDCClientRepresentation;

/**
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public class OIDCClientModelMapperFactory implements ClientModelMapperFactory {
    @Override
    public ClientModelMapper create(PassportSession session) {
        return new OIDCClientModelMapper(session);
    }

    @Override
    public String getId() {
        return OIDCClientRepresentation.PROTOCOL;
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(PassportSessionFactory factory) {
    }

    @Override
    public void close() {
    }
}
