export interface EmailMessage {
  to: string;
  subject: string;
  text: string;
  html?: string;
  replyTo?: string;
}

export interface EmailSender {
  send(message: EmailMessage): Promise<void>;
}
