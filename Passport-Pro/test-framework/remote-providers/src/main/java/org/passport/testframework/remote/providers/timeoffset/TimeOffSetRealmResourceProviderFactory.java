package org.passport.testframework.remote.providers.timeoffset;

import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.services.resource.RealmResourceProvider;
import org.passport.services.resource.RealmResourceProviderFactory;

public class TimeOffSetRealmResourceProviderFactory implements RealmResourceProviderFactory {

    private final String ID = "testing-timeoffset";

    @Override
    public RealmResourceProvider create(PassportSession session) {
        return new TimeOffSetRealmResourceProvider(session);
    }

    @Override
    public void postInit(PassportSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void init(org.passport.Config.Scope config) {

    }
}
