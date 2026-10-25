# Product Catalogue API Specification

Base URL for local development: `http://localhost:3000`. All responses are JSON except empty middleware responses where applicable. Unknown enquiry body fields are stripped.

For local email testing, open the Mailpit inbox at `http://localhost:8025/`. The API sends SMTP mail to Mailpit on port `1025` when run with the provided Docker Compose setup.

## Product

```json
{
  "id": 1,
  "title": "Product title",
  "description": "Product description",
  "price": 10.5,
  "currency": "USD",
  "category": "electronics",
  "imageUrl": "https://example.test/image.jpg",
  "rating": { "average": 4.2, "count": 120 }
}
```

## Endpoints

| Method and path | Success | Validation and errors |
| --- | --- | --- |
| `GET /health` | `200 { "status": "ok" }` | No upstream dependency. |
| `GET /products/all` | `200 Product[]` | All products in provider order; `502` on upstream failure. |
| `GET /products/featured` | `200 Product[]` | Uses configured five IDs and skips missing upstream products; `502` on upstream failure. |
| `GET /categories` | `200 string[]` | `502` on upstream failure. |
| `GET /products/:id` | `200 Product` | Positive integer ID; `400` invalid ID, `404` missing product, `502` upstream failure. |
| `POST /enquiries` | `201 { "status": "received" }` | Per-IP limit; `400`, `404`, `429`, or `502` as described below. |

## Enquiry request

```json
{
  "productId": 1,
  "name": "Asha",
  "email": "asha@example.com",
  "phone": "+1 (555) 010-0000",
  "message": "Is this in stock?"
}
```

`productId` is a positive integer; `name` is trimmed and 1–100 characters; `email` is trimmed, valid, at most 254 characters; optional `phone` is at most 30 characters and allows only digits, spaces, `+`, `-`, `(`, and `)`; `message` is trimmed and 1–2000 characters. Name and email reject CR/LF. Phone may be omitted. The email is plain text, goes to the configured sales inbox, and sets the customer email as `replyTo`. No enquiry is persisted.

## Error envelope

Every handled error has this shape:

```json
{ "error": { "code": "NOT_FOUND", "message": "Product not found" } }
```

Validation errors use `400`/`VALIDATION_ERROR`; missing resources use `404`/`NOT_FOUND`; product upstream failures use `502`/`UPSTREAM_ERROR`; email delivery failures use `502`/`EMAIL_DELIVERY_FAILED`; rate limits use `429`/`RATE_LIMITED`; unexpected failures use `500`/`INTERNAL_ERROR`. Stack traces, upstream URLs, SMTP details, credentials, and enquiry contents are not returned to clients.