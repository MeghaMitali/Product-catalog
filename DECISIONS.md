# Decisions and Growth Path

## Assumptions

- **Unknown upstream product:** an HTTP 404, empty body, or JSON `null` from the single-product endpoint is treated as missing. Other non-2xx responses are upstream failures (502).
- **Featured products:** the safe default is IDs `1,5,9,13,17`; unavailable configured products are skipped and logged as warnings.
- **Enquiries:** enquiries are sent once by email and are not persisted. Email is plain text, avoiding HTML rendering and escaping concerns.
- **Enquiry acknowledgement:** no customer confirmation email is sent. A successful 201 means the SMTP server accepted the sales email; enquiries are not durably stored or retried.
- **Production CORS:** `CORS_ORIGINS` must explicitly list allowed origins; wildcard is rejected.

## Cuts and operational constraints

- No database, authentication, enquiry persistence, retries, or paid service is included. These are not needed for the specified catalogue and local Mailpit run path.
- Cache contents are in-memory and per process; a restart clears them. The service is stateless and safe to run in a container, but cache entries are not shared across replicas.
- SMTP is the current email adapter. Production credentials belong in environment injection or a secrets manager.
- Compose expects `backend/.env` to exist. Copy `backend/.env.example` to that path before starting Compose; secret `.env` files are ignored by Git.

## Provider swaps

- **New product source:** implement `ProductProvider`, add its selection in `app.ts` and configuration, and run the provider contract tests. No service or route changes should be needed.
- **New email provider (for example, SES):** implement `EmailSender`, select it through `EMAIL_PROVIDER`, and inject deployment credentials through environment or a secrets manager. No service or route changes should be needed.
- **Deployment:** use the same multi-stage image on ECS Fargate, App Runner, or another container platform; supply runtime config through environment variables or a secrets manager. Keep the app stateless and expose `/health` for health checks.

## Next steps

1. Confirm the production client origin(s) and set `CORS_ORIGINS` at deployment.
2. Replace sample sales/sender addresses and configure a real SMTP-compatible provider.
3. If enquiry audit/history becomes a requirement, define retention and privacy rules before adding persistence.