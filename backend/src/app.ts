import cors from "cors";
import express, { type Express } from "express";
import helmet from "helmet";
import pino from "pino";
import pinoHttp from "pino-http";
import { FakeStoreProvider } from "./adapters/fakeStoreProvider";
import { SmtpEmailSender } from "./adapters/smtpEmailSender";
import type { AppConfig } from "./config/env";
import { loadConfig } from "./config/env";
import { CatalogService } from "./services/catalogService";
import { EnquiryService } from "./services/enquiryService";
import type { EmailSender } from "./ports/emailSender";
import type { ProductProvider } from "./ports/productProvider";
import { categoriesRouter } from "./routes/categories";
import { enquiriesRouter } from "./routes/enquiries";
import { healthRouter } from "./routes/health";
import { productsRouter } from "./routes/products";
import { errorHandler } from "./middleware/errorHandler";
import { notFound } from "./middleware/notFound";

export interface AppDependencies {
  provider?: ProductProvider;
  emailSender?: EmailSender;
  config?: AppConfig;
}

export function createApp(dependencies: AppDependencies = {}): Express {
  const config = dependencies.config ?? loadConfig();
  const logger = pino({ level: config.nodeEnv === "test" ? "silent" : "info" });
  const provider =
    dependencies.provider ??
    new FakeStoreProvider({
      baseUrl: config.fakeStoreBaseUrl,
      timeoutMs: config.upstreamTimeoutMs,
      cacheTtlSeconds: config.cacheTtlSeconds,
    });
  const emailSender =
    dependencies.emailSender ??
    new SmtpEmailSender({
      host: config.smtpHost,
      port: config.smtpPort,
      user: config.smtpUser,
      pass: config.smtpPass,
      from: config.emailFrom,
    });
  const catalog = new CatalogService(provider, config.featuredProductIds);
  const enquiry = new EnquiryService(
    provider,
    emailSender,
    config.salesInboxEmail,
  );
  const app = express();
  const allowedOrigins = config.corsOrigins;

  app.use(pinoHttp({ logger }));
  app.use(helmet());
  app.use(
    cors({
      origin: (origin, callback) => {
        callback(
          null,
          !origin ||
            allowedOrigins.includes("*") ||
            allowedOrigins.includes(origin),
        );
      },
    }),
  );
  app.use(express.json({ limit: "10kb" }));
  app.use(healthRouter);
  app.use(productsRouter(catalog));
  app.use(categoriesRouter(catalog));
  app.use(enquiriesRouter(enquiry, config.enquiryRateLimitPerMin));
  app.use(notFound);
  app.use(errorHandler);
  return app;
}
