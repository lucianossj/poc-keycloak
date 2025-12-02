-- Keycloak PostgreSQL initialization script
-- Keycloak will auto-create its tables on first startup
-- This script adds any additional custom tables if needed

-- Grant all privileges to keycloak user
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;

-- Create extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Keycloak manages its own schema, but we can add custom tables here if needed
-- The following is a placeholder for any application-specific tables

-- Example: Custom user profile extension table (optional)
CREATE TABLE IF NOT EXISTS custom_user_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    keycloak_user_id VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255),
    avatar_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_custom_user_profiles_keycloak_id ON custom_user_profiles(keycloak_user_id);
