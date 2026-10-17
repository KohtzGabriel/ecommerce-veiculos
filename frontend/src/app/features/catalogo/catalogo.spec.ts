import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Component, signal } from '@angular/core';
import { provideRouter } from '@angular/router';
import { LOCALE_ID } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';
import { CatalogoApiService } from './catalogo-api.service';
import { CatalogoPage } from './catalogo';
import { API_BASE_URL } from '../../core/auth/auth-api.service';
import { AuthService } from '../../core/auth/auth.service';

registerLocaleData(localePt, 'pt-BR');

describe('CatalogoApiService', () => {
  let service: CatalogoApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(CatalogoApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('consulta o catálogo público de veículos', () => {
    service.listar().subscribe((veiculos) => expect(veiculos[0].modelo).toBe('Sedan'));
    const request = http.expectOne(`${API_BASE_URL}/catalogo/veiculos`);
    expect(request.request.method).toBe('GET');
    request.flush([{
      id: 1, descricao: 'Sedan completo', valorVenda: 120000, marcaId: 3, modelo: 'Sedan',
      anoFabricacao: 2024, anoModelo: 2025, cor: 'Prata', marca: 'Marca A', status: 'DISPONIVEL',
    }]);
  });

  it('envia filtros disponíveis para consulta pública', () => {
    service.listar({ anoModelo: 2025, modelo: 'Giulia', marcaId: 3 }).subscribe();
    const request = http.expectOne(`${API_BASE_URL}/catalogo/veiculos?anoModelo=2025&modelo=Giulia&marcaId=3`);
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });
});

@Component({ template: '<h1>Login Cliente</h1>' })
class CustomerLoginDestination {}

describe('CatalogoPage', () => {
  const carro = {
    id: 7, descricao: 'Versão completa', valorVenda: 92500, marcaId: 3, modelo: 'Hatch',
    anoFabricacao: 2024, anoModelo: 2025, cor: 'Prata', marca: 'Marca X', status: 'DISPONIVEL',
  };

  async function abrir(logado: boolean) {
    const logged = signal(logado);
    const auth = {
      session: signal(null),
      hasRole: (role: string) => role === 'CLIENTE' && logged(),
      logout: vi.fn(() => logged.set(false)),
    };
    TestBed.configureTestingModule({
      imports: [CatalogoPage, CustomerLoginDestination],
      providers: [
        { provide: LOCALE_ID, useValue: 'pt-BR' },
        provideRouter([
          { path: 'loja', component: CatalogoPage },
          { path: 'cliente/login', component: CustomerLoginDestination },
        ]),
        { provide: AuthService, useValue: auth },
        { provide: CatalogoApiService, useValue: { listar: () => of([carro]) } },
      ],
    });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/loja', CatalogoPage);
    await harness.fixture.whenStable();
    return { harness, auth };
  }

  it('mostra veículos disponíveis sem ações de compra', async () => {
    const { harness } = await abrir(false);
    expect(harness.routeNativeElement?.textContent).toContain('Hatch');
    expect(harness.routeNativeElement?.textContent).toContain('Marca X');
    expect(harness.routeNativeElement?.textContent).not.toContain('Comprar');
    expect(harness.routeNativeElement?.querySelector('a.login-link')?.textContent).toContain('Entrar');
  });

  it('encerra a sessão do cliente pelo mostruário', async () => {
    const { harness, auth } = await abrir(true);
    (harness.routeNativeElement?.querySelector('button.login-link') as HTMLButtonElement).click();
    await harness.fixture.whenStable();
    expect(auth.logout).toHaveBeenCalledOnce();
    expect(harness.routeNativeElement?.textContent).toContain('Login Cliente');
  });
});
