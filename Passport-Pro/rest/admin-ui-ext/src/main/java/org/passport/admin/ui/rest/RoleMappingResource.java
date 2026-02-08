package org.passport.admin.ui.rest;

import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.services.resources.admin.fgap.AdminPermissionEvaluator;

public abstract class RoleMappingResource {
    protected final PassportSession session;
    protected final RealmModel realm;
    protected final AdminPermissionEvaluator auth;

    public RoleMappingResource(PassportSession session, RealmModel realm, AdminPermissionEvaluator auth) {
        this.session = session;
        this.realm = realm;
        this.auth = auth;
    }
}
