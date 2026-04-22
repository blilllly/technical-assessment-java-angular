import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cuenta, CuentaRequest } from '../models/cuenta.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CuentaService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/cuentas`;

  listar(): Observable<Cuenta[]> {
    return this.http.get<Cuenta[]>(this.url);
  }

  buscarPorId(id: number): Observable<Cuenta> {
    return this.http.get<Cuenta>(`${this.url}/${id}`);
  }

  crear(request: CuentaRequest): Observable<Cuenta> {
    return this.http.post<Cuenta>(this.url, request);
  }

  actualizar(id: number, request: CuentaRequest): Observable<Cuenta> {
    return this.http.put<Cuenta>(`${this.url}/${id}`, request);
  }

  actualizarParcial(id: number, request: Partial<CuentaRequest>): Observable<Cuenta> {
    return this.http.patch<Cuenta>(`${this.url}/${id}`, request);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
