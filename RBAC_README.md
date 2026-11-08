# Faro Identity — RBAC multi-tenant (fase 1)

## Prerequisiti
Java 21, PostgreSQL, Maven. Usa il database e le credenziali di sviluppo già configurati in `application.yml`.

## Bootstrap ROOT
Nella Run Configuration di Eclipse → Environment:
```
FARO_DEV_BOOTSTRAP=true
FARO_DEV_ADMIN_EMAIL=admin@example.test
FARO_DEV_ADMIN_PASSWORD=<password lunga e unica, almeno 12 caratteri>
```
Flyway applica V2. Al primo avvio un account nuovo viene creato con `ROLE_ROOT`; il bootstrap rifiuta di promuovere un account già esistente. Se `admin@example.test` era stato creato prima, non verrà elevato: per la prova usare un **nuovo indirizzo** e solo con database di sviluppo controllato. Disabilita subito bootstrap dopo la creazione.

Non usare questo bootstrap di sviluppo in produzione. Non c'è ancora MFA, né rotazione delle chiavi JWT / autorizzazioni persistenti.

## API su localhost:9000
Le API usano **sessione Spring Security e cookie JSESSIONID** in questo stadio. In un browser già autenticato apri `/api/identity/me`; per le POST serve il token CSRF di Spring Security. Non sono ancora configurate come OAuth2 Resource Server e **non** accettano un Bearer JWT al posto della sessione. Non inoltrarle attraverso Gateway come fossero già API JWT.

- GET `/api/identity/me`
- GET `/api/identity/me/permissions?tenantId=<uuid>`
- GET `/api/identity/me/navigation?tenantId=<uuid>`
- GET `/api/identity/admin/roles` ROOT
- GET `/api/identity/admin/permissions` ROOT
- GET `/api/identity/admin/tenants` ROOT
- POST `/api/identity/admin/tenants` ROOT JSON `{"code":"associazione-demo","displayName":"Associazione Demo"}`
- POST `/api/identity/admin/profiles` ROOT JSON `{"tenantId":"<uuid>","code":"COORDINATORE","displayName":"Coordinatore"}`
- POST `/api/identity/admin/membership-roles` ROOT JSON `{"membershipId":"<uuid>","roleId":"<uuid>"}`

La UI Angular di amministrazione, CRUD completi, protezione delle API con JWT, audit, autorizzazioni nel token e controlli server-side delle risorse sono da implementare nella fase successiva. La sola visibilità del menu non fornisce sicurezza.

## Test manuale
1. `mvn clean test` (progetto non compilato nell'ambiente di generazione)
2. Avvia Identity, login in `/login` con il nuovo ROOT.
3. Apri `/api/identity/me` e verifica `root: true`.
4. Apri `/api/identity/me/navigation`, deve contenere le voci previste.
5. GET `/api/identity/admin/tenants` da una sessione non ROOT deve produrre 403.
6. Con un client HTTP che gestisce cookie e CSRF, prova la POST di creazione tenant.

### Note
- V1 non viene modificata.
- Un ROOT ha accesso all'elenco dei permessi, ma i microservizi dovranno comunque applicare controlli autorizzativi propri.
- Endpoint destinati solo a una prima fase amministrativa; non esporli pubblicamente finché non siano completati hardening e audit.
