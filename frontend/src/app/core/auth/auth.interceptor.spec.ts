import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { API_BASE_URL } from './auth-api.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpTestingController;
  let client: HttpClient;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: Router, useValue: { url: '/admin/home', navigateByUrl: () => Promise.resolve(true) } },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    client = TestBed.inject(HttpClient);
  });

  afterEach(() => http.verify());

  it('envia bearer token nas chamadas autenticadas', () => {
    sessionStorage.setItem('tp2.auth.session', JSON.stringify({
      accessToken: 'jwt-protegido', tokenType: 'Bearer', expiresAt: '2099-01-01T00:00:00Z', roles: ['ADMINISTRADOR'],
    }));
    client.get(`${API_BASE_URL}/admin/pessoas`).subscribe();
    const request = http.expectOne(`${API_BASE_URL}/admin/pessoas`);
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt-protegido');
    request.flush([]);
  });

  it('não envia token na autenticação e remove sessão após 401 protegido', () => {
    sessionStorage.setItem('tp2.auth.session', JSON.stringify({
      accessToken: 'jwt-expirado', tokenType: 'Bearer', expiresAt: '2099-01-01T00:00:00Z', roles: ['ADMINISTRADOR'],
    }));
    client.post(`${API_BASE_URL}/auth/login`, {}).subscribe({ error: () => undefined });
    const loginRequest = http.expectOne(`${API_BASE_URL}/auth/login`);
    expect(loginRequest.request.headers.has('Authorization')).toBe(false);
    loginRequest.flush({}, { status: 401, statusText: 'Unauthorized' });

    client.get(`${API_BASE_URL}/admin/pessoas`).subscribe({ error: () => undefined });
    http.expectOne(`${API_BASE_URL}/admin/pessoas`).flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(sessionStorage.getItem('tp2.auth.session')).toBeNull();
  });
});
