import type { RequestHandler } from "express";
import { NotFoundError } from "../domain/errors";

export const notFound: RequestHandler = (_request, _response, next) => {
  next(new NotFoundError("Route not found"));
};
