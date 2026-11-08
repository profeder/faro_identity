package it.faro.identity.access;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AccessService {
    private final JdbcTemplate jdbc;
    public AccessService(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public UUID userId(String email) {
        List<UUID> rows=jdbc.query("select id from identity_users where lower(email)=lower(?)",
            (rs,n)->(UUID)rs.getObject(1),email);
        if(rows.isEmpty()) throw new org.springframework.security.access.AccessDeniedException("Utente sconosciuto");
        return rows.get(0);
    }
    public boolean root(UUID user) {
        Integer c=jdbc.queryForObject("""
            select count(*) from identity_user_global_roles g join identity_roles r on r.id=g.role_id
            where g.user_id=? and r.code='ROOT'
            """,Integer.class,user);
        return c!=null && c>0;
    }
    public boolean member(UUID user,UUID tenant) {
        Integer c=jdbc.queryForObject("""
            select count(*) from identity_memberships where user_id=? and tenant_id=? and enabled=true
            """,Integer.class,user,tenant);
        return c!=null && c>0;
    }
    public Set<String> permissions(UUID user,UUID tenant) {
        if(tenant!=null && !root(user) && !member(user,tenant))
            throw new org.springframework.security.access.AccessDeniedException("Tenant non consentito");
        if(root(user)) return new TreeSet<>(jdbc.query("select code from identity_permissions",(r,n)->r.getString(1)));
        if(tenant==null) return Set.of();
        return new TreeSet<>(jdbc.query("""
            select distinct p.code from identity_memberships m
            join identity_membership_roles mr on mr.membership_id=m.id
            join identity_role_permissions rp on rp.role_id=mr.role_id
            join identity_permissions p on p.id=rp.permission_id
            where m.user_id=? and m.tenant_id=? and m.enabled=true
            union
            select distinct p.code from identity_memberships m
            join identity_membership_profiles mp on mp.membership_id=m.id
            join identity_profiles pr on pr.id=mp.profile_id and (pr.tenant_id=m.tenant_id or pr.tenant_id is null)
            join identity_profile_roles r on r.profile_id=pr.id
            join identity_role_permissions rp on rp.role_id=r.role_id
            join identity_permissions p on p.id=rp.permission_id
            where m.user_id=? and m.tenant_id=? and m.enabled=true
            """,(r,n)->r.getString(1), user,tenant,user,tenant));
    }
    public List<Map<String,Object>> navigation(Set<String> permissions) {
        return jdbc.query("""
            select m.code,m.label,m.route,m.sort_order,p.code as required_permission
            from identity_menus m join identity_menu_permissions mp on mp.menu_id=m.id
            join identity_permissions p on p.id=mp.permission_id where m.enabled=true
            order by m.sort_order,m.code
            """,(r,n)->Map.<String,Object>of("code",r.getString(1),"label",r.getString(2),
                "route",r.getString(3),"sortOrder",r.getInt(4),"permission",r.getString(5)))
            .stream().filter(m->permissions.contains(m.get("permission"))).toList();
    }
}
