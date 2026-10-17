import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { AuthService } from '../../core/auth/auth.service';
import { AdminShell } from './admin-shell/admin-shell';

@Component({ template: '<p>Login administrativo</p>' })
class LoginDestination {}

describe('AdminShell', () => {
  it('apresenta a navegação de gestão e encerra a sessão ao sair', async () => {
    const logout = vi.fn();
    TestBed.configureTestingModule({
      imports: [AdminShell, LoginDestination],
      providers: [
        provideRouter([{ path: 'admin', component: AdminShell }, { path: 'admin/login', component: LoginDestination }]),
        { provide: AuthService, useValue: { logout } },
      ],
    });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin', AdminShell);
    await harness.fixture.whenStable();

    const root = harness.routeNativeElement!;
    expect(root.textContent).toContain('Visão geral');
    expect(root.textContent).toContain('Pessoas');
    expect(root.textContent).toContain('Notas fiscais');
    expect(root.querySelector('button[aria-label="Sair da área administrativa"]')).not.toBeNull();

    (root.querySelector('button[aria-label="Sair da área administrativa"]') as HTMLButtonElement).click();
    await harness.fixture.whenStable();
    expect(logout).toHaveBeenCalledOnce();
    expect(TestBed.inject(Router).url).toBe('/admin/login');
  });
});
