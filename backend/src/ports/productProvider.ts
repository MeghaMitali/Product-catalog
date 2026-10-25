import type { Product } from "../domain/product";

export interface ProductProvider {
  listProducts(): Promise<Product[]>;
  listCategories(): Promise<string[]>;
  getProduct(id: number): Promise<Product | null>;
}
