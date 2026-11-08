package it.faro.identity.access;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/identity")
public class AccessController {
    private final AccessService access;
    private final JdbcTemplate jdbc;
    public AccessController(AccessService access,JdbcTemplate jdbc){this.access=access;this.jdbc=jdbc;}
    public record TenantRequest(String code,String displayName) {}
    public record ProfileRequest(UUID tenantId,String code,String displayName) {}
    public record RoleAssignment(UUID membershipId,UUID roleId) {}
    private UUID current(Authentication auth){ return access.userId(auth.getName()); }
    private UUID tenant(String tenantId){return tenantId==null||tenantId.isBlank()?null:UUID.fromString(tenantId);}
    @GetMapping("/me")
    public Map<String,Object> me(Authentication auth){
        UUID id=current(auth);
        return Map.of("id",id,"email",auth.getName(),"root",access.root(id),
            "tenants",jdbc.query("""
                select t.id,t.code,t.display_name from identity_memberships m
                join identity_tenants t on t.id=m.tenant_id
                where m.user_id=? and m.enabled=true and t.enabled=true order by t.code
                """,(rs,n)->Map.of("id",rs.getObject(1),"code",rs.getString(2),"name",rs.getString(3)),id));
    }
    @GetMapping("/me/permissions")
    public Set<String> permissions(Authentication auth,@RequestParam(required=false) String tenantId){
        return access.permissions(current(auth),tenant(tenantId));
    }
    @GetMapping("/me/navigation")
    public List<Map<String,Object>> navigation(Authentication auth,@RequestParam(required=false) String tenantId){
        return access.navigation(access.permissions(current(auth),tenant(tenantId)));
    }
    @GetMapping("/admin/roles")
    public List<Map<String,Object>> roles(){return jdbc.query("select id,code from identity_roles order by code",
        (rs,n)->Map.of("id",rs.getObject(1),"code",rs.getString(2)));}
    @GetMapping("/admin/permissions")
    public List<Map<String,Object>> allPermissions(){return jdbc.query("select id,code from identity_permissions order by code",
        (rs,n)->Map.of("id",rs.getObject(1),"code",rs.getString(2)));}
    @GetMapping("/admin/tenants")
    public List<Map<String,Object>> tenants(){return jdbc.query("select id,code,display_name from identity_tenants order by code",
        (rs,n)->Map.of("id",rs.getObject(1),"code",rs.getString(2),"name",rs.getString(3)));}
    @PostMapping("/admin/tenants") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> createTenant(@RequestBody TenantRequest request){
        if(request.code()==null||!request.code().matches("[a-z][a-z0-9_-]{2,79}")||
            request.displayName()==null||request.displayName().isBlank()||request.displayName().length()>200)
            throw new IllegalArgumentException("Codice o nome tenant non valido");
        UUID id=UUID.randomUUID();
        jdbc.update("insert into identity_tenants(id,code,display_name) values(?,?,?)",
            id,request.code(),request.displayName());
        return Map.of("id",id,"code",request.code(),"name",request.displayName());
    }
    @PostMapping("/admin/profiles") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> createProfile(@RequestBody ProfileRequest request){
        if(request.code()==null||!request.code().matches("[A-Z][A-Z0-9_]{2,79}")||
            request.displayName()==null||request.displayName().isBlank()||request.displayName().length()>160)
            throw new IllegalArgumentException("Profilo non valido");
        UUID id=UUID.randomUUID();
        jdbc.update("insert into identity_profiles(id,tenant_id,code,display_name) values(?,?,?,?)",
            id,request.tenantId(),request.code(),request.displayName());
        return Map.of("id",id,"code",request.code());
    }
    @PostMapping("/admin/membership-roles") @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void assignRole(@RequestBody RoleAssignment req){
        // ROOT can only be global, never assigned as a tenant role.
        Integer allowed=jdbc.queryForObject("select count(*) from identity_roles where id=? and code<>'ROOT'",Integer.class,req.roleId());
        if(allowed==null||allowed!=1) throw new AccessDeniedException("Ruolo non assegnabile al tenant");
        jdbc.update("insert into identity_membership_roles(membership_id,role_id) values(?,?) on conflict do nothing",
            req.membershipId(),req.roleId());
    }
}
