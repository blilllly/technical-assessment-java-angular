import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReporteService } from '../../services/reporte.service';
import { ClienteService } from '../../services/cliente.service';
import { ReporteItem } from '../../models/movimiento.model';
import { Cliente } from '../../models/cliente.model';

interface FiltroErrors {
  fechaInicio?: string;
  fechaFin?: string;
  clienteId?: string;
}

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reportes.component.html',
  styleUrl: './reportes.component.css',
})
export class ReportesComponent implements OnInit {
  private readonly reporteService = inject(ReporteService);
  private readonly clienteService = inject(ClienteService);

  readonly clientes = signal<Cliente[]>([]);
  readonly reporte = signal<ReporteItem[]>([]);
  readonly loading = signal(false);
  readonly buscado = signal(false);
  readonly toast = signal<{ msg: string; tipo: string } | null>(null);

  filtros = {
    fechaInicio: '',
    fechaFin: '',
    clienteId: 0,
  };
  errors: FiltroErrors = {};
  private pdfBase64 = '';

  ngOnInit(): void {
    this.clienteService.listar().subscribe({ next: data => this.clientes.set(data) });
  }

  buscar(): void {
    if (!this.validar()) return;
    this.loading.set(true);
    this.buscado.set(false);

    this.reporteService
      .generarReporte(this.filtros.fechaInicio, this.filtros.fechaFin, this.filtros.clienteId)
      .subscribe({
        next: data => {
          this.reporte.set(data.reporte);
          this.pdfBase64 = data.pdfBase64;
          this.loading.set(false);
          this.buscado.set(true);
          if (data.reporte.length === 0) this.mostrarToast('No se encontraron movimientos', 'error');
        },
        error: (err: any) => {
          this.loading.set(false);
          this.mostrarToast(err.error?.message ?? 'Error al generar reporte', 'error');
        },
      });
  }

  descargarPdf(): void {
    if (!this.pdfBase64) return;
    const link = document.createElement('a');
    link.href = `data:application/pdf;base64,${this.pdfBase64}`;
    link.download = `reporte-${this.filtros.fechaInicio}-${this.filtros.fechaFin}.pdf`;
    link.click();
  }

  get totalDebitos(): number {
    return this.reporte()
      .filter(r => r.Movimiento < 0)
      .reduce((sum, r) => sum + r.Movimiento, 0);
  }

  get totalCreditos(): number {
    return this.reporte()
      .filter(r => r.Movimiento > 0)
      .reduce((sum, r) => sum + r.Movimiento, 0);
  }

  private validar(): boolean {
    this.errors = {};
    if (!this.filtros.fechaInicio) this.errors.fechaInicio = 'La fecha de inicio es obligatoria';
    if (!this.filtros.fechaFin) this.errors.fechaFin = 'La fecha de fin es obligatoria';
    if (!this.filtros.clienteId) this.errors.clienteId = 'Debe seleccionar un cliente';
    if (this.filtros.fechaInicio && this.filtros.fechaFin && this.filtros.fechaInicio > this.filtros.fechaFin)
      this.errors.fechaFin = 'La fecha de fin debe ser mayor o igual a la fecha de inicio';
    return Object.keys(this.errors).length === 0;
  }

  private mostrarToast(msg: string, tipo: string): void {
    this.toast.set({ msg, tipo });
    setTimeout(() => this.toast.set(null), 3500);
  }
}
