import request from "supertest";
import { describe, expect, it } from "vitest";
import { createApp } from "../src/app";
import { EmailDeliveryError, UpstreamError } from "../src/domain/errors";
import { FakeEmailSender } from "./helpers/fakeEmailSender";
import { FakeProvider } from "./helpers/fakeProvider";
import { makeProducts, makeTestApp, testConfig } from "./helpers/makeApp";

describe("catalog API", () => {
  it("returns the available categories", async () => {
    const app = makeTestApp().app;
    await request(app)
      .get("/categories")
      .expect(200, ["electronics", "clothing"]);
  });

  it("returns featured products in configured order and skips missing IDs", async () => {
    const products = makeProducts(17).filter((product) => product.id !== 5);
    const { app } = makeTestApp(products);
    const response = await request(app).get("/products/featured").expect(200);
    expect(response.body.map((product: { id: number }) => product.id)).toEqual([
      1, 9, 13, 17,
    ]);
    expect(response.body).toHaveLength(4);
  });

  it("returns all products from the provider using the API product shape", async () => {
    const products = makeProducts(8);
    const app = makeTestApp(products).app;
    const response = await request(app).get("/products/all").expect(200);
    expect(response.body).toHaveLength(products.length);
    expect(response.body.map((product: { id: number }) => product.id)).toEqual(
      products.map((product) => product.id),
    );
    expect(response.body[0]).toHaveProperty("imageUrl");
    expect(JSON.stringify(response.body)).not.toMatch(/"(?:image|rate)"\s*:/);
  });

  it("does not expose the removed category-products endpoint", async () => {
    const app = makeTestApp().app;
    await request(app).get("/categories/electronics/products").expect(404);
  });

  it("does not expose the removed search endpoint", async () => {
    const app = makeTestApp().app;
    await request(app).get("/products?q=shirt").expect(404);
  });

  it("returns a product, 404 for unknown IDs, and 400 for non-integer IDs", async () => {
    const app = makeTestApp().app;
    await request(app).get("/products/1").expect(200);
    await request(app).get("/products/9999").expect(404);
    await request(app).get("/products/abc").expect(400);
  });

  it("accepts a valid enquiry and sends exactly one email to sales with customer reply-to", async () => {
    const { app, emailSender } = makeTestApp();
    await request(app)
      .post("/enquiries")
      .send({
        productId: 1,
        name: "Asha",
        email: "asha@example.com",
        message: "Is this in stock?",
      })
      .expect(201, { status: "received" });
    expect(emailSender.messages).toHaveLength(1);
    expect(emailSender.messages[0]).toMatchObject({
      to: "sales@example.com",
      replyTo: "asha@example.com",
      subject: "New enquiry: Product 1 (#1)",
    });
  });

  it.each([
    { productId: 1, name: "Asha", email: "not-an-email", message: "Hello" },
    { productId: 1, email: "asha@example.com", message: "Hello" },
    { productId: 1, name: "Asha", email: "asha@example.com" },
    {
      productId: 1,
      name: "Asha",
      email: "asha@example.com",
      message: "x".repeat(2001),
    },
    {
      productId: 1,
      name: "Asha\r\nBcc: x@example.com",
      email: "asha@example.com",
      message: "Hello",
    },
    {
      productId: 1,
      name: "Asha",
      email: "asha@example.com\r\nBcc:x@example.com",
      message: "Hello",
    },
  ])("rejects invalid enquiry input without sending email", async (body) => {
    const { app, emailSender } = makeTestApp();
    await request(app).post("/enquiries").send(body).expect(400);
    expect(emailSender.messages).toHaveLength(0);
  });

  it("returns 404 for an enquiry about a missing product", async () => {
    const { app, emailSender } = makeTestApp();
    await request(app)
      .post("/enquiries")
      .send({
        productId: 9999,
        name: "Asha",
        email: "asha@example.com",
        message: "Hello",
      })
      .expect(404);
    expect(emailSender.messages).toHaveLength(0);
  });

  it("sends enquiry text as plain text without an HTML body", async () => {
    const { app, emailSender } = makeTestApp();
    await request(app)
      .post("/enquiries")
      .send({
        productId: 1,
        name: "Asha",
        email: "asha@example.com",
        message: "<script>alert(1)</script>",
      })
      .expect(201);
    expect(emailSender.messages[0]?.html).toBeUndefined();
    expect(emailSender.messages[0]?.text).toContain(
      "<script>alert(1)</script>",
    );
  });

  it("returns a generic 502 when email delivery fails", async () => {
    const { app, emailSender } = makeTestApp();
    emailSender.send = async () => {
      throw new EmailDeliveryError({
        cause: new Error("SMTP password or host detail"),
      });
    };

    const response = await request(app)
      .post("/enquiries")
      .send({
        productId: 1,
        name: "Asha",
        email: "asha@example.com",
        message: "Is this in stock?",
      })
      .expect(502);

    expect(response.body).toEqual({
      error: {
        code: "EMAIL_DELIVERY_FAILED",
        message: "Email delivery failed",
      },
    });
    expect(JSON.stringify(response.body)).not.toContain("SMTP password");
  });

  it("maps upstream failures to a generic 502 error body", async () => {
    const provider = new FakeProvider(makeProducts());
    provider.listProducts = async () => {
      throw new UpstreamError("sensitive upstream URL https://secret.invalid");
    };
    const app = createApp({
      provider,
      emailSender: new FakeEmailSender(),
      config: testConfig,
    });
    const response = await request(app).get("/products/featured").expect(502);
    expect(response.body).toEqual({
      error: {
        code: "UPSTREAM_ERROR",
        message: "Product provider is unavailable",
      },
    });
    expect(JSON.stringify(response.body)).not.toContain("secret.invalid");
  });

  it("limits the sixth enquiry request within the minute", async () => {
    const { app } = makeTestApp();
    const body = {
      productId: 1,
      name: "Asha",
      email: "asha@example.com",
      message: "Hello",
    };
    for (let index = 0; index < 5; index += 1) {
      await request(app).post("/enquiries").send(body).expect(201);
    }
    const response = await request(app)
      .post("/enquiries")
      .send(body)
      .expect(429);
    expect(response.body.error.code).toBe("RATE_LIMITED");
  });

  it("never returns upstream-only field names", async () => {
    const app = makeTestApp().app;
    const response = await request(app).get("/products/1").expect(200);
    expect(JSON.stringify(response.body)).not.toMatch(/"(?:image|rate)"\s*:/);
  });
});
