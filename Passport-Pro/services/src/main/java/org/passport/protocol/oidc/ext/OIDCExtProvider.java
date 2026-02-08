package org.passport.protocol.oidc.ext;

import org.passport.events.EventBuilder;
import org.passport.provider.Provider;

public interface OIDCExtProvider extends Provider {

    void setEvent(EventBuilder event);

    @Override
    default void close() {
    }

}
