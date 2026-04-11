INSERT INTO app_users (id, username, email, password_hash, first_name, last_name, enabled, created_at, updated_at)
VALUES
    (
        '90000000-0000-0000-0000-000000000001',
        'admin.demo',
        'admin.demo@example.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi9HBqE6xvV2RgNQ7Y2Bk1HpslNE8u2',
        'Admin',
        'Demo',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '90000000-0000-0000-0000-000000000002',
        'organizer.demo',
        'organizer.demo@example.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi9HBqE6xvV2RgNQ7Y2Bk1HpslNE8u2',
        'Organizer',
        'Demo',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '90000000-0000-0000-0000-000000000003',
        'voter.demo',
        'voter.demo@example.com',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi9HBqE6xvV2RgNQ7Y2Bk1HpslNE8u2',
        'Voter',
        'Demo',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

INSERT INTO user_roles (user_id, role_id)
VALUES
    ('90000000-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111'),
    ('90000000-0000-0000-0000-000000000002', '22222222-2222-2222-2222-222222222222'),
    ('90000000-0000-0000-0000-000000000003', '33333333-3333-3333-3333-333333333333');

INSERT INTO tournaments (
    id,
    title,
    description,
    type,
    status,
    created_by,
    start_at,
    end_at,
    created_at,
    updated_at,
    access_mode,
    join_pin,
    qr_token
)
VALUES (
    '91000000-0000-0000-0000-000000000001',
    'Demo Mejores Cafes',
    'Torneo de prueba para validar rondas, matches, sesiones y votos.',
    'BRACKET',
    'ACTIVE',
    '90000000-0000-0000-0000-000000000002',
    CURRENT_TIMESTAMP,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'DISPLAY_NAME',
    '654321',
    'demo-tournament-qr-token'
);

INSERT INTO participants (id, tournament_id, name, description, image_url, active, created_at, updated_at)
VALUES
    (
        '92000000-0000-0000-0000-000000000001',
        '91000000-0000-0000-0000-000000000001',
        'Cafe Central',
        'Participante de prueba A.',
        'https://example.com/images/cafe-central.png',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '92000000-0000-0000-0000-000000000002',
        '91000000-0000-0000-0000-000000000001',
        'Cafe Norte',
        'Participante de prueba B.',
        'https://example.com/images/cafe-norte.png',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '92000000-0000-0000-0000-000000000003',
        '91000000-0000-0000-0000-000000000001',
        'Cafe Sur',
        'Participante de prueba C.',
        'https://example.com/images/cafe-sur.png',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '92000000-0000-0000-0000-000000000004',
        '91000000-0000-0000-0000-000000000001',
        'Cafe Oeste',
        'Participante de prueba D.',
        'https://example.com/images/cafe-oeste.png',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

INSERT INTO tournament_rounds (
    id,
    tournament_id,
    name,
    round_number,
    status,
    opens_at,
    closes_at,
    results_published_at,
    created_at,
    updated_at
)
VALUES (
    '93000000-0000-0000-0000-000000000001',
    '91000000-0000-0000-0000-000000000001',
    'Ronda 1',
    1,
    'OPEN',
    CURRENT_TIMESTAMP,
    NULL,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO matches (
    id,
    round_id,
    participant_a_id,
    participant_b_id,
    winner_id,
    status,
    created_at,
    updated_at
)
VALUES
    (
        '94000000-0000-0000-0000-000000000001',
        '93000000-0000-0000-0000-000000000001',
        '92000000-0000-0000-0000-000000000001',
        '92000000-0000-0000-0000-000000000002',
        NULL,
        'OPEN',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '94000000-0000-0000-0000-000000000002',
        '93000000-0000-0000-0000-000000000001',
        '92000000-0000-0000-0000-000000000003',
        '92000000-0000-0000-0000-000000000004',
        NULL,
        'OPEN',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

INSERT INTO tournament_join_sessions (
    id,
    tournament_id,
    user_id,
    display_name,
    session_token_hash,
    active,
    joined_at,
    last_seen_at,
    expires_at,
    created_at,
    updated_at
)
VALUES
    (
        '95000000-0000-0000-0000-000000000001',
        '91000000-0000-0000-0000-000000000001',
        '90000000-0000-0000-0000-000000000003',
        'Voter Demo',
        'test-session-token-hash-voter-demo-000000000000000000000000000000000001',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        '95000000-0000-0000-0000-000000000002',
        '91000000-0000-0000-0000-000000000001',
        NULL,
        'Invitado Demo',
        'test-session-token-hash-guest-demo-000000000000000000000000000000000001',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

INSERT INTO votes (
    id,
    tournament_id,
    round_id,
    match_id,
    voter_id,
    selected_participant_id,
    created_at,
    updated_at,
    join_session_id
)
VALUES
    (
        '96000000-0000-0000-0000-000000000001',
        '91000000-0000-0000-0000-000000000001',
        '93000000-0000-0000-0000-000000000001',
        '94000000-0000-0000-0000-000000000001',
        '90000000-0000-0000-0000-000000000003',
        '92000000-0000-0000-0000-000000000001',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '95000000-0000-0000-0000-000000000001'
    ),
    (
        '96000000-0000-0000-0000-000000000002',
        '91000000-0000-0000-0000-000000000001',
        '93000000-0000-0000-0000-000000000001',
        '94000000-0000-0000-0000-000000000002',
        NULL,
        '92000000-0000-0000-0000-000000000004',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '95000000-0000-0000-0000-000000000002'
    );
