import type { Product } from "../../src/domain/product";
import type { ProductProvider } from "../../src/ports/productProvider";

export class FakeProvider implements ProductProvider {
  constructor(
    public products: Product[],
    public categories: string[] = [
      ...new Set(products.map((product) => product.category)),
    ],
  ) {}

  async listProducts(): Promise<Product[]> {
    return this.products;
  }

  async listCategories(): Promise<string[]> {
    return this.categories;
  }

  async getProduct(id: number): Promise<Product | null> {
    return this.products.find((product) => product.id === id) ?? null;
  }
}
