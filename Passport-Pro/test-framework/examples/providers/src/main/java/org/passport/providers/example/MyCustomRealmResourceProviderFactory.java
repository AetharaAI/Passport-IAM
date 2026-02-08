package org.passport.providers.example;

import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.services.resource.RealmResourceProvider;
import org.passport.services.resource.RealmResourceProviderFactory;

/**
 *
 * @author <a href="mailto:svacek@redhat.com">Simon Vacek</a>
 */
public class MyCustomRealmResourceProviderFactory implements RealmResourceProviderFactory {

    public static final String ID = "custom-provider";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public RealmResourceProvider create(PassportSession session) {
        return new MyCustomRealmResourceProvider(session);
    }

    @Override
    public void init(org.passport.Config.Scope config) {

    }

    @Override
    public void postInit(PassportSessionFactory factory) {

    }

    @Override
    public void close() {

    }
}
