# Product Catalogue

A product catalogue API for an Android client. The API owns the client-facing contract, adapts Fake Store data to our product model, and sends product enquiries through SMTP. The Android app must call this API rather than Fake Store directly.

## Technology choices

| Concern | Choice | Reason |
| --- | --- | --- |
| Runtime and language | Node.js 20 LTS + strict TypeScript | Supported LTS runtime with compile-time domain and API contracts. |
| HTTP | Express | Small, familiar middleware-based API framework. |
| Validation | Zod | Runtime validation for environment, requests, and untrusted upstream data. |
| Email | Nodemailer over SMTP | Works with local Mailpit and can target an SMTP-compatible provider later. |
| Security | Helmet, CORS, express-rate-limit | Standard security headers, explicit browser-origin policy, and enquiry abuse protection. |
| Logging | pino + pino-http | Structured request and server-side error logs without logging enquiry bodies. |
| HTTP client | Built-in `fetch` + `AbortController` | No extra HTTP dependency; supports bounded upstream requests. |
| Tests | Vitest + Supertest | Fast TypeScript tests against the Express app and hand-written port fakes. |
| Lint and format | ESLint + Prettier | Consistent static checks and formatting. |
| Dev runner | tsx | Runs and watches TypeScript without a separate build step. |
| Container | Multi-stage Docker + Compose + Mailpit | Small non-root runtime image and reproducible local SMTP testing. |

`dotenv` is included only to load the documented local `.env` file for the `tsx watch` development command; deployed environments can provide variables directly.

## 10-minute local run

Requirements: Node.js 20+, npm, and Docker with the Compose plugin.

1. Copy `backend/.env.example` to `backend/.env`. This file is ignored by Git. Compose requires the copy; it does not automatically fall back to `.env.example`.
2. From the repository root, run `docker compose up --build`.
3. Check `http://localhost:3000/health`; the response is `{"status":"ok"}`.
4. Explore the API with the examples below. Enquiries are delivered to Mailpit at `http://localhost:8025`.
5. The Android emulator can reach the host API at `http://10.0.2.2:3000`.

Stop the stack with Ctrl+C. No database or persistent volume is required.

## Backend development

From `backend/`:

- `npm ci` installs locked dependencies.
- `npm run dev` starts the API with `tsx` watch mode. Copy `.env.example` to `.env` first if using local defaults.
- `npm run build` compiles to `dist/`.
- `npm start` runs the compiled server.
- `npm run typecheck && npm run lint && npm test` runs the required quality checks.

## Environment variables

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `3000` | HTTP listen port. |
| `NODE_ENV` | `development` | Runtime mode (`development`, `test`, or `production`). |
| `CORS_ORIGINS` | `*` | Comma-separated allowed origins; production requires explicit non-wildcard origins. |
| `PRODUCT_PROVIDER` | `fakestore` | Product adapter selector. |
| `FAKESTORE_BASE_URL` | `https://fakestoreapi.com` | Fake Store upstream base URL. |
| `UPSTREAM_TIMEOUT_MS` | `5000` | Maximum upstream request duration. |
| `CACHE_TTL_SECONDS` | `60` | In-memory upstream response cache duration; `0` disables caching. |
| `FEATURED_PRODUCT_IDS` | `1,5,9,13,17` | Exactly five unique positive IDs, in display order. |
| `EMAIL_PROVIDER` | `smtp` | Enquiry email adapter selector. |
| `SMTP_HOST` | `localhost` | SMTP host (`mailpit` in Compose). |
| `SMTP_PORT` | `1025` | SMTP port. |
| `SMTP_USER` | empty | Optional SMTP username; used with `SMTP_PASS`. |
| `SMTP_PASS` | empty | Optional SMTP password; never commit credentials. |
| `EMAIL_FROM` | `catalogue@example.com` | Sender address. |
| `SALES_INBOX_EMAIL` | `sales@example.com` | Enquiry recipient address. |
| `ENQUIRY_RATE_LIMIT_PER_MIN` | `5` | Maximum enquiry requests per IP per minute. |

`backend/.env.example` lists the complete safe-default configuration. Production secrets should be injected by the deployment platform or a secrets manager, not stored in Git.

## API quick reference

- `GET /health` — liveness check.
- `GET /products/all` — all products from the configured provider.
- `GET /products/featured` — configured featured products in configured order (missing upstream IDs are skipped).
- `GET /categories` — category names.
- `GET /products/:id` — one product.
- `POST /enquiries` — submits a product enquiry to the configured sales inbox.

All product responses use our domain shape; Fake Store fields such as `image` and `rating.rate` are mapped inside the adapter and are not part of the API contract. Errors use `{ "error": { "code": "...", "message": "..." } }`. Full request and response details are in [SPEC.md](SPEC.md).

Submit an enquiry as JSON with `productId`, `name`, `email`, and `message`; `phone` is optional. A `201` response means the SMTP server accepted the message for the sales inbox. Locally, view it in Mailpit at `http://localhost:8025`. Invalid input returns `400`, an unknown product `404`, rate limiting `429`, and delivery failure `502`. Enquiries are not persisted and no customer confirmation email is sent.

## Architecture and decisions

Routes depend on services, services depend on provider ports, and adapters implement those ports. The app accepts injected provider, email sender, and config dependencies for tests. This keeps Fake Store and SMTP replaceable without changing routes or business rules. See [DECISIONS.md](DECISIONS.md) for assumptions, cuts, deployment notes, and the growth path; see [AI_USAGE.md](AI_USAGE.md) for implementation-assistance notes.
# Product-catalog

This repo contains a Jetpack Compose product catalog prototype built around the requirements in `CLAUDE.md`.

## Android module

The app is organized under `android/` with the structure required for a single-module PoC:

- `android/app`: Compose app, ViewModels, UI screens, navigation, repository setup
- `ui/theme`: central look-and-feel definitions
- `ui/components`: reusable loading/error/empty states
- `ui/home`, `ui/search`, `ui/detail`, `ui/enquiry`: screen-level UI and state

## Local run setup

1. Install Android Studio and the Android SDK.
2. Open the `android` folder in Android Studio.
3. Launch the app with the Android emulator or device.

## Notes

- Network calls use `http://10.0.2.2:3000` for emulator debug builds.
- Cleartext traffic is allowed for debug builds only.
