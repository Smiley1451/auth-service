--We use CASCADE to remove any dependent objects (like foreign keys relying on these tables/types)
DROP TABLE IF EXISTS blacklisted_tokens CASCADE;
DROP TABLE IF EXISTS oauth_users CASCADE;
DROP TABLE IF EXISTS users CASCADE;

DROP TYPE IF EXISTS role CASCADE;
DROP TYPE IF EXISTS status CASCADE;
DROP TYPE IF EXISTS provider CASCADE;

-- 2. Create Enums (matching Java Enum constants)
-- Based on Role.java
CREATE TYPE role AS ENUM ('ADMIN', 'TEACHER', 'STUDENT');

-- Based on Status.java
CREATE TYPE status AS ENUM ('PENDING', 'ACTIVE', 'BLOCKED');

-- Based on OAuthProvider.java and inferred from repository casting (::provider)
CREATE TYPE provider AS ENUM ('GOOGLE', 'GITHUB');

-- 3. Create Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL,
    email VARCHAR(255) UNIQUE, -- @Email validation implies format, usually unique in auth systems
    password_hash VARCHAR(128), -- Nullable for OAuth users who might not have a password
    role role,                 -- Uses the custom 'role' enum type
    status status,             -- Uses the custom 'status' enum type
    mfa_enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Create OAuth Users Table
CREATE TABLE oauth_users (
    id VARCHAR(255) PRIMARY KEY, -- Entity defines ID as String
    provider provider NOT NULL,  -- Uses the custom 'provider' enum type
    external_id VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL,       -- Foreign key to users table
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_oauth_user_users
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    -- Ensure one user doesn't link the same provider account twice
    CONSTRAINT uq_provider_external_id
        UNIQUE (provider, external_id)
);

-- 5. Create Blacklisted Tokens Table
CREATE TABLE blacklisted_tokens (
    id VARCHAR(255) PRIMARY KEY, -- Entity defines ID as String
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    user_id UUID NOT NULL,       -- Foreign key to users table

    CONSTRAINT fk_blacklisted_token_users
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- 6. Create Indexes for Performance
-- Speeds up lookups by email (common in login)
CREATE INDEX idx_users_email ON users(email);
-- Speeds up lookups by username
CREATE INDEX idx_users_username ON users(username);
-- Speeds up token validation checks
CREATE INDEX idx_blacklisted_tokens_hash ON blacklisted_tokens(token_hash);