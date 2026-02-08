-- =====================================================
-- Passport Pro Database Initialization
-- =====================================================

-- Create schema for Passport tables
CREATE SCHEMA IF NOT EXISTS passport;

-- Grant permissions  
GRANT ALL ON SCHEMA passport TO passport_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA passport TO passport_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA passport TO passport_admin;

-- Set default search path
ALTER DATABASE passport_iam SET search_path TO passport, public;

-- Create extensions for advanced features
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Log successful initialization
DO $$
BEGIN
    RAISE NOTICE 'Passport Pro database initialized successfully';
END $$;
