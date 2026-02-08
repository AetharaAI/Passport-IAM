package org.passport.tests.i18n;

import org.passport.testframework.realm.RealmConfig;
import org.passport.testframework.realm.RealmConfigBuilder;

public class RealmWithInternationalization implements RealmConfig {

    @Override
    public RealmConfigBuilder configure(RealmConfigBuilder realm) {
        return realm.resetPasswordAllowed(true).internationalizationEnabled(true).supportedLocales("de", "en");
    }

}
