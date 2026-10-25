import { createApp } from "../../src/app";
import type { AppConfig } from "../../src/config/env";
import type { Product } from "../../src/domain/product";
import { FakeEmailSender } from "./fakeEmailSender";
import { FakeProvider } from "./fakeProvider";

export const testConfig: AppConfig = {
  port: 3000,
  nodeEnv: "test",
  corsOrigins: ["*"],
  productProvider: "fakestore",
  fakeStoreBaseUrl: "https://fakestoreapi.com",
  upstreamTimeoutMs: 5000,
  cacheTtlSeconds: 60,
  featuredProductIds: [1, 5, 9, 13, 17],
  emailProvider: "smtp",
  smtpHost: "localhost",
  smtpPort: 1025,
  smtpUser: "",
  smtpPass: "",
  emailFrom: "catalogue@example.com",
  salesInboxEmail: "sales@example.com",
  enquiryRateLimitPerMin: 5,
};

export function makeProducts(count = 6): Product[] {
  return Array.from({ length: count }, (_, index) => ({
    id: index + 1,
    title: `Product ${index + 1}`,
    description: `Description ${index + 1}`,
    price: index + 10,
    currency: "USD",
    category: index % 2 === 0 ? "electronics" : "clothing",
    imageUrl: `https://images.example/${index + 1}.jpg`,
    rating: { average: count - index, count: index + 2 },
  }));
}

export function makeTestApp(products = makeProducts()) {
  const provider = new FakeProvider(products, ["electronics", "clothing"]);
  const emailSender = new FakeEmailSender();
  const app = createApp({ provider, emailSender, config: testConfig });
  return { app, provider, emailSender };
}
