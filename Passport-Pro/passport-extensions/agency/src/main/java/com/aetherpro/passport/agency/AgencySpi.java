package com.aetherpro.passport.agency;

import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;

/**
 * Passport-Pro Agency SPI
 * 
 * Provides Legal/Agent-Based Access Control (LBAC) extensions for Passport.
 * This SPI enables:
 * - Principal management (legal entities)
 * - Delegate relationships (agents acting on behalf of principals)
 * - Mandate-based authorizations (time-bound, scoped permissions)
 * - Qualification management (credentials/certifications)
 * - Agent Passport minting (persistent AI agent identities)
 */
public class AgencySpi implements Spi {
    
    public static final String SPI_NAME = "agency";
    
    @Override
    public String getName() {
        return SPI_NAME;
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return AgencyProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return AgencyProviderFactory.class;
    }

    @Override
    public boolean isInternal() {
        return false;
    }
}
