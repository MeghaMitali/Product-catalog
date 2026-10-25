import type { RequestHandler } from "express";
import type { ZodType, ZodTypeDef } from "zod";
import { ValidationError } from "../domain/errors";

export function validate<T, Input>(
  schema: ZodType<T, ZodTypeDef, Input>,
  source: "body" | "params" | "query",
): RequestHandler {
  return (request, response, next) => {
    const result = schema.safeParse(request[source]);
    if (!result.success) {
      next(new ValidationError("Request validation failed"));
      return;
    }
    response.locals.validated = result.data as T;
    next();
  };
}

export function asyncHandler(handler: RequestHandler): RequestHandler {
  return (request, response, next) => {
    Promise.resolve(handler(request, response, next)).catch(next);
  };
}
