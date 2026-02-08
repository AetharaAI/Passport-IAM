package org.passport.migration.migrators;

import org.passport.migration.ModelVersion;
import org.passport.models.AccountRoles;
import org.passport.models.ClientModel;
import org.passport.models.Constants;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.RoleModel;
import org.passport.representations.idm.RealmRepresentation;

public class MigrateTo20_0_0 implements Migration {

    public static final ModelVersion VERSION = new ModelVersion("20.0.0");

    @Override
    public void migrate(PassportSession session) {

        session.realms().getRealmsStream().forEach(this::addViewGroupsRole);
    }

    @Override
    public void migrateImport(PassportSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        addViewGroupsRole(realm);
    }

    private void addViewGroupsRole(RealmModel realm) {
        ClientModel accountClient = realm.getClientByClientId(Constants.ACCOUNT_MANAGEMENT_CLIENT_ID);
        if (accountClient != null && accountClient.getRole(AccountRoles.VIEW_GROUPS) == null) {
            RoleModel viewGroupsRole = accountClient.addRole(AccountRoles.VIEW_GROUPS);
            viewGroupsRole.setDescription("${role_" + AccountRoles.VIEW_GROUPS + "}");
            ClientModel accountConsoleClient = realm.getClientByClientId(Constants.ACCOUNT_CONSOLE_CLIENT_ID);
            accountConsoleClient.addScopeMapping(viewGroupsRole);
        }
    }

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }
}
