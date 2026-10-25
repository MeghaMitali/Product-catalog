export class ValidationError extends Error {
  readonly code = "VALIDATION_ERROR";

  constructor(message = "Request validation failed") {
    super(message);
    this.name = "ValidationError";
  }
}

export class NotFoundError extends Error {
  readonly code = "NOT_FOUND";

  constructor(message = "Resource not found") {
    super(message);
    this.name = "NotFoundError";
  }
}

export class UpstreamError extends Error {
  readonly code = "UPSTREAM_ERROR";

  constructor(
    message = "Product provider is unavailable",
    options?: ErrorOptions,
  ) {
    super(message, options);
    this.name = "UpstreamError";
  }
}

export class EmailDeliveryError extends Error {
  readonly code = "EMAIL_DELIVERY_FAILED";

  constructor(options?: ErrorOptions) {
    super("Email delivery failed", options);
    this.name = "EmailDeliveryError";
  }
}

export class RateLimitError extends Error {
  readonly code = "RATE_LIMITED";

  constructor() {
    super("Too many requests");
    this.name = "RateLimitError";
  }
}
