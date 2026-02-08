package com.aetherpro.passport.agency.admin;

import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.services.resources.admin.AdminEventBuilder;
import org.passport.services.resources.admin.ext.AdminRealmResourceProvider;
import org.passport.services.resources.admin.fgap.AdminPermissionEvaluator;

/**
 * Admin Realm Resource Provider for Agency/LBAC
 * 
 * This is the proper Passport extension point for adding REST endpoints
 * to the Admin API. When a request comes to /admin/realms/{realm}/agency,
 * Passport will delegate to this provider.
 * 
 * This is completely additive - does not modify any existing Passport behavior.
 */
public class AgencyAdminResourceProvider implements AdminRealmResourceProvider {
    
    private final PassportSession session;
    
    public AgencyAdminResourceProvider(PassportSession session) {
        this.session = session;
    }
    
    @Override
    public Object getResource(PassportSession session, RealmModel realm, 
                             AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        return new AgencyAdminResource(session, realm, auth);
    }
    
    @Override
    public void close() {
        // Nothing to close
    }
}
