import type { EmailMessage, EmailSender } from "../../src/ports/emailSender";

export class FakeEmailSender implements EmailSender {
  readonly messages: EmailMessage[] = [];

  async send(message: EmailMessage): Promise<void> {
    this.messages.push(message);
  }
}
