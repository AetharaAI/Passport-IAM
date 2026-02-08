package org.passport.testframework.remote;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.passport.models.PassportSession;
import org.passport.testframework.TestFrameworkExecutor;
import org.passport.testframework.TestFrameworkExtension;
import org.passport.testframework.injection.Registry;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.remote.annotations.TestOnServer;
import org.passport.testframework.remote.runonserver.RunOnServerClient;
import org.passport.testframework.remote.runonserver.RunOnServerSupplier;
import org.passport.testframework.remote.runonserver.RunTestOnServer;
import org.passport.testframework.remote.runonserver.TestClassServerSupplier;
import org.passport.testframework.remote.timeoffset.TimeOffsetSupplier;

public class RemoteTestFrameworkExtension implements TestFrameworkExtension, TestFrameworkExecutor {
    @Override
    public List<Supplier<?, ?>> suppliers() {
        return List.of(
                new TimeOffsetSupplier(),
                new RunOnServerSupplier(),
                new RemoteProvidersSupplier(),
                new TestClassServerSupplier()
        );
    }

    @Override
    public List<Class<?>> alwaysEnabledValueTypes() {
        return List.of(RemoteProviders.class);
    }

    @Override
    public List<Class<?>> getMethodValueTypes(Method method) {
        return isTestOnServer(method) ? List.of(RunOnServerClient.class) : Collections.emptyList();
    }

    @Override
    public boolean supportsParameter(Method method, Class<?> parameterType) {
        return isTestOnServer(method) && parameterType.equals(PassportSession.class);
    }

    @Override
    public boolean shouldExecute(Method testMethod) {
        return isTestOnServer(testMethod);
    }

    @Override
    public void execute(Registry registry, Class<?> testClass, Method testMethod) {
        RunOnServerClient value = (RunOnServerClient) registry.getDeployedInstances().stream().filter(i -> i.getRequestedValueType() != null && i.getRequestedValueType().equals(RunOnServerClient.class)).findFirst().get().getValue();

        RunTestOnServer runTestOnServer = new RunTestOnServer(testClass.getName(), testMethod.getName());
        value.run(runTestOnServer);
    }

    private boolean isTestOnServer(Method method) {
        return method.isAnnotationPresent(TestOnServer.class);
    }

}
