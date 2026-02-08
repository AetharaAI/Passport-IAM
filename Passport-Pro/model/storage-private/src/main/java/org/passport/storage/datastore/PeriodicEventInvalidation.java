package org.passport.storage.datastore;

import org.passport.provider.InvalidationHandler;

public enum PeriodicEventInvalidation implements InvalidationHandler.InvalidableObjectType {
    JPA_EVENT_STORE,
}
