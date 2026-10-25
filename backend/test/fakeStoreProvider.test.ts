import { describe, expect, it, vi } from "vitest";
import { FakeStoreProvider } from "../src/adapters/fakeStoreProvider";
import { UpstreamError } from "../src/domain/errors";

const upstreamProduct = {
  id: 7,
  title: "Example item",
  price: 19.99,
  description: "A sample product",
  category: "electronics",
  image: "https://images.example/item.jpg",
  rating: { rate: 4.5, count: 12 },
};

function response(body: unknown, status = 200): Response {
  return new Response(body === undefined ? "" : JSON.stringify(body), {
    status,
  });
}

describe("FakeStoreProvider", () => {
  it("maps upstream fields to the API product shape", async () => {
    const fetcher = vi
      .fn<typeof fetch>()
      .mockResolvedValue(response(upstreamProduct));
    const provider = new FakeStoreProvider({
      baseUrl: "https://fakestoreapi.com",
      timeoutMs: 100,
      cacheTtlSeconds: 60,
      fetcher,
    });
    await expect(provider.getProduct(7)).resolves.toEqual({
      id: 7,
      title: "Example item",
      description: "A sample product",
      price: 19.99,
      currency: "USD",
      category: "electronics",
      imageUrl: "https://images.example/item.jpg",
      rating: { average: 4.5, count: 12 },
    });
  });

  it("maps timeout and network failures to UpstreamError", async () => {
    const fetcher = vi
      .fn<typeof fetch>()
      .mockRejectedValue(new Error("network failure"));
    const provider = new FakeStoreProvider({
      baseUrl: "https://fakestoreapi.com",
      timeoutMs: 10,
      cacheTtlSeconds: 0,
      fetcher,
    });
    await expect(provider.listProducts()).rejects.toBeInstanceOf(UpstreamError);
  });

  it("aborts a fetch that exceeds the configured timeout", async () => {
    const fetcher = vi.fn<typeof fetch>(
      (_input, init) =>
        new Promise((_resolve, reject) => {
          init?.signal?.addEventListener(
            "abort",
            () => reject(new Error("aborted")),
            { once: true },
          );
        }),
    );
    const provider = new FakeStoreProvider({
      baseUrl: "https://fakestoreapi.com",
      timeoutMs: 5,
      cacheTtlSeconds: 0,
      fetcher,
    });
    await expect(provider.listProducts()).rejects.toBeInstanceOf(UpstreamError);
  });

  it("maps non-2xx and malformed payloads to UpstreamError", async () => {
    const nonOk = new FakeStoreProvider({
      baseUrl: "https://fakestoreapi.com",
      timeoutMs: 100,
      cacheTtlSeconds: 0,
      fetcher: vi.fn<typeof fetch>().mockResolvedValue(response({}, 503)),
    });
    const malformed = new FakeStoreProvider({
      baseUrl: "https://fakestoreapi.com",
      timeoutMs: 100,
      cacheTtlSeconds: 0,
      fetcher: vi
        .fn<typeof fetch>()
        .mockResolvedValue(response({ ...upstreamProduct, rating: {} })),
    });
    await expect(nonOk.listProducts()).rejects.toBeInstanceOf(UpstreamError);
    await expect(malformed.getProduct(7)).rejects.toBeInstanceOf(UpstreamError);
  });

  it("maps invalid JSON to UpstreamError", async () => {
    const fetcher = vi
      .fn<typeof fetch>()
      .mockResolvedValue(new Response("{not-json", { status: 200 }));
    const provider = new FakeStoreProvider({
      baseUrl: "https://fakestoreapi.com",
      timeoutMs: 100,
      cacheTtlSeconds: 0,
      fetcher,
    });
    await expect(provider.listProducts()).rejects.toBeInstanceOf(UpstreamError);
  });

  it("caches a successful result and handles an unknown product 404 as null", async () => {
    const fetcher = vi
      .fn<typeof fetch>()
      .mockResolvedValueOnce(response([upstreamProduct]))
      .mockResolvedValueOnce(response(undefined, 404));
    const provider = new FakeStoreProvider({
      baseUrl: "https://fakestoreapi.com",
      timeoutMs: 100,
      cacheTtlSeconds: 60,
      fetcher,
    });
    await provider.listProducts();
    await provider.listProducts();
    expect(fetcher).toHaveBeenCalledTimes(1);
    await expect(provider.getProduct(9999)).resolves.toBeNull();
  });
});
