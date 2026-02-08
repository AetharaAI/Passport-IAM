package com.aetherpro.passport.agency;

/**
 * Types of legal principals that can be represented in the Agency system
 */
public enum PrincipalType {
    /** Individual person */
    INDIVIDUAL,
    
    /** Organization (company, NGO, government agency) */
    ORGANIZATION,
    
    /** System/service account */
    SYSTEM,
    
    /** AI agent with persistent identity */
    AI_AGENT,
    
    /** Smart contract or automated entity */
    SMART_CONTRACT
}
