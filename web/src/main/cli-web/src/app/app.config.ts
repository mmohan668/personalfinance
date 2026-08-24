import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideClientHydration } from '@angular/platform-browser';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes), provideClientHydration(),
    providePrimeNG({
      license: 'eyJpZCI6ImU3OGJjZjc1LTk5NzctNGMyZS05ODc1LTRjMzhhMTI2Mzc5MCIsInByb2R1Y3QiOiJwcmltZXVpIiwidGllciI6ImNvbW11bml0eSIsInR5cGUiOiJkZXYiLCJpYXQiOjE3ODc0OTU1NTgsImV4cCI6MTgxOTAzMTU1OH0.Ju7gPGd-M-AywwqmkUkXk1nhyTQMP1PCgMalCT93lBVFFDbMilNjq97hl_F5i-aHtTE7QTL6loLDK907nD9gAg',
      theme: {
        preset: Aura,
      }
    })
  ]
};
