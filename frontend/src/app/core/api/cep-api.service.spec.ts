import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { CepApiService } from './cep-api.service';

describe('CepApiService', () => {
  let service: CepApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        CepApiService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(CepApiService);
    http = TestBed.inject(HttpTestingController);
  });

  it('deve consultar o CEP normalizado no backend', () => {
    service.buscar('77001-000').subscribe((endereco) => {
      expect(endereco.cep).toBe('77001000');
    });

    const request = http.expectOne('http://localhost:8080/ceps/77001000');
    expect(request.request.method).toBe('GET');
    request.flush({ cep: '77001000', logradouro: 'Rua A', bairro: 'Centro' });
  });
});
