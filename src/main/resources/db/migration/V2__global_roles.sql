CREATE TABLE identity_user_global_roles (
    user_id UUID NOT NULL
        REFERENCES identity_users(id),
    role_id UUID NOT NULL
        REFERENCES identity_roles(id),
    PRIMARY KEY (user_id, role_id)
);

INSERT INTO identity_roles (id, code)
VALUES (
    gen_random_uuid(),
    'ROOT'
)
ON CONFLICT (code) DO NOTHING;