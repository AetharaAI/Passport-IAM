package com.aetherpro.passport.agency.admin;

import org.passport.Config;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.services.resources.admin.ext.AdminRealmResourceProvider;
import org.passport.services.resources.admin.ext.AdminRealmResourceProviderFactory;

/**
 * Factory for Agency Admin Resource Provider
 * 
 * The getId() method returns "agency" which means this provider handles
 * requests to /admin/realms/{realm}/agency
 */
public class AgencyAdminResourceProviderFactory implements AdminRealmResourceProviderFactory {
    
    public static final String PROVIDER_ID = "agency";
    
    @Override
    public AdminRealmResourceProvider create(PassportSession session) {
        return new AgencyAdminResourceProvider(session);
    }
    
    @Override
    public void init(Config.Scope config) {
        // No configuration needed
    }
    
    @Override
    public void postInit(PassportSessionFactory factory) {
        // No post-initialization needed
    }
    
    @Override
    public void close() {
        // Nothing to close
    }
    
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
