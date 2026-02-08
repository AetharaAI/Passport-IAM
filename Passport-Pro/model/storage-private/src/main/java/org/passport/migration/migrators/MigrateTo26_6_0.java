package org.passport.migration.migrators;

import org.passport.migration.ModelVersion;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.utils.DefaultAuthenticationFlows;


public class MigrateTo26_6_0 extends RealmMigration {

    public static final ModelVersion VERSION = new ModelVersion("26.6.0");

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }


    @Override
    public void migrateRealm(PassportSession session, RealmModel realm) {
        DefaultAuthenticationFlows.addOrganizationBrowserFlowStep(realm, realm.getBrowserFlow());
    }
}
