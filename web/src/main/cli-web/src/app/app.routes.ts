import { Routes } from '@angular/router';

export const routes: Routes = [
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
];
