package com.aetherpro.passport.agency;

/**
 * Types of delegation relationships between agents and principals
 */
public enum DelegationType {
    /** Full authority to act on behalf of principal */
    FULL,
    
    /** Limited to specific scopes/actions */
    LIMITED,
    
    /** Requires additional conditions to be met */
    CONDITIONAL,
    
    /** Emergency/break-glass access with enhanced auditing */
    EMERGENCY,
    
    /** Time-limited temporary delegation */
    TEMPORARY
}
