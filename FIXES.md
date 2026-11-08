# Correzioni Eclipse / Spring Security 7

- Corretto artifactId dello starter OAuth2 Authorization Server.
- Aggiornato package OAuth2AuthorizationServerConfigurer a Spring Security 7.
- Aggiunto bean JwtDecoder per endpoint OIDC UserInfo.
- Gli avvisi XSD nel Language Server Eclipse sono indipendenti dalla sintassi del POM.

In Eclipse: Maven > Update Project (Force Update); Project > Clean.
