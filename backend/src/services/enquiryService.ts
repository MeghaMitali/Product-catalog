import { NotFoundError } from "../domain/errors";
import type { EnquiryInput } from "../domain/enquiry";
import type { EmailSender } from "../ports/emailSender";
import type { ProductProvider } from "../ports/productProvider";

export class EnquiryService {
  constructor(
    private readonly provider: ProductProvider,
    private readonly emailSender: EmailSender,
    private readonly salesInbox: string,
  ) {}

  async submit(input: EnquiryInput): Promise<void> {
    const product = await this.provider.getProduct(input.productId);
    if (!product) throw new NotFoundError("Product not found");

    const text = [
      `Product: ${product.title} (#${product.id})`,
      `Name: ${input.name}`,
      `Email: ${input.email}`,
      `Phone: ${input.phone ?? "Not provided"}`,
      "",
      "Message:",
      input.message,
    ].join("\n");

    await this.emailSender.send({
      to: this.salesInbox,
      subject: `New enquiry: ${product.title} (#${product.id})`,
      replyTo: input.email,
      text,
    });
  }
}
