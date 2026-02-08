package org.passport.admin.ui.rest;

import org.passport.Config;
import org.passport.common.Profile;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RealmModel;
import org.passport.provider.EnvironmentDependentProviderFactory;
import org.passport.services.resources.admin.AdminEventBuilder;
import org.passport.services.resources.admin.ext.AdminRealmResourceProvider;
import org.passport.services.resources.admin.ext.AdminRealmResourceProviderFactory;
import org.passport.services.resources.admin.fgap.AdminPermissionEvaluator;

public final class AdminExtProvider implements AdminRealmResourceProviderFactory, AdminRealmResourceProvider, EnvironmentDependentProviderFactory {
    public AdminRealmResourceProvider create(PassportSession session) {
        return this;
    }

    public void init(Config.Scope config) {
    }

    public void postInit(PassportSessionFactory factory) {
    }

    public void close() {
    }

    public String getId() {
        return "ui-ext";
    }

    public Object getResource(PassportSession session, RealmModel realm, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        return new AdminExtResource(session, realm, auth, adminEvent);
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.ADMIN_V2);
    }
}
