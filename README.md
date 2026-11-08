# Faro Identity — milestone 1 (sviluppo)

Progetto Maven Java 21 / Spring Boot 4.1.2 / Spring Security Authorization Server / PostgreSQL 17 / Flyway.

## Avvio locale

1. `cp .env.example .env`, cambiare `DB_PASSWORD` (non committare `.env`).
2. `docker compose --env-file .env up -d`
3. Esportare le variabili `DB_PASSWORD`, `FARO_DEV_BOOTSTRAP=true`, `FARO_DEV_ADMIN_EMAIL`, `FARO_DEV_ADMIN_PASSWORD` (almeno 12 caratteri). Esempio: `set -a; . ./.env; set +a` per DB_PASSWORD.
4. `mvn spring-boot:run` (o importare come **Existing Maven Project** in Eclipse).
5. Verificare:
   - `curl http://localhost:9000/actuator/health`
   - `curl http://localhost:9000/.well-known/openid-configuration`
   - `curl http://localhost:9000/oauth2/jwks`
6. Visita browser: `http://localhost:9000/oauth2/authorize?response_type=code&client_id=faro-angular&redirect_uri=http%3A%2F%2Flocalhost%3A4200%2Fauth%2Fcallback&scope=openid%20profile&state=demo&code_challenge=...&code_challenge_method=S256`. Occorre un **vero challenge PKCE** calcolato dal verifier; non usare letteralmente `...`. Senza frontend la redirect URI può portare ad una pagina inesistente: il codice appare nella barra URL del browser.

## Stato e limiti

- Gli utenti e la struttura iniziale multi-tenant sono su PostgreSQL (Flyway V1).
- Il bootstrap crea un solo utente locale quando esplicitamente attivato; non crea ruoli o tenant.
- I client OAuth2, codici di autorizzazione, consensi e refresh token sono **IN MEMORIA**.
- La chiave RSA è **EFFIMERA**: al riavvio i JWT esistenti non sono più verificabili.
- Non utilizzare questo profilo in produzione né esporlo su Internet.
- I permessi tenant non vengono ancora inclusi nei JWT; API di amministrazione e revoca sessioni non implementate.
- La callback Flutter loopback è solo un esempio per testing su desktop; quella mobile richiede deep link/app link validato.

## Integrazione futura Gateway

Per un gateway Spring Security WebFlux configurare `spring.security.oauth2.resourceserver.jwt.issuer-uri` con l'issuer pubblico raggiungibile e un aud validator per l'API Faro. Prima di collegare i client va scelto un issuer stabile e il percorso pubblico NGINX (senza cambiare issuer tra richieste).

## Produzione (milestone successive)

Persistenza JDBC OAuth2 (registered clients, authorizations, consents), chiavi in KMS/secret gestito e rotazione, bootstrap disabilitato, audit, blocco tentativi login, MFA, CORS/reverse proxy, verifiche tenant per risorsa, session revocation e security test.
