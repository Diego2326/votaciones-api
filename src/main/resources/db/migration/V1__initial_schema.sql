CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uk_roles_name ON roles (name);

CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    email VARCHAR(128) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uk_users_username ON app_users (username);
CREATE UNIQUE INDEX uk_users_email ON app_users (email);
CREATE INDEX idx_users_enabled ON app_users (enabled);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES app_users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE tournaments (
    id UUID PRIMARY KEY,
    title VARCHAR(160) NOT NULL,
    description TEXT,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_by UUID NOT NULL,
    start_at TIMESTAMP WITH TIME ZONE,
    end_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_tournaments_created_by FOREIGN KEY (created_by) REFERENCES app_users (id)
);

CREATE INDEX idx_tournaments_status ON tournaments (status);
CREATE INDEX idx_tournaments_type ON tournaments (type);
CREATE INDEX idx_tournaments_created_by ON tournaments (created_by);

CREATE TABLE participants (
    id UUID PRIMARY KEY,
    tournament_id UUID NOT NULL,
    name VARCHAR(160) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_participants_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments (id)
);

CREATE INDEX idx_participants_tournament ON participants (tournament_id);
CREATE INDEX idx_participants_active ON participants (active);

CREATE TABLE tournament_rounds (
    id UUID PRIMARY KEY,
    tournament_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    round_number INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL,
    opens_at TIMESTAMP WITH TIME ZONE,
    closes_at TIMESTAMP WITH TIME ZONE,
    results_published_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_rounds_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments (id),
    CONSTRAINT uk_round_tournament_number UNIQUE (tournament_id, round_number)
);

CREATE INDEX idx_rounds_tournament ON tournament_rounds (tournament_id);
CREATE INDEX idx_rounds_status ON tournament_rounds (status);

CREATE TABLE matches (
    id UUID PRIMARY KEY,
    round_id UUID NOT NULL,
    participant_a_id UUID NOT NULL,
    participant_b_id UUID NOT NULL,
    winner_id UUID,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_matches_round FOREIGN KEY (round_id) REFERENCES tournament_rounds (id),
    CONSTRAINT fk_matches_participant_a FOREIGN KEY (participant_a_id) REFERENCES participants (id),
    CONSTRAINT fk_matches_participant_b FOREIGN KEY (participant_b_id) REFERENCES participants (id),
    CONSTRAINT fk_matches_winner FOREIGN KEY (winner_id) REFERENCES participants (id),
    CONSTRAINT chk_matches_distinct_participants CHECK (participant_a_id <> participant_b_id)
);

CREATE INDEX idx_matches_round ON matches (round_id);
CREATE INDEX idx_matches_status ON matches (status);

CREATE TABLE votes (
    id UUID PRIMARY KEY,
    tournament_id UUID NOT NULL,
    round_id UUID NOT NULL,
    match_id UUID NOT NULL,
    voter_id UUID NOT NULL,
    selected_participant_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_votes_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments (id),
    CONSTRAINT fk_votes_round FOREIGN KEY (round_id) REFERENCES tournament_rounds (id),
    CONSTRAINT fk_votes_match FOREIGN KEY (match_id) REFERENCES matches (id),
    CONSTRAINT fk_votes_voter FOREIGN KEY (voter_id) REFERENCES app_users (id),
    CONSTRAINT fk_votes_selected_participant FOREIGN KEY (selected_participant_id) REFERENCES participants (id),
    CONSTRAINT uk_votes_match_voter UNIQUE (match_id, voter_id)
);

CREATE INDEX idx_votes_tournament ON votes (tournament_id);
CREATE INDEX idx_votes_round ON votes (round_id);
CREATE INDEX idx_votes_match ON votes (match_id);
CREATE INDEX idx_votes_voter ON votes (voter_id);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES app_users (id)
);

CREATE UNIQUE INDEX uk_refresh_tokens_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    user_id UUID,
    action VARCHAR(64) NOT NULL,
    entity_type VARCHAR(32) NOT NULL,
    tournament_id UUID,
    entity_id UUID,
    details_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES app_users (id),
    CONSTRAINT fk_audit_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments (id)
);

CREATE INDEX idx_audit_user ON audit_logs (user_id);
CREATE INDEX idx_audit_tournament_id ON audit_logs (tournament_id);
CREATE INDEX idx_audit_entity_type ON audit_logs (entity_type);
CREATE INDEX idx_audit_entity_id ON audit_logs (entity_id);
CREATE INDEX idx_audit_action ON audit_logs (action);
CREATE INDEX idx_audit_created_at ON audit_logs (created_at);

INSERT INTO roles (id, name, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', 'ORGANIZER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333333', 'VOTER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
