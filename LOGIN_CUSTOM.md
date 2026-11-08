# Login in stile Angular

La pagina di login usa Thymeleaf (`src/main/resources/templates/login.html`) e CSS locale (`static/css/login.css`).

Imposta `FARO_BRAND_LOGO_URL` all'URL HTTPS accessibile dal browser del logo originale su MinIO. Non inserire credenziali, presigned URL con scadenza breve o endpoint interni Kubernetes.
Imposta facoltativamente `FARO_ANDROID_DOWNLOAD_URL` alla pagina HTTPS pubblica per scaricare l'app Android di test.

Esempio:
```
FARO_BRAND_LOGO_URL=https://<host-pubblico>/<bucket>/<logo>
FARO_ANDROID_DOWNLOAD_URL=https://<host-pubblico>/<percorso-download>
```

Il form usa POST `/login`, username/password e token CSRF di Spring Security. Per l'accesso OAuth2 il redirect richiesto resta gestito dal server.

Progetto non compilato nel runtime di generazione: verificare con `mvn clean test`.
