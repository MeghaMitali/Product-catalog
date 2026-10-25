import { z } from "zod";
import { UpstreamError } from "../domain/errors";
import type { Product } from "../domain/product";
import type { ProductProvider } from "../ports/productProvider";
import { TtlCache } from "./ttlCache";

const fakeProductSchema = z.object({
  id: z.number().int().positive(),
  title: z.string(),
  price: z.number().finite(),
  description: z.string(),
  category: z.string(),
  image: z.string().url(),
  rating: z.object({
    rate: z.number().finite(),
    count: z.number().int().nonnegative(),
  }),
});
const fakeProductsSchema = z.array(fakeProductSchema);
const categoriesSchema = z.array(z.string());

type FakeProduct = z.infer<typeof fakeProductSchema>;

export interface FakeStoreProviderOptions {
  baseUrl: string;
  timeoutMs: number;
  cacheTtlSeconds: number;
  cache?: TtlCache;
  fetcher?: typeof fetch;
}

export class FakeStoreProvider implements ProductProvider {
  private readonly cache: TtlCache;
  private readonly fetcher: typeof fetch;

  constructor(private readonly options: FakeStoreProviderOptions) {
    this.cache = options.cache ?? new TtlCache();
    this.fetcher = options.fetcher ?? fetch;
  }

  listProducts(): Promise<Product[]> {
    return this.cache.getOrSet(
      "products",
      this.options.cacheTtlSeconds,
      async () => {
        const payload = await this.requestJson("/products", fakeProductsSchema);
        return payload.map(mapProduct);
      },
    );
  }

  listCategories(): Promise<string[]> {
    return this.cache.getOrSet("categories", this.options.cacheTtlSeconds, () =>
      this.requestJson("/products/categories", categoriesSchema),
    );
  }

  getProduct(id: number): Promise<Product | null> {
    return this.cache.getOrSet(
      `product:${id}`,
      this.options.cacheTtlSeconds,
      async () => {
        const path = `/products/${encodeURIComponent(String(id))}`;
        const result = await this.requestJson(path, fakeProductSchema, true);
        return result === null ? null : mapProduct(result);
      },
    );
  }

  private requestJson<T>(path: string, schema: z.ZodType<T>): Promise<T>;
  private requestJson<T>(
    path: string,
    schema: z.ZodType<T>,
    allowEmpty: true,
  ): Promise<T | null>;
  private async requestJson<T>(
    path: string,
    schema: z.ZodType<T>,
    allowEmpty = false,
  ): Promise<T | null> {
    const controller = new AbortController();
    const timeout = setTimeout(
      () => controller.abort(),
      this.options.timeoutMs,
    );
    try {
      const response = await this.fetcher(`${this.options.baseUrl}${path}`, {
        signal: controller.signal,
      });
      if (allowEmpty && response.status === 404) return null;
      if (!response.ok) throw new UpstreamError();

      const body = await response.text();
      if (allowEmpty && body.trim() === "") return null;
      let parsed: unknown;
      try {
        parsed = JSON.parse(body) as unknown;
      } catch (error) {
        throw new UpstreamError("Product provider returned invalid data", {
          cause: error,
        });
      }
      if (allowEmpty && parsed === null) return null;

      const validated = schema.safeParse(parsed);
      if (!validated.success)
        throw new UpstreamError("Product provider returned invalid data");
      return validated.data;
    } catch (error) {
      if (error instanceof UpstreamError) throw error;
      throw new UpstreamError("Product provider is unavailable", {
        cause: error,
      });
    } finally {
      clearTimeout(timeout);
    }
  }
}

function mapProduct(product: FakeProduct): Product {
  return {
    id: product.id,
    title: product.title,
    description: product.description,
    price: product.price,
    currency: "USD",
    category: product.category,
    imageUrl: product.image,
    rating: { average: product.rating.rate, count: product.rating.count },
  };
}
