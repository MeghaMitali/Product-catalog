import pino from "pino";
import { NotFoundError } from "../domain/errors";
import type { Product } from "../domain/product";
import type { ProductProvider } from "../ports/productProvider";

const logger = pino();

export class CatalogService {
  constructor(
    private readonly provider: ProductProvider,
    private readonly featuredIds: number[],
  ) {}

  async getFeatured(): Promise<Product[]> {
    const products = await this.provider.listProducts();
    const byId = new Map(products.map((product) => [product.id, product]));
    return this.featuredIds.flatMap((id) => {
      const product = byId.get(id);
      if (product) return [product];
      logger.warn({ productId: id }, "Configured featured product is missing");
      return [];
    });
  }

  getCategories(): Promise<string[]> {
    return this.provider.listCategories();
  }

  getAllProducts(): Promise<Product[]> {
    return this.provider.listProducts();
  }

  async getProduct(id: number): Promise<Product> {
    const product = await this.provider.getProduct(id);
    if (!product) throw new NotFoundError("Product not found");
    return product;
  }
}
