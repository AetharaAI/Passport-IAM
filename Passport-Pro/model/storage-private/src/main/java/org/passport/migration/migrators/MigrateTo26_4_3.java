package org.passport.migration.migrators;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.passport.authorization.AuthorizationProvider;
import org.passport.authorization.fgap.AdminPermissionsSchema;
import org.passport.authorization.model.Policy;
import org.passport.authorization.model.Policy.FilterOption;
import org.passport.authorization.model.Resource;
import org.passport.authorization.model.ResourceServer;
import org.passport.authorization.model.Scope;
import org.passport.authorization.store.ResourceStore;
import org.passport.authorization.store.ScopeStore;
import org.passport.authorization.store.StoreFactory;
import org.passport.migration.ModelVersion;
import org.passport.models.ClientModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;

public class MigrateTo26_4_3 extends RealmMigration {

    public static final ModelVersion VERSION = new ModelVersion("26.4.3");

    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }


    @Override
    public void migrateRealm(PassportSession session, RealmModel realm) {
        ClientModel client = realm.getAdminPermissionsClient();

        if (client == null) {
            return;
        }

        AuthorizationProvider authorizationProvider = session.getProvider(AuthorizationProvider.class);
        StoreFactory storeFactory = authorizationProvider.getStoreFactory();
        ResourceServer resourceServer = storeFactory.getResourceServerStore().findByClient(client);

        if (resourceServer == null) {
            return;
        }

        ScopeStore scopeStore = storeFactory.getScopeStore();
        Scope resetPassword = scopeStore.findByName(resourceServer, AdminPermissionsSchema.RESET_PASSWORD);

        if (resetPassword == null) {
            resetPassword = scopeStore.create(resourceServer, AdminPermissionsSchema.RESET_PASSWORD);
        }

        ResourceStore resourceStore = storeFactory.getResourceStore();
        String userResourceType = AdminPermissionsSchema.USERS.getType();
        Resource resourceTypeResource = resourceStore.findByName(resourceServer, userResourceType);
        Set<Scope> newScopes = new HashSet<>(resourceTypeResource.getScopes());

        newScopes.add(resetPassword);

        resourceTypeResource.updateScopes(newScopes);

        for (Policy policy : storeFactory.getPolicyStore().find(resourceServer, Map.of(FilterOption.CONFIG, new String[]{"defaultResourceType", userResourceType}), -1, -1)) {
            for (Resource resource : policy.getResources()) {
                resource.updateScopes(newScopes);
            }
        }
    }
}
