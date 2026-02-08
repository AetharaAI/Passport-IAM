package org.passport.cookie;

import org.passport.Config;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;

public class DefaultCookieProviderFactory implements CookieProviderFactory {

    @Override
    public CookieProvider create(PassportSession session) {
        return new DefaultCookieProvider(session);
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

    @Override
    public String getId() {
        return "default";
    }

}
