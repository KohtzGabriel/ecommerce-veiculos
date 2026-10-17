import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { LoginPage } from './login';

@Component({ template: '<h1>Destino</h1>' })
class DestinationPage {}

describe('LoginPage', () => {
  const session = {
    accessToken: 'jwt', tokenType: 'Bearer' as const,
    expiresAt: '2099-01-01T00:00:00Z', roles: ['ADMINISTRADOR' as const],
  };
  const authMock = { login: vi.fn(() => of(session)) };

  beforeEach(() => {
    authMock.login.mockClear();
    TestBed.configureTestingModule({
      imports: [LoginPage, DestinationPage],
      providers: [
        provideRouter([
          { path: 'admin/login', component: LoginPage, data: { role: 'ADMINISTRADOR' } },
          { path: 'cliente/login', component: LoginPage, data: { role: 'CLIENTE' } },
          { path: 'admin/home', component: DestinationPage },
          { path: 'loja', component: DestinationPage },
        ]),
        { provide: AuthService, useValue: authMock },
      ],
    });
  });

  it('envia credenciais com o perfil administrativo e redireciona após login', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin/login', LoginPage);
    const host = harness.routeNativeElement as HTMLElement;
    (host.querySelector('[name="login"]') as HTMLInputElement).value = 'admin@example.com';
    host.querySelector('[name="login"]')?.dispatchEvent(new Event('input', { bubbles: true }));
    (host.querySelector('[name="senha"]') as HTMLInputElement).value = 'SenhaForte-123456';
    host.querySelector('[name="senha"]')?.dispatchEvent(new Event('input', { bubbles: true }));
    host.querySelector('form')?.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await harness.fixture.whenStable();

    expect(authMock.login).toHaveBeenCalledWith(
      { login: 'admin@example.com', senha: 'SenhaForte-123456' }, 'ADMINISTRADOR',
    );
    expect(TestBed.inject(Router).url).toBe('/admin/home');
  });

  it('apresenta contexto de login do cliente', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/cliente/login', LoginPage);
    await harness.fixture.whenStable();
    expect(harness.routeNativeElement?.textContent).toContain('Acesse sua conta de cliente');
  });
});
