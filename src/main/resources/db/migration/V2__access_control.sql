-- Separate global roles from tenant roles. V1 remains unchanged.
create table identity_user_global_roles (
 user_id uuid not null references identity_users(id) on delete cascade,
 role_id uuid not null references identity_roles(id),
 primary key(user_id,role_id)
);
create table identity_profiles (
 id uuid primary key,
 tenant_id uuid references identity_tenants(id),
 code varchar(80) not null,
 display_name varchar(160) not null,
 unique (id,tenant_id)
);
create unique index uq_identity_profile_global on identity_profiles(code) where tenant_id is null;
create unique index uq_identity_profile_tenant on identity_profiles(tenant_id,code) where tenant_id is not null;
create table identity_profile_roles (
 profile_id uuid not null references identity_profiles(id) on delete cascade,
 role_id uuid not null references identity_roles(id),
 primary key(profile_id,role_id)
);
create table identity_membership_profiles (
 membership_id uuid not null references identity_memberships(id) on delete cascade,
 profile_id uuid not null references identity_profiles(id) on delete cascade,
 primary key(membership_id,profile_id)
);
create table identity_modules (
 code varchar(80) primary key,
 label varchar(160) not null
);
create table identity_actions (
 code varchar(120) primary key,
 module_code varchar(80) not null references identity_modules(code),
 label varchar(160) not null,
 permission_id uuid not null unique references identity_permissions(id)
);
create table identity_menus (
 id uuid primary key,
 parent_id uuid references identity_menus(id),
 code varchar(80) not null unique,
 label varchar(160) not null,
 route varchar(250) not null,
 sort_order integer not null default 0,
 enabled boolean not null default true
);
create table identity_menu_permissions (
 menu_id uuid not null references identity_menus(id) on delete cascade,
 permission_id uuid not null references identity_permissions(id),
 primary key(menu_id,permission_id)
);
insert into identity_roles(id,code) values
 (gen_random_uuid(),'ROOT'),(gen_random_uuid(),'TENANT_ADMIN'),
 (gen_random_uuid(),'RESPONSABILE'),(gen_random_uuid(),'CAPOSQUADRA'),(gen_random_uuid(),'VOLONTARIO')
on conflict(code) do nothing;
insert into identity_modules(code,label) values
 ('dashboard','Dashboard'),('mission','Missioni'),('fleet','Mezzi'),
 ('users','Utenti'),('tenants','Tenant'),('access','Autorizzazioni')
on conflict(code) do nothing;
insert into identity_permissions(id,code) values
 (gen_random_uuid(),'dashboard:read'),(gen_random_uuid(),'mission:read'),
 (gen_random_uuid(),'mission:create'),(gen_random_uuid(),'fleet:read'),
 (gen_random_uuid(),'users:manage'),(gen_random_uuid(),'tenants:manage'),
 (gen_random_uuid(),'access:manage')
on conflict(code) do nothing;
insert into identity_actions(code,module_code,label,permission_id)
 select p.code,split_part(p.code,':',1),p.code,p.id from identity_permissions p
 where p.code in ('dashboard:read','mission:read','mission:create','fleet:read','users:manage','tenants:manage','access:manage')
on conflict(code) do nothing;
insert into identity_menus(id,code,label,route,sort_order) values
 (gen_random_uuid(),'dashboard','Dashboard','/dashboard',10),
 (gen_random_uuid(),'missions','Missioni','/missions',20),
 (gen_random_uuid(),'fleet','Mezzi','/fleet',30),
 (gen_random_uuid(),'users','Utenti','/admin/users',40),
 (gen_random_uuid(),'tenants','Tenant','/admin/tenants',50),
 (gen_random_uuid(),'access','Accessi','/admin/access',60)
on conflict(code) do nothing;
insert into identity_menu_permissions(menu_id,permission_id)
 select m.id,p.id from identity_menus m join identity_permissions p on p.code = case m.code
 when 'dashboard' then 'dashboard:read' when 'missions' then 'mission:read'
 when 'fleet' then 'fleet:read' when 'users' then 'users:manage'
 when 'tenants' then 'tenants:manage' when 'access' then 'access:manage' end
on conflict do nothing;
-- Starter roles are intentionally conservative; ROOT is enforced separately.
insert into identity_role_permissions(role_id,permission_id)
 select r.id,p.id from identity_roles r cross join identity_permissions p
 where (r.code='TENANT_ADMIN' and p.code in ('dashboard:read','mission:read','fleet:read','users:manage','access:manage'))
 or (r.code='RESPONSABILE' and p.code in ('dashboard:read','mission:read','mission:create','fleet:read'))
 or (r.code='CAPOSQUADRA' and p.code in ('dashboard:read','mission:read','fleet:read'))
 or (r.code='VOLONTARIO' and p.code in ('dashboard:read','mission:read'))
on conflict do nothing;
