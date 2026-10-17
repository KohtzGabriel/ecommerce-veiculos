import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { roleGuard } from './auth.guards';

@Component({ template: '<h1>restricted</h1>' })
class RestrictedPage {}

@Component({ template: '<h1>login</h1>' })
class LoginPage {}

@Component({ template: '<h1>catalog</h1>' })
class CatalogPage {}

describe('roleGuard', () => {
  beforeEach(() => sessionStorage.clear());

  async function openAdminRoute() {
    TestBed.configureTestingModule({
      imports: [RestrictedPage, LoginPage, CatalogPage],
      providers: [provideRouter([
        { path: 'admin/home', component: RestrictedPage, canActivate: [roleGuard], data: { role: 'ADMINISTRADOR' } },
        { path: 'admin/login', component: LoginPage },
        { path: 'cliente/login', component: LoginPage },
        { path: 'loja', component: CatalogPage },
      ])],
    });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin/home');
    return { harness, router: TestBed.inject(Router) };
  }

  it('redireciona para login quando não há token', async () => {
    const { router } = await openAdminRoute();
    expect(router.url).toBe('/admin/login');
  });

  it('redireciona cliente para o mostruário ao acessar gestão', async () => {
    sessionStorage.setItem('tp2.auth.session', JSON.stringify({
      accessToken: 'cliente-token', tokenType: 'Bearer', expiresAt: '2099-01-01T00:00:00Z', roles: ['CLIENTE'],
    }));
    const { router } = await openAdminRoute();
    expect(router.url).toBe('/loja');
  });

  it('permite acesso ao administrador autenticado', async () => {
    sessionStorage.setItem('tp2.auth.session', JSON.stringify({
      accessToken: 'admin-token', tokenType: 'Bearer', expiresAt: '2099-01-01T00:00:00Z', roles: ['ADMINISTRADOR'],
    }));
    const { router } = await openAdminRoute();
    expect(router.url).toBe('/admin/home');
  });
});
