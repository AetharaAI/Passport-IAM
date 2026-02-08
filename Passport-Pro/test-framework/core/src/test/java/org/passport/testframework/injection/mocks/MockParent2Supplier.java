package org.passport.testframework.injection.mocks;

import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;

public class MockParent2Supplier implements Supplier<MockParentValue, MockParentAnnotation> {

    public static LifeCycle DEFAULT_LIFECYCLE = LifeCycle.CLASS;

    public static void reset() {
        DEFAULT_LIFECYCLE = LifeCycle.CLASS;
    }

    @Override
    public MockParentValue getValue(InstanceContext<MockParentValue, MockParentAnnotation> instanceContext) {
        return new MockParentValue(null, false);
    }

    @Override
    public boolean compatible(InstanceContext<MockParentValue, MockParentAnnotation> a, RequestedInstance<MockParentValue, MockParentAnnotation> b) {
        return true;
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
