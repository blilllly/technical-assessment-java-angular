import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Movimiento, MovimientoRequest } from '../models/movimiento.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class MovimientoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/movimientos`;

  listar(): Observable<Movimiento[]> {
    return this.http.get<Movimiento[]>(this.url);
  }

  buscarPorId(id: number): Observable<Movimiento> {
    return this.http.get<Movimiento>(`${this.url}/${id}`);
  }

  crear(request: MovimientoRequest): Observable<Movimiento> {
    return this.http.post<Movimiento>(this.url, request);
  }

  actualizar(id: number, request: MovimientoRequest): Observable<Movimiento> {
    return this.http.put<Movimiento>(`${this.url}/${id}`, request);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
