import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CuentaService } from '../../services/cuenta.service';
import { ClienteService } from '../../services/cliente.service';
import { Cuenta, CuentaRequest } from '../../models/cuenta.model';
import { Cliente } from '../../models/cliente.model';

interface FormErrors {
  numeroCuenta?: string;
  tipoCuenta?: string;
  saldoInicial?: string;
  clienteId?: string;
}

@Component({
  selector: 'app-cuentas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cuentas.component.html',
  styleUrl: './cuentas.component.css',
})
export class CuentasComponent implements OnInit {
  private readonly cuentaService = inject(CuentaService);
  private readonly clienteService = inject(ClienteService);

  readonly cuentas = signal<Cuenta[]>([]);
  readonly clientes = signal<Cliente[]>([]);
  readonly loading = signal(false);
  readonly busqueda = signal('');
  readonly showModal = signal(false);
  readonly editando = signal<Cuenta | null>(null);
  readonly toast = signal<{ msg: string; tipo: string } | null>(null);

  readonly filtradas = computed(() => {
    const q = this.busqueda().toLowerCase();
    return this.cuentas().filter(
      c =>
        c.numeroCuenta.toLowerCase().includes(q) ||
        c.tipoCuenta.toLowerCase().includes(q) ||
        c.clienteNombre.toLowerCase().includes(q)
    );
  });

  form: CuentaRequest = this.emptyForm();
  errors: FormErrors = {};

  readonly tiposCuenta = ['Ahorro', 'Ahorros', 'Corriente'];

  ngOnInit(): void {
    this.cargar();
    this.clienteService.listar().subscribe({ next: data => this.clientes.set(data) });
  }

  cargar(): void {
    this.loading.set(true);
    this.cuentaService.listar().subscribe({
      next: data => { this.cuentas.set(data); this.loading.set(false); },
      error: () => { this.mostrarToast('Error al cargar cuentas', 'error'); this.loading.set(false); },
    });
  }

  abrirNuevo(): void {
    this.editando.set(null);
    this.form = this.emptyForm();
    this.errors = {};
    this.showModal.set(true);
  }

  abrirEditar(cuenta: Cuenta): void {
    this.editando.set(cuenta);
    this.form = {
      numeroCuenta: cuenta.numeroCuenta,
      tipoCuenta: cuenta.tipoCuenta,
      saldoInicial: cuenta.saldoInicial,
      estado: cuenta.estado,
      clienteId: cuenta.clienteId,
    };
    this.errors = {};
    this.showModal.set(true);
  }

  guardar(): void {
    if (!this.validar()) return;
    const editando = this.editando();
    const op = editando
      ? this.cuentaService.actualizar(editando.id, this.form)
      : this.cuentaService.crear(this.form);

    op.subscribe({
      next: () => {
        this.mostrarToast(editando ? 'Cuenta actualizada' : 'Cuenta creada', 'success');
        this.showModal.set(false);
        this.cargar();
      },
      error: (err: any) => this.mostrarToast(err.error?.message ?? 'Error al guardar', 'error'),
    });
  }

  eliminar(cuenta: Cuenta): void {
    if (!confirm(`¿Eliminar cuenta ${cuenta.numeroCuenta}?`)) return;
    this.cuentaService.eliminar(cuenta.id).subscribe({
      next: () => { this.mostrarToast('Cuenta eliminada', 'success'); this.cargar(); },
      error: (err: any) => this.mostrarToast(err.error?.message ?? 'Error al eliminar', 'error'),
    });
  }

  onBusqueda(event: Event): void {
    this.busqueda.set((event.target as HTMLInputElement).value);
  }

  private validar(): boolean {
    this.errors = {};
    if (!this.form.numeroCuenta?.trim()) this.errors.numeroCuenta = 'El número de cuenta es obligatorio';
    if (!this.form.tipoCuenta?.trim()) this.errors.tipoCuenta = 'El tipo de cuenta es obligatorio';
    if (this.form.saldoInicial == null || this.form.saldoInicial < 0)
      this.errors.saldoInicial = 'El saldo inicial no puede ser negativo';
    if (!this.form.clienteId) this.errors.clienteId = 'Debe seleccionar un cliente';
    return Object.keys(this.errors).length === 0;
  }

  private mostrarToast(msg: string, tipo: string): void {
    this.toast.set({ msg, tipo });
    setTimeout(() => this.toast.set(null), 3500);
  }

  private emptyForm(): CuentaRequest {
    return { numeroCuenta: '', tipoCuenta: '', saldoInicial: 0, estado: true, clienteId: 0 };
  }
}
