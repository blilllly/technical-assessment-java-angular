import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'clientes', pathMatch: 'full' },
  {
    path: 'clientes',
    loadComponent: () =>
      import('./components/clientes/clientes.component').then(m => m.ClientesComponent),
  },
  {
    path: 'cuentas',
    loadComponent: () =>
      import('./components/cuentas/cuentas.component').then(m => m.CuentasComponent),
  },
  {
    path: 'movimientos',
    loadComponent: () =>
      import('./components/movimientos/movimientos.component').then(m => m.MovimientosComponent),
  },
  {
    path: 'reportes',
    loadComponent: () =>
      import('./components/reportes/reportes.component').then(m => m.ReportesComponent),
  },
  { path: '**', redirectTo: 'clientes' },
];
