import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { MovimientoService } from './movimiento.service';
import { Movimiento, MovimientoRequest } from '../models/movimiento.model';

describe('MovimientoService', () => {
  let service: MovimientoService;
  let http: HttpTestingController;

  const baseUrl = 'http://localhost:8080/movimientos';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        MovimientoService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(MovimientoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('debería listar movimientos', () => {
    const mock: Movimiento[] = [
      { id: 1, fecha: '2022-02-10', tipoMovimiento: 'Retiro', valor: -575, saldo: 1425, cuentaId: 1, numeroCuenta: '478758' },
    ];

    service.listar().subscribe(movimientos => {
      expect(movimientos.length).toBe(1);
      expect(movimientos[0].valor).toBe(-575);
    });

    const req = http.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('debería crear un movimiento de depósito', () => {
    const request: MovimientoRequest = {
      tipoMovimiento: 'Depósito',
      valor: 600,
      cuentaId: 2,
    };
    const mock: Movimiento = {
      id: 2, fecha: '2022-02-10', tipoMovimiento: 'Depósito',
      valor: 600, saldo: 700, cuentaId: 2, numeroCuenta: '225487',
    };

    service.crear(request).subscribe(mov => {
      expect(mov.valor).toBe(600);
      expect(mov.saldo).toBe(700);
    });

    const req = http.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    req.flush(mock);
  });

  it('debería eliminar un movimiento', () => {
    service.eliminar(1).subscribe(() => {});

    const req = http.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
