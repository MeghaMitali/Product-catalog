import nodemailer from "nodemailer";
import { EmailDeliveryError } from "../domain/errors";
import type { EmailMessage, EmailSender } from "../ports/emailSender";

export interface SmtpEmailSenderOptions {
  host: string;
  port: number;
  user: string;
  pass: string;
  from: string;
}

export class SmtpEmailSender implements EmailSender {
  private readonly transport: nodemailer.Transporter;

  constructor(private readonly options: SmtpEmailSenderOptions) {
    this.transport = nodemailer.createTransport({
      host: options.host,
      port: options.port,
      secure: false,
      ...(options.user && options.pass
        ? { auth: { user: options.user, pass: options.pass } }
        : {}),
    });
  }

  async send(message: EmailMessage): Promise<void> {
    try {
      await this.transport.sendMail({
        from: this.options.from,
        to: message.to,
        subject: message.subject,
        text: message.text,
        ...(message.html === undefined ? {} : { html: message.html }),
        ...(message.replyTo === undefined ? {} : { replyTo: message.replyTo }),
      });
    } catch (error) {
      throw new EmailDeliveryError({ cause: error });
    }
  }
}
