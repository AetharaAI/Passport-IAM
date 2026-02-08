package org.passport.services.ui.extend;

import org.passport.component.ComponentFactory;
import org.passport.component.ComponentModel;
import org.passport.models.PassportSession;

public interface UiPageProviderFactory<T> extends ComponentFactory<T, UiPageProvider> {
    default T create(PassportSession session, ComponentModel model) {
        return null;
    }
}
