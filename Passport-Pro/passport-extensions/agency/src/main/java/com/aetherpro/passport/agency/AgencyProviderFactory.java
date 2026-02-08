package com.aetherpro.passport.agency;

import org.passport.models.PassportSession;
import org.passport.provider.ProviderFactory;

/**
 * Factory interface for creating AgencyProvider instances
 */
public interface AgencyProviderFactory extends ProviderFactory<AgencyProvider> {
    
    @Override
    AgencyProvider create(PassportSession session);
    
    @Override
    default void init(org.passport.Config.Scope config) {
        // Default implementation - override if configuration needed
    }
    
    @Override
    default void postInit(org.passport.models.PassportSessionFactory factory) {
        // Default implementation - override if post-init needed
    }
    
    @Override
    default void close() {
        // Default implementation
    }
}
