package org.passport.services.ui.extend;

import java.util.HashMap;
import java.util.Map;

import org.passport.component.ComponentFactory;
import org.passport.component.ComponentModel;
import org.passport.models.PassportSession;

public interface UiTabProviderFactory<T> extends ComponentFactory<T, UiTabProvider> {
    default T create(PassportSession session, ComponentModel model) {
        return null;
    }

    @Override
    default Map<String, Object> getTypeMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("path", getPath());
        metadata.put("params", getParams());
        return metadata;
    }

    String getPath();

    Map<String, String> getParams();
}
