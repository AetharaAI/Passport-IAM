package org.passport.testframework.remote.runonserver;

import java.io.IOException;
import java.lang.reflect.Method;

import org.passport.common.VerificationException;
import org.passport.models.PassportSession;
import org.passport.testframework.remote.providers.runonserver.RunOnServer;

public class RunTestOnServer implements RunOnServer {

    private final String testClass;
    private final String testMethod;

    public RunTestOnServer(String testClass, String testMethod) {
        this.testClass = testClass;
        this.testMethod = testMethod;
    }

    @Override
    public void run(PassportSession session) throws IOException, VerificationException {
        try {
            Class<?> clazz = this.getClass().getClassLoader().loadClass(testClass);
            Object test = clazz.getDeclaredConstructor().newInstance();
            Method method = clazz.getDeclaredMethod(testMethod, PassportSession.class);
            method.invoke(test, session);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
