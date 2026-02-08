package org.passport.testframework.events;

import java.lang.annotation.Annotation;
import java.util.List;

import org.passport.testframework.injection.DependenciesBuilder;
import org.passport.testframework.injection.Dependency;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.injection.SupplierHelpers;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.RealmConfigInterceptor;

@SuppressWarnings("rawtypes")
public abstract class AbstractEventsSupplier<E extends AbstractEvents, A extends Annotation> implements Supplier<E, A>, RealmConfigInterceptor<E, A> {

    @Override
    public List<Dependency> getDependencies(RequestedInstance<E, A> instanceContext) {
        return DependenciesBuilder.create(ManagedRealm.class, SupplierHelpers.getAnnotationField(instanceContext.getAnnotation(), "realmRef")).build();
    }

    @Override
    public E getValue(InstanceContext<E, A> instanceContext) {
        String realmRef = SupplierHelpers.getAnnotationField(instanceContext.getAnnotation(), "realmRef");
        ManagedRealm realm = instanceContext.getDependency(ManagedRealm.class, realmRef);
        return createValue(realm);
    }

    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    @Override
    public boolean compatible(InstanceContext<E, A> a, RequestedInstance<E, A> b) {
        return true;
    }

    @Override
    public void onBeforeEach(InstanceContext<E, A> instanceContext) {
        instanceContext.getValue().testStarted();
    }

    @Override
    public void close(InstanceContext<E, A> instanceContext) {
        instanceContext.getValue().clear();
    }

    protected abstract E createValue(ManagedRealm realm);

}
