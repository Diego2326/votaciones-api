ALTER TABLE tournaments ADD COLUMN access_mode VARCHAR(32) NOT NULL DEFAULT 'ANONYMOUS';
ALTER TABLE tournaments ADD COLUMN join_pin VARCHAR(12);
ALTER TABLE tournaments ADD COLUMN qr_token VARCHAR(128);

UPDATE tournaments
SET join_pin = RIGHT(REPLACE(CAST(id AS VARCHAR), '-', ''), 6),
    qr_token = REPLACE(CAST(id AS VARCHAR), '-', '')
WHERE join_pin IS NULL OR qr_token IS NULL;

ALTER TABLE tournaments ALTER COLUMN join_pin SET NOT NULL;
ALTER TABLE tournaments ALTER COLUMN qr_token SET NOT NULL;

CREATE UNIQUE INDEX uk_tournaments_join_pin ON tournaments (join_pin);
CREATE UNIQUE INDEX uk_tournaments_qr_token ON tournaments (qr_token);

CREATE TABLE tournament_join_sessions (
    id UUID PRIMARY KEY,
    tournament_id UUID NOT NULL,
    user_id UUID,
    display_name VARCHAR(120),
    session_token_hash VARCHAR(128) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_seen_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_join_sessions_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments (id),
    CONSTRAINT fk_join_sessions_user FOREIGN KEY (user_id) REFERENCES app_users (id)
);

CREATE UNIQUE INDEX uk_join_sessions_token_hash ON tournament_join_sessions (session_token_hash);
CREATE INDEX idx_join_sessions_tournament ON tournament_join_sessions (tournament_id);
CREATE INDEX idx_join_sessions_user ON tournament_join_sessions (user_id);
CREATE INDEX idx_join_sessions_active ON tournament_join_sessions (active);

ALTER TABLE votes
    ADD COLUMN join_session_id UUID;

UPDATE votes
SET join_session_id = id;

ALTER TABLE votes
    ALTER COLUMN voter_id DROP NOT NULL;

ALTER TABLE votes
    ADD CONSTRAINT fk_votes_join_session FOREIGN KEY (join_session_id) REFERENCES tournament_join_sessions (id);

CREATE INDEX idx_votes_join_session ON votes (join_session_id);

ALTER TABLE votes
    DROP CONSTRAINT uk_votes_match_voter;

ALTER TABLE votes
    ALTER COLUMN join_session_id SET NOT NULL;

ALTER TABLE votes
    ADD CONSTRAINT uk_votes_match_join_session UNIQUE (match_id, join_session_id);
