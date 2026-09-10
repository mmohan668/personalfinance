import {
  mergeApplicationConfig,
  ApplicationConfig,
  provideAppInitializer,
  inject,
} from '@angular/core';
import { provideServerRendering, withRoutes } from '@angular/ssr';
import { appConfig } from './app.config';
import { serverRoutes } from './app.routes.server';
import { HttpClient } from '@angular/common/http';
import { AppConfig, AppConfigService } from './modules/shared/service/app-config-service';
import { firstValueFrom } from 'rxjs';

const serverConfig: ApplicationConfig = {
  providers: [
    provideServerRendering(withRoutes(serverRoutes)),
    provideAppInitializer(() => {
      const http = inject(HttpClient);
      const configService = inject(AppConfigService);
      return firstValueFrom(http.get<AppConfig>('/api/config')).then((config) => {
        configService.setConfig(config);
      });
    }),
  ],
};

export const config = mergeApplicationConfig(appConfig, serverConfig);
