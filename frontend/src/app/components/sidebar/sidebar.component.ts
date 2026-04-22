import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
})
export class SidebarComponent {
  readonly nav = [
    { path: '/clientes',    label: 'Clientes' },
    { path: '/cuentas',     label: 'Cuentas' },
    { path: '/movimientos', label: 'Movimientos' },
    { path: '/reportes',    label: 'Reportes' },
  ];
}
