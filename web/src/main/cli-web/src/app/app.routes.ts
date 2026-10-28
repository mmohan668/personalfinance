import { Routes } from '@angular/router';
import { authGuard } from './auth-guard';

export const routes: Routes = [
  // Login is outside the application shell
  {
    path: 'login',
    loadComponent: () => import('./modules/login/login').then((m) => m.Login),
  },

  // Signup is outside the application shell
  {
    path: 'signup',
    loadComponent: () => import('./modules/signup/signup').then((m) => m.Signup),
  },

  // All authenticated pages share the application shell
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/layout').then((m) => m.Layout),
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full',
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./modules/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'allTransactions',
        loadComponent: () =>
          import('./modules/all-transactions/all-transactions').then((m) => m.AllTransactions),
      },
      {
        path: 'expenses',
        loadComponent: () => import('./modules/expenses/expenses').then((m) => m.Expenses),
      },
      {
        path: 'income',
        loadComponent: () => import('./modules/income/income').then((m) => m.Income),
      },
      {
        path: 'transfers',
        loadComponent: () => import('./modules/transfers/transfers').then((m) => m.Transfers),
      },
      {
        path: 'investments',
        loadComponent: () => import('./modules/investments/investments').then((m) => m.Investments),
      },
      {
        path: 'categories',
        loadComponent: () => import('./modules/categories/categories').then((m) => m.Categories),
      },
      {
        path: 'subcategories',
        loadComponent: () =>
          import('./modules/subcategories/subcategories').then((m) => m.Subcategories),
      },
      {
        path: 'system-config',
        loadComponent: () =>
          import('./modules/system-config/system-config').then((m) => m.SystemConfig),
      },
      {
        path: 'reference-objects',
        loadComponent: () =>
          import('./modules/reference-objects/reference-objects').then((m) => m.ReferenceObjects),
      },
      {
        path: 'reference-values',
        loadComponent: () =>
          import('./modules/reference-values/reference-values').then((m) => m.ReferenceValues),
      },
      {
        path: 'users',
        loadComponent: () => import('./modules/users/users').then((m) => m.Users),
      },
      {
        path: 'downloads',
        loadComponent: () => import('./modules/downloads/downloads').then((m) => m.Downloads),
      },
      {
        path: 'bulkupload',
        loadComponent: () => import('./modules/bulk-upload/bulk-upload').then((m) => m.BulkUpload),
      },
    ],
  },

  {
    path: '**',
    redirectTo: 'login',
  },
];
