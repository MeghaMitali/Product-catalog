import { Router } from "express";
import type { CatalogService } from "../services/catalogService";
import { asyncHandler } from "../middleware/validate";

export function categoriesRouter(catalog: CatalogService): Router {
  const router = Router();
  router.get(
    "/categories",
    asyncHandler(async (_request, response) => {
      response.json(await catalog.getCategories());
    }),
  );
  return router;
}
