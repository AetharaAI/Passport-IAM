package org.passport.testframework.injection.mocks;

import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;

import org.eclipse.microprofile.config.inject.ConfigProperty;

public class MockParentSupplier implements Supplier<MockParentValue, MockParentAnnotation> {

    @ConfigProperty(name = "string")
    String configString;

    @ConfigProperty(name = "boolean", defaultValue = "true")
    boolean configBoolean;

    public static LifeCycle DEFAULT_LIFECYCLE = LifeCycle.CLASS;
    public static boolean COMPATIBLE = true;

    public static void reset() {
        DEFAULT_LIFECYCLE = LifeCycle.CLASS;
        COMPATIBLE = true;
    }

    @Override
    public MockParentValue getValue(InstanceContext<MockParentValue, MockParentAnnotation> instanceContext) {
        return new MockParentValue(configString, configBoolean);
    }

    @Override
    public boolean compatible(InstanceContext<MockParentValue, MockParentAnnotation> a, RequestedInstance<MockParentValue, MockParentAnnotation> b) {
        return COMPATIBLE;
    }

    @Override
    public void close(InstanceContext<MockParentValue, MockParentAnnotation> instanceContext) {
        instanceContext.getValue().close();
    }

    @Override
    public LifeCycle getDefaultLifecycle() {
        return DEFAULT_LIFECYCLE;
    }
}
