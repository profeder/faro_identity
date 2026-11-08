package it.faro.identity.user;

import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final JdbcTemplate jdbc;
    public DatabaseUserDetailsService(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return jdbc.query("select id,email,password_hash,enabled from identity_users where lower(email)=lower(?)", rs -> {
            if (!rs.next()) throw new UsernameNotFoundException("Utente non trovato");
            String id = rs.getString("id");
            List<String> authorities = new ArrayList<>();
            authorities.add("ROLE_USER");
            authorities.addAll(jdbc.query("""
                select 'ROLE_' || r.code from identity_user_global_roles g
                join identity_roles r on r.id=g.role_id where g.user_id=?::uuid
                """, (row, index) -> row.getString(1), id));
            return User.withUsername(rs.getString("email"))
                .password(rs.getString("password_hash"))
                .disabled(!rs.getBoolean("enabled"))
                .authorities(authorities.toArray(new String[0]))
                .build();
        }, username);
    }
}
