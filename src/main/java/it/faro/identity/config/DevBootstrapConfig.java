package it.faro.identity.config;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class DevBootstrapConfig {
    @Bean ApplicationRunner devBootstrap(JdbcTemplate jdbc, PasswordEncoder encoder,
            TransactionTemplate tx,
            @Value("${faro.dev.bootstrap-enabled:false}") boolean enabled,
            @Value("${FARO_DEV_ADMIN_EMAIL:}") String email,
            @Value("${FARO_DEV_ADMIN_PASSWORD:}") String password) {
        return args -> {
            if (!enabled) return;
            if (email.isBlank() || password.length() < 12)
                throw new IllegalStateException("Bootstrap: email obbligatoria e password di almeno 12 caratteri");
            tx.executeWithoutResult(status -> {
                // One-time bootstrap: serialize concurrent replicas on the PostgreSQL database.
                jdbc.queryForObject("select pg_advisory_xact_lock(7301721)", (rs, row) -> rs.getObject(1));
                Integer roots = jdbc.queryForObject("""
                    select count(*) from identity_user_global_roles gr
                    join identity_roles r on r.id=gr.role_id where r.code='ROOT'
                    """, Integer.class);
                if (roots != null && roots > 0) return;
                Integer existing = jdbc.queryForObject(
                    "select count(*) from identity_users where lower(email)=lower(?)", Integer.class, email);
                if (existing != null && existing > 0)
                    throw new IllegalStateException("L'utente bootstrap esiste gia': assegnazione ROOT rifiutata");
                UUID id = UUID.randomUUID();
                jdbc.update("insert into identity_users(id,email,password_hash,enabled) values(?,?,?,true)",
                    id, email, encoder.encode(password));
                jdbc.update("""
                    insert into identity_user_global_roles(user_id,role_id)
                    select ?,id from identity_roles where code='ROOT'
                    """, id);
            });
        };
    }
}
