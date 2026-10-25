# AI Usage Notes

## Implementation assistance

- The implementation followed the supplied backend build brief for the Node.js 20, strict TypeScript, Express, Zod, SMTP, Docker Compose, and Mailpit stack.
- The workspace initially contained only a short README, so the brief was used as the backend contract and new project documentation was added alongside it.
- A design choice was made to use plain-text enquiry email only, avoiding an HTML injection surface.
- The single-product upstream HTTP 404 behavior was ambiguous. This implementation treats it as a missing product and maps other non-2xx responses to an upstream error, as recorded in `DECISIONS.md`.
- Enquiry review aligned the email-delivery error code with the API contract (`EMAIL_DELIVERY_FAILED`) and added coverage for missing names, the email subject, and generic delivery-failure responses.

## Review checklist

- Confirm no Fake Store response shapes are used outside the upstream adapter.
- Confirm validation occurs before business actions and invalid enquiries do not send mail.
- Confirm client error bodies do not expose stack traces, upstream URLs, or enquiry content.
- Run `npm run typecheck`, `npm run lint`, and `npm test` before merging.