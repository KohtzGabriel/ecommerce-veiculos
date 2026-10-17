import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { AdminApi } from '../../core/api/admin-api';
import { AdminHome } from './admin-home';

registerLocaleData(localePt, 'pt-BR');

describe('AdminHome', () => {
  it('apresenta a visão geral e usa os dados reais do estoque', async () => {
    TestBed.configureTestingModule({
      imports: [AdminHome],
      providers: [
        provideRouter([{ path: 'admin/home', component: AdminHome }]),
        { provide: AdminApi, useValue: { get: () => ({ subscribe: ({ next }: any) => next([{ id: 1, modelo: 'Giulia', marcaNome: 'Alfa', anoModelo: 2025, status: 'DISPONIVEL', valorVenda: 180000 }]) }) } },
      ],
    });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin/home', AdminHome);
    await harness.fixture.whenStable();

    expect(harness.routeNativeElement?.textContent).toContain('Visão geral');
    expect(harness.routeNativeElement?.textContent).toContain('Giulia');
    expect(harness.routeNativeElement?.textContent).not.toContain('undefined');
  });
});
