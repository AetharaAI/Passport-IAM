package com.aetherpro.passport.agency.jpa;

import com.aetherpro.passport.agency.AgencyProvider;
import com.aetherpro.passport.agency.AgencyProviderFactory;
import jakarta.persistence.EntityManager;
import org.passport.Config;
import org.passport.connections.jpa.JpaConnectionProvider;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;

/**
 * Factory for JPA Agency Provider
 * 
 * Follows Passport's JpaUserProviderFactory pattern exactly:
 * - Gets EntityManager from JpaConnectionProvider
 * - Creates provider with session and em
 */
public class JpaAgencyProviderFactory implements AgencyProviderFactory {
    
    public static final String PROVIDER_ID = "jpa-agency";
    public static final int PROVIDER_PRIORITY = 1;
    
    @Override
    public AgencyProvider create(PassportSession session) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        return new JpaAgencyProvider(session, em);
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
        // Nothing to close at factory level
    }
    
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
    
    @Override
    public int order() {
        return PROVIDER_PRIORITY;
    }
}
