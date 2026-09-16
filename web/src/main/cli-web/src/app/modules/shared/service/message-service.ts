import { Service } from '@angular/core';

@Service()
export class MessageService {
  private messages: any = {};

  async load(): Promise<void> {
    const response = await fetch('assets/i18n/messages.json');
    this.messages = await response.json();
  }

  get(key: string): string {
    return key.split('.').reduce((obj, part) => obj?.[part], this.messages) ?? key;
  }
}
