package org.passport.device;

import org.passport.provider.Provider;
import org.passport.representations.account.DeviceRepresentation;

public interface DeviceRepresentationProvider extends Provider {

    DeviceRepresentation deviceRepresentation();

    @Override
    default void close() {
    }
}
