import { Router } from "express";
import rateLimit from "express-rate-limit";
import { z } from "zod";
import { RateLimitError } from "../domain/errors";
import type { EnquiryInput } from "../domain/enquiry";
import { asyncHandler, validate } from "../middleware/validate";
import type { EnquiryService } from "../services/enquiryService";

const safeHeaderString = z
  .string()
  .refine((value) => !/[\r\n]/.test(value), "Must not contain line breaks")
  .transform((value) => value.trim());
const enquirySchema = z.object({
  productId: z.number().int().positive(),
  name: safeHeaderString.pipe(z.string().min(1).max(100)),
  email: safeHeaderString.pipe(z.string().email().max(254)),
  phone: z
    .string()
    .transform((value) => value.trim())
    .pipe(
      z
        .string()
        .max(30)
        .regex(/^[\d +()-]*$/),
    )
    .optional(),
  message: z
    .string()
    .transform((value) => value.trim())
    .pipe(z.string().min(1).max(2000)),
});

export function enquiriesRouter(
  service: EnquiryService,
  limitPerMinute: number,
): Router {
  const router = Router();
  const enquiryLimiter = rateLimit({
    windowMs: 60_000,
    limit: limitPerMinute,
    standardHeaders: true,
    legacyHeaders: false,
    handler: (_request, _response, next) => next(new RateLimitError()),
  });

  router.post(
    "/enquiries",
    enquiryLimiter,
    validate(enquirySchema, "body"),
    asyncHandler(async (_request, response) => {
      await service.submit(response.locals.validated as EnquiryInput);
      response.status(201).json({ status: "received" });
    }),
  );
  return router;
}
