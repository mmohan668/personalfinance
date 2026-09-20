import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { loadingInterceptor } from './modules/shared/interceptor/loading.interceptor';
import { MessageService } from './modules/shared/service/message-service';
import { errorInterceptor } from './modules/shared/interceptor/error-interceptor';
import { DateAdapter, MAT_DATE_FORMATS } from '@angular/material/core';
import { SystemDateAdapter } from './modules/shared/adapter/system-date-adapter';

export const APP_DATE_FORMATS = {
  parse: {
    dateInput: 'input',
  },
  display: {
    dateInput: 'input',
    monthYearLabel: 'MMM yyyy',
    dateA11yLabel: 'dd/MM/yyyy',
    monthYearA11yLabel: 'MMMM yyyy',
  },
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideAppInitializer(() => {
      return inject(MessageService).load();
    }),
    provideHttpClient(withInterceptors([loadingInterceptor, errorInterceptor])),
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    providePrimeNG({
      license:
        'eyJpZCI6ImU3OGJjZjc1LTk5NzctNGMyZS05ODc1LTRjMzhhMTI2Mzc5MCIsInByb2R1Y3QiOiJwcmltZXVpIiwidGllciI6ImNvbW11bml0eSIsInR5cGUiOiJkZXYiLCJpYXQiOjE3ODc0OTU1NTgsImV4cCI6MTgxOTAzMTU1OH0.Ju7gPGd-M-AywwqmkUkXk1nhyTQMP1PCgMalCT93lBVFFDbMilNjq97hl_F5i-aHtTE7QTL6loLDK907nD9gAg',
      theme: {
        preset: Aura,
      },
    }),
    {
      provide: DateAdapter,
      useClass: SystemDateAdapter,
    },
    {
      provide: MAT_DATE_FORMATS,
      useValue: APP_DATE_FORMATS,
    },
  ],
};
