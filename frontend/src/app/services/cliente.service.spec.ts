import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ClienteService } from './cliente.service';
import { Cliente, ClienteRequest } from '../models/cliente.model';

describe('ClienteService', () => {
  let service: ClienteService;
  let http: HttpTestingController;

  const baseUrl = 'http://localhost:8080/clientes';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ClienteService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(ClienteService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('debería listar clientes', () => {
    const mock: Cliente[] = [
      { clienteId: 1, nombre: 'Jose Lema', identificacion: 'ID-001', estado: true },
    ];

    service.listar().subscribe(clientes => {
      expect(clientes.length).toBe(1);
      expect(clientes[0].nombre).toBe('Jose Lema');
    });

    const req = http.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('debería crear un cliente', () => {
    const request: ClienteRequest = {
      nombre: 'Jose Lema',
      identificacion: 'ID-001',
      contrasena: '1234',
      estado: true,
    };
    const mock: Cliente = { clienteId: 1, ...request };

    service.crear(request).subscribe(cliente => {
      expect(cliente.clienteId).toBe(1);
      expect(cliente.nombre).toBe('Jose Lema');
    });

    const req = http.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mock);
  });

  it('debería actualizar un cliente', () => {
    const request: ClienteRequest = {
      nombre: 'Jose Updated',
      identificacion: 'ID-001',
      contrasena: '1234',
      estado: true,
    };
    const mock: Cliente = { clienteId: 1, ...request };

    service.actualizar(1, request).subscribe(cliente => {
      expect(cliente.nombre).toBe('Jose Updated');
    });

    const req = http.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(mock);
  });

  it('debería eliminar un cliente', () => {
    service.eliminar(1).subscribe(() => {});

    const req = http.expectOne(`${baseUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
