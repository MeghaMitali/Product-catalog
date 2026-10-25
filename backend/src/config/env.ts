import dotenv from "dotenv";
import { z } from "zod";

dotenv.config();

const featuredIdsSchema = z
  .string()
  .default("1,5,9,13,17")
  .transform((value) => value.split(",").map((part) => Number(part.trim())))
  .refine(
    (ids) =>
      ids.length === 5 && ids.every((id) => Number.isInteger(id) && id > 0),
    {
      message: "must contain exactly five positive integer IDs",
    },
  )
  .refine((ids) => new Set(ids).size === ids.length, {
    message: "must not contain duplicate IDs",
  });

const envSchema = z
  .object({
    PORT: z.coerce.number().int().positive().default(3000),
    NODE_ENV: z
      .enum(["development", "test", "production"])
      .default("development"),
    CORS_ORIGINS: z.string().default("*"),
    PRODUCT_PROVIDER: z.literal("fakestore").default("fakestore"),
    FAKESTORE_BASE_URL: z.string().url().default("https://fakestoreapi.com"),
    UPSTREAM_TIMEOUT_MS: z.coerce.number().int().positive().default(5000),
    CACHE_TTL_SECONDS: z.coerce.number().int().nonnegative().default(60),
    FEATURED_PRODUCT_IDS: featuredIdsSchema,
    EMAIL_PROVIDER: z.literal("smtp").default("smtp"),
    SMTP_HOST: z.string().min(1).default("localhost"),
    SMTP_PORT: z.coerce.number().int().positive().default(1025),
    SMTP_USER: z.string().default(""),
    SMTP_PASS: z.string().default(""),
    EMAIL_FROM: z.string().email().default("catalogue@example.com"),
    SALES_INBOX_EMAIL: z.string().email().default("sales@example.com"),
    ENQUIRY_RATE_LIMIT_PER_MIN: z.coerce.number().int().positive().default(5),
  })
  .superRefine((config, context) => {
    if (config.NODE_ENV === "production") {
      const origins = config.CORS_ORIGINS.split(",").map((origin) =>
        origin.trim(),
      );
      if (
        origins.length === 0 ||
        origins.some((origin) => origin.length === 0 || origin === "*")
      ) {
        context.addIssue({
          code: z.ZodIssueCode.custom,
          path: ["CORS_ORIGINS"],
          message:
            "must contain explicit origins and cannot include * in production",
        });
      }
    }
  });

export interface AppConfig {
  port: number;
  nodeEnv: "development" | "test" | "production";
  corsOrigins: string[];
  productProvider: "fakestore";
  fakeStoreBaseUrl: string;
  upstreamTimeoutMs: number;
  cacheTtlSeconds: number;
  featuredProductIds: number[];
  emailProvider: "smtp";
  smtpHost: string;
  smtpPort: number;
  smtpUser: string;
  smtpPass: string;
  emailFrom: string;
  salesInboxEmail: string;
  enquiryRateLimitPerMin: number;
}

export function parseEnv(raw: NodeJS.ProcessEnv = process.env): AppConfig {
  const result = envSchema.safeParse(raw);
  if (!result.success) {
    const details = result.error.issues
      .map(
        (issue) => `${issue.path.join(".") || "environment"}: ${issue.message}`,
      )
      .join("\n");
    throw new Error(`Invalid environment configuration:\n${details}`);
  }

  return {
    port: result.data.PORT,
    nodeEnv: result.data.NODE_ENV,
    corsOrigins: result.data.CORS_ORIGINS.split(",").map((origin) =>
      origin.trim(),
    ),
    productProvider: result.data.PRODUCT_PROVIDER,
    fakeStoreBaseUrl: result.data.FAKESTORE_BASE_URL.replace(/\/$/, ""),
    upstreamTimeoutMs: result.data.UPSTREAM_TIMEOUT_MS,
    cacheTtlSeconds: result.data.CACHE_TTL_SECONDS,
    featuredProductIds: result.data.FEATURED_PRODUCT_IDS,
    emailProvider: result.data.EMAIL_PROVIDER,
    smtpHost: result.data.SMTP_HOST,
    smtpPort: result.data.SMTP_PORT,
    smtpUser: result.data.SMTP_USER,
    smtpPass: result.data.SMTP_PASS,
    emailFrom: result.data.EMAIL_FROM,
    salesInboxEmail: result.data.SALES_INBOX_EMAIL,
    enquiryRateLimitPerMin: result.data.ENQUIRY_RATE_LIMIT_PER_MIN,
  };
}

export function loadConfig(): AppConfig {
  try {
    return parseEnv();
  } catch (error) {
    console.error(
      error instanceof Error
        ? error.message
        : "Invalid environment configuration",
    );
    process.exit(1);
  }
}
