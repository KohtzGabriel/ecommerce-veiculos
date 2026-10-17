import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

import { AdminApi } from './admin-api';

describe('AdminApi', () => {
  let service: AdminApi;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AdminApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('consulta recurso administrativo com filtros sem perder os parâmetros', () => {
    service.get<{ id: number }[]>('/admin/veiculos', { modelo: 'Giulia', status: 'DISPONIVEL' })
      .subscribe((items) => expect(items[0].id).toBe(4));
    const req = http.expectOne((request) => request.url === 'http://localhost:8081/admin/veiculos');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('modelo')).toBe('Giulia');
    expect(req.request.params.get('status')).toBe('DISPONIVEL');
    req.flush([{ id: 4 }]);
  });
});
