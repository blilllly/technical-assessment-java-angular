export interface Movimiento {
  id: number;
  fecha: string;
  tipoMovimiento: string;
  valor: number;
  saldo: number;
  cuentaId: number;
  numeroCuenta: string;
}

export interface MovimientoRequest {
  fecha?: string;
  tipoMovimiento: string;
  valor: number;
  cuentaId: number;
}

export interface ReporteItem {
  Fecha: string;
  Cliente: string;
  'Numero Cuenta': string;
  Tipo: string;
  'Saldo Inicial': number;
  Estado: boolean;
  Movimiento: number;
  'Saldo Disponible': number;
}

export interface ReporteResponse {
  reporte: ReporteItem[];
  pdfBase64: string;
}
