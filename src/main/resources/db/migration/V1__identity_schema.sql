create table identity_users (
    id uuid primary key,
    email varchar(320) not null,
    password_hash varchar(255) not null,
    enabled boolean not null default true,
    created_at timestamptz not null default now()
);
create unique index uq_identity_users_email_lower on identity_users (lower(email));
create table identity_tenants (
    id uuid primary key,
    code varchar(80) not null unique,
    display_name varchar(200) not null,
    enabled boolean not null default true,
    created_at timestamptz not null default now()
);
create table identity_memberships (
    id uuid primary key,
    user_id uuid not null references identity_users(id),
    tenant_id uuid not null references identity_tenants(id),
    enabled boolean not null default true,
    unique(user_id, tenant_id)
);
create table identity_roles (
    id uuid primary key,
    code varchar(80) not null unique
);
create table identity_permissions (
    id uuid primary key,
    code varchar(120) not null unique
);
create table identity_role_permissions (
    role_id uuid not null references identity_roles(id),
    permission_id uuid not null references identity_permissions(id),
    primary key(role_id, permission_id)
);
create table identity_membership_roles (
    membership_id uuid not null references identity_memberships(id),
    role_id uuid not null references identity_roles(id),
    primary key(membership_id,role_id)
);
