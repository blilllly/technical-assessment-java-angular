import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClienteService } from '../../services/cliente.service';
import { Cliente, ClienteRequest } from '../../models/cliente.model';

interface FormErrors {
  nombre?: string;
  identificacion?: string;
  contrasena?: string;
}

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './clientes.component.html',
  styleUrl: './clientes.component.css',
})
export class ClientesComponent implements OnInit {
  private readonly clienteService = inject(ClienteService);

  readonly clientes = signal<Cliente[]>([]);
  readonly loading = signal(false);
  readonly busqueda = signal('');
  readonly showModal = signal(false);
  readonly editando = signal<Cliente | null>(null);
  readonly toast = signal<{ msg: string; tipo: string } | null>(null);

  readonly filtrados = computed(() => {
    const q = this.busqueda().toLowerCase();
    return this.clientes().filter(
      c =>
        c.nombre.toLowerCase().includes(q) ||
        c.identificacion.toLowerCase().includes(q) ||
        (c.telefono ?? '').includes(q)
    );
  });

  form: ClienteRequest = this.emptyForm();
  errors: FormErrors = {};

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.clienteService.listar().subscribe({
      next: data => { this.clientes.set(data); this.loading.set(false); },
      error: () => { this.mostrarToast('Error al cargar clientes', 'error'); this.loading.set(false); },
    });
  }

  abrirNuevo(): void {
    this.editando.set(null);
    this.form = this.emptyForm();
    this.errors = {};
    this.showModal.set(true);
  }

  abrirEditar(cliente: Cliente): void {
    this.editando.set(cliente);
    this.form = {
      nombre: cliente.nombre,
      genero: cliente.genero ?? '',
      edad: cliente.edad,
      identificacion: cliente.identificacion,
      direccion: cliente.direccion ?? '',
      telefono: cliente.telefono ?? '',
      contrasena: '',
      estado: cliente.estado,
    };
    this.errors = {};
    this.showModal.set(true);
  }

  guardar(): void {
    if (!this.validar()) return;
    const editando = this.editando();
    const op = editando
      ? this.clienteService.actualizar(editando.clienteId, this.form)
      : this.clienteService.crear(this.form);

    op.subscribe({
      next: () => {
        this.mostrarToast(editando ? 'Cliente actualizado' : 'Cliente creado', 'success');
        this.showModal.set(false);
        this.cargar();
      },
      error: (err: any) => this.mostrarToast(err.error?.message ?? 'Error al guardar', 'error'),
    });
  }

  eliminar(cliente: Cliente): void {
    if (!confirm(`¿Eliminar cliente "${cliente.nombre}"?`)) return;
    this.clienteService.eliminar(cliente.clienteId).subscribe({
      next: () => { this.mostrarToast('Cliente eliminado', 'success'); this.cargar(); },
      error: (err: any) => this.mostrarToast(err.error?.message ?? 'Error al eliminar', 'error'),
    });
  }

  onBusqueda(event: Event): void {
    this.busqueda.set((event.target as HTMLInputElement).value);
  }

  private validar(): boolean {
    this.errors = {};
    if (!this.form.nombre?.trim()) this.errors.nombre = 'El nombre es obligatorio';
    if (!this.form.identificacion?.trim()) this.errors.identificacion = 'La identificación es obligatoria';
    if (!this.editando() && !this.form.contrasena?.trim()) this.errors.contrasena = 'La contraseña es obligatoria';
    return Object.keys(this.errors).length === 0;
  }

  private mostrarToast(msg: string, tipo: string): void {
    this.toast.set({ msg, tipo });
    setTimeout(() => this.toast.set(null), 3500);
  }

  private emptyForm(): ClienteRequest {
    return { nombre: '', genero: '', identificacion: '', direccion: '', telefono: '', contrasena: '', estado: true };
  }
}
