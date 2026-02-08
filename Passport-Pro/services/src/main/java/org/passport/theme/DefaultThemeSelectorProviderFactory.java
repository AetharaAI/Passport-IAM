package org.passport.theme;

import org.passport.Config;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;

public class DefaultThemeSelectorProviderFactory implements ThemeSelectorProviderFactory {

    @Override
    public ThemeSelectorProvider create(PassportSession session) {
        return new DefaultThemeSelectorProvider(session);
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
