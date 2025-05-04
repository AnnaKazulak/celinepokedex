declare module 'vuetify' {
  import { ComponentPublicInstance } from 'vue';

  export interface VForm extends ComponentPublicInstance {
    validate(): Promise<{ valid: boolean }>;
    reset(): void;
    resetValidation(): void;
  }
}