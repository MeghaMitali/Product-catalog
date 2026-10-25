import { Router } from "express";
import { z } from "zod";
import type { CatalogService } from "../services/catalogService";
import { asyncHandler, validate } from "../middleware/validate";

const idSchema = z.object({
  id: z
    .string()
    .regex(/^\d+$/)
    .transform(Number)
    .refine((id) => Number.isSafeInteger(id) && id > 0),
});

export function productsRouter(catalog: CatalogService): Router {
  const router = Router();

  router.get(
    "/products/featured",
    asyncHandler(async (_request, response) => {
      response.json(await catalog.getFeatured());
    }),
  );
  router.get(
    "/products/all",
    asyncHandler(async (_request, response) => {
      response.json(await catalog.getAllProducts());
    }),
  );
  router.get(
    "/products/:id",
    validate(idSchema, "params"),
    asyncHandler(async (_request, response) => {
      const { id } = response.locals.validated as z.infer<typeof idSchema>;
      response.json(await catalog.getProduct(id));
    }),
  );

  return router;
}
