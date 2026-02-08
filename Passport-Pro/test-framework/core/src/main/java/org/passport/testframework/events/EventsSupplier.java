package org.passport.testframework.events;

import org.passport.testframework.annotations.InjectEvents;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.RealmConfigBuilder;

public class EventsSupplier extends AbstractEventsSupplier<Events, InjectEvents> {

    @Override
    public Events getValue(InstanceContext<Events, InjectEvents> instanceContext) {
        return super.getValue(instanceContext);
    }

    @Override
    protected Events createValue(ManagedRealm realm) {
        return new Events(realm);
    }

    @Override
    public RealmConfigBuilder intercept(RealmConfigBuilder realm, InstanceContext<Events, InjectEvents> instanceContext) {
        return realm.eventsEnabled(true);
    }

}
