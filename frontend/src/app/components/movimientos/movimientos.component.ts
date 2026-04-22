import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MovimientoService } from '../../services/movimiento.service';
import { CuentaService } from '../../services/cuenta.service';
import { Movimiento, MovimientoRequest } from '../../models/movimiento.model';
import { Cuenta } from '../../models/cuenta.model';

interface FormErrors {
  tipoMovimiento?: string;
  valor?: string;
  cuentaId?: string;
}

@Component({
  selector: 'app-movimientos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './movimientos.component.html',
  styleUrl: './movimientos.component.css',
})
export class MovimientosComponent implements OnInit {
  private readonly movimientoService = inject(MovimientoService);
  private readonly cuentaService = inject(CuentaService);

  readonly movimientos = signal<Movimiento[]>([]);
  readonly cuentas = signal<Cuenta[]>([]);
  readonly loading = signal(false);
  readonly busqueda = signal('');
  readonly showModal = signal(false);
  readonly editando = signal<Movimiento | null>(null);
  readonly toast = signal<{ msg: string; tipo: string } | null>(null);

  readonly filtrados = computed(() => {
    const q = this.busqueda().toLowerCase();
    return this.movimientos().filter(
      m =>
        m.numeroCuenta.toLowerCase().includes(q) ||
        m.tipoMovimiento.toLowerCase().includes(q)
    );
  });

  form: MovimientoRequest = this.emptyForm();
  errors: FormErrors = {};

  readonly tiposMovimiento = ['Depósito', 'Retiro'];

  ngOnInit(): void {
    this.cargar();
    this.cuentaService.listar().subscribe({ next: data => this.cuentas.set(data) });
  }

  cargar(): void {
    this.loading.set(true);
    this.movimientoService.listar().subscribe({
      next: data => { this.movimientos.set(data); this.loading.set(false); },
      error: () => { this.mostrarToast('Error al cargar movimientos', 'error'); this.loading.set(false); },
    });
  }

  abrirNuevo(): void {
    this.editando.set(null);
    this.form = this.emptyForm();
    this.errors = {};
    this.showModal.set(true);
  }

  abrirEditar(mov: Movimiento): void {
    this.editando.set(mov);
    this.form = {
      fecha: mov.fecha,
      tipoMovimiento: mov.tipoMovimiento,
      valor: Math.abs(mov.valor),
      cuentaId: mov.cuentaId,
    };
    this.errors = {};
    this.showModal.set(true);
  }

  guardar(): void {
    if (!this.validar()) return;
    const editando = this.editando();
    const op = editando
      ? this.movimientoService.actualizar(editando.id, this.form)
      : this.movimientoService.crear(this.form);

    op.subscribe({
      next: () => {
        this.mostrarToast(editando ? 'Movimiento actualizado' : 'Movimiento registrado', 'success');
        this.showModal.set(false);
        this.cargar();
      },
      error: (err: any) => this.mostrarToast(err.error?.message ?? 'Error al guardar', 'error'),
    });
  }

  eliminar(mov: Movimiento): void {
    if (!confirm(`¿Eliminar movimiento #${mov.id}?`)) return;
    this.movimientoService.eliminar(mov.id).subscribe({
      next: () => { this.mostrarToast('Movimiento eliminado', 'success'); this.cargar(); },
      error: (err: any) => this.mostrarToast(err.error?.message ?? 'Error al eliminar', 'error'),
    });
  }

  onBusqueda(event: Event): void {
    this.busqueda.set((event.target as HTMLInputElement).value);
  }

  esDebito(valor: number): boolean {
    return valor < 0;
  }

  private validar(): boolean {
    this.errors = {};
    if (!this.form.tipoMovimiento) this.errors.tipoMovimiento = 'El tipo de movimiento es obligatorio';
    if (!this.form.valor || this.form.valor <= 0) this.errors.valor = 'El valor debe ser mayor a 0';
    if (!this.form.cuentaId) this.errors.cuentaId = 'Debe seleccionar una cuenta';
    return Object.keys(this.errors).length === 0;
  }

  private mostrarToast(msg: string, tipo: string): void {
    this.toast.set({ msg, tipo });
    setTimeout(() => this.toast.set(null), 3500);
  }

  private emptyForm(): MovimientoRequest {
    return { tipoMovimiento: '', valor: 0, cuentaId: 0 };
  }
}
