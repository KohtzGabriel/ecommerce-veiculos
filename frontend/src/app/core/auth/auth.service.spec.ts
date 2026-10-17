import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let auth: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [AuthService, provideHttpClient(), provideHttpClientTesting()],
    });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('salva JWT e perfil na sessão após autenticação', () => {
    auth.login({ login: 'admin@example.com', senha: 'uma-senha-valida' }, 'ADMINISTRADOR').subscribe();
    const request = http.expectOne('http://localhost:8080/auth/login');
    expect(request.request.method).toBe('POST');
    request.flush({
      accessToken: 'jwt-de-teste',
      tokenType: 'Bearer',
      expiresAt: '2099-01-01T00:00:00Z',
      roles: ['ADMINISTRADOR'],
    });

    expect(auth.isAuthenticated()).toBe(true);
    expect(auth.hasRole('ADMINISTRADOR')).toBe(true);
    expect(sessionStorage.getItem('tp2.auth.session')).toContain('jwt-de-teste');
  });

  it('restaura a sessão persistida ao recriar o serviço', () => {
    sessionStorage.setItem('tp2.auth.session', JSON.stringify({
      accessToken: 'jwt-persistido',
      tokenType: 'Bearer',
      expiresAt: '2099-01-01T00:00:00Z',
      roles: ['CLIENTE'],
    }));

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({ providers: [AuthService] });
    const reloadedAuth = TestBed.inject(AuthService);

    expect(reloadedAuth.isAuthenticated()).toBe(true);
    expect(reloadedAuth.hasRole('CLIENTE')).toBe(true);
  });

  it('rejeita perfil incompatível e não mantém uma sessão anterior', () => {
    sessionStorage.setItem('tp2.auth.session', JSON.stringify({
      accessToken: 'admin-antigo', tokenType: 'Bearer', expiresAt: '2099-01-01T00:00:00Z', roles: ['ADMINISTRADOR'],
    }));
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [AuthService, provideHttpClient(), provideHttpClientTesting()],
    });
    const currentAuth = TestBed.inject(AuthService);
    const currentHttp = TestBed.inject(HttpTestingController);
    let failed = false;
    currentAuth.login({ login: 'cliente@example.com', senha: 'senha' }, 'CLIENTE')
      .subscribe({ error: () => { failed = true; } });
    currentHttp.expectOne('http://localhost:8080/auth/login').flush({
      accessToken: 'admin-jwt', tokenType: 'Bearer', expiresAt: '2099-01-01T00:00:00Z', roles: ['ADMINISTRADOR'],
    });

    expect(failed).toBe(true);
    expect(currentAuth.isAuthenticated()).toBe(false);
    expect(sessionStorage.getItem('tp2.auth.session')).toBeNull();
    currentHttp.verify();
  });

  it('remove token e sessão ao encerrar', () => {
    sessionStorage.setItem('tp2.auth.session', JSON.stringify({
      accessToken: 'jwt', tokenType: 'Bearer', expiresAt: '2099-01-01T00:00:00Z', roles: ['CLIENTE'],
    }));
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({ providers: [AuthService] });
    const reloadedAuth = TestBed.inject(AuthService);
    reloadedAuth.logout();

    expect(sessionStorage.getItem('tp2.auth.session')).toBeNull();
    expect(reloadedAuth.isAuthenticated()).toBe(false);
  });
});
