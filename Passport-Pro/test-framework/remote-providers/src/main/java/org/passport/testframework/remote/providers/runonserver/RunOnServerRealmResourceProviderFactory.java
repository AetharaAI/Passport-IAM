package org.passport.testframework.remote.providers.runonserver;

import java.net.MalformedURLException;

import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.services.resource.RealmResourceProvider;
import org.passport.services.resource.RealmResourceProviderFactory;

public class RunOnServerRealmResourceProviderFactory implements RealmResourceProviderFactory {

    private static final String ID = "testing-run-on-server";

    private String executionId;
    private ClassLoader testClassLoader;

    @Override
    public RealmResourceProvider create(PassportSession session) {
        return new RunOnServerRealmResourceProvider(session, this);
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

    public ClassLoader getTestClassLoader(String executionId) {
        if (!executionId.equals(this.executionId)) {
            try {
                testClassLoader = new TestClassLoader();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            this.executionId = executionId;
        }
        return testClassLoader;
    }

    @Override
    public void init(org.passport.Config.Scope config) {
        try {
            testClassLoader = new TestClassLoader();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
