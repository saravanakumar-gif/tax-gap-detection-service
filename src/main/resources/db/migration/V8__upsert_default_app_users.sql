INSERT INTO app_users (
    id,
    username,
    password,
    role,
    enabled
) VALUES
(
    gen_random_uuid(),
    'auditor',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'AUDITOR',
    true
),
(
    gen_random_uuid(),
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    true
)
ON CONFLICT (username) DO UPDATE
SET password = EXCLUDED.password,
    role = EXCLUDED.role,
    enabled = EXCLUDED.enabled;
