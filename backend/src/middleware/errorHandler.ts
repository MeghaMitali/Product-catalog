import type { ErrorRequestHandler } from "express";
import pino from "pino";
import {
  EmailDeliveryError,
  NotFoundError,
  RateLimitError,
  UpstreamError,
  ValidationError,
} from "../domain/errors";

const logger = pino();

export const errorHandler: ErrorRequestHandler = (
  error: unknown,
  request,
  response,
  _next,
) => {
  if (error instanceof ValidationError) {
    response
      .status(400)
      .json({ error: { code: error.code, message: error.message } });
    return;
  }
  if (error instanceof NotFoundError) {
    response
      .status(404)
      .json({ error: { code: error.code, message: error.message } });
    return;
  }
  if (error instanceof UpstreamError || error instanceof EmailDeliveryError) {
    logger.error(
      { err: error, method: request.method, path: request.path },
      "External service request failed",
    );
    response.status(502).json({
      error: {
        code: error.code,
        message:
          error instanceof EmailDeliveryError
            ? "Email delivery failed"
            : "Product provider is unavailable",
      },
    });
    return;
  }
  if (error instanceof RateLimitError) {
    response
      .status(429)
      .json({ error: { code: error.code, message: error.message } });
    return;
  }

  const parserError = error as { status?: number; type?: string };
  if (parserError.status === 400 || parserError.type === "entity.too.large") {
    response.status(400).json({
      error: {
        code: "VALIDATION_ERROR",
        message: "Request body is invalid or too large",
      },
    });
    return;
  }

  logger.error(
    { err: error, method: request.method, path: request.path },
    "Unhandled request error",
  );
  response.status(500).json({
    error: { code: "INTERNAL_ERROR", message: "An internal error occurred" },
  });
};
