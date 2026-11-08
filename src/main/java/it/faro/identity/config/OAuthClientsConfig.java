package it.faro.identity.config;

import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

@Configuration
public class OAuthClientsConfig {
    @Bean RegisteredClientRepository registeredClientRepository() {
        RegisteredClient angular = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("faro-angular")
            .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri("http://localhost:4200/auth/callback")
            .scope(OidcScopes.OPENID).scope(OidcScopes.PROFILE).scope("faro.read")
            .clientSettings(ClientSettings.builder().requireProofKey(true).requireAuthorizationConsent(false).build())
            .build();
        RegisteredClient flutter = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("faro-flutter")
            .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri("http://127.0.0.1:8765/callback") // callback loopback di test; sostituire con URI app verificato
            .scope(OidcScopes.OPENID).scope(OidcScopes.PROFILE).scope("faro.read")
            .clientSettings(ClientSettings.builder().requireProofKey(true).requireAuthorizationConsent(false).build())
            .build();
        return new InMemoryRegisteredClientRepository(angular, flutter);
    }
}
