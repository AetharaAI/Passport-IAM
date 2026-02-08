package org.passport.migration.migrators;

import org.passport.migration.ModelVersion;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.representations.idm.RealmRepresentation;

public class MigrateTo21_0_0 implements Migration {

    public static final ModelVersion VERSION = new ModelVersion("21.0.0");

    @Override
    public void migrate(PassportSession session) {
        session.realms().getRealmsStream().forEach(this::updateAdminTheme);
    }

    @Override
    public void migrateImport(PassportSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        updateAdminTheme(realm);
    }

    private void updateAdminTheme(RealmModel realm) {
        String adminTheme = realm.getAdminTheme();
        if ("passport".equals(adminTheme) || "rh-sso".equals(adminTheme)) {
            realm.setAdminTheme("passport.v2");
        }
    }

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }
}
