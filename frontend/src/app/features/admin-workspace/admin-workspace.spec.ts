import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';
import { AdminApi } from '../../core/api/admin-api';
import { AdminWorkspace } from './admin-workspace';

describe('AdminWorkspace', () => {
  it('carrega marcas e envia POST ao salvar novo cadastro', async () => {
    const api = {
      get: vi.fn(() => of([{ id: 2, nome: 'Alfa' }])),
      post: vi.fn(() => of({ id: 3, nome: 'Lancia' })),
      put: vi.fn(), delete: vi.fn(),
    };
    TestBed.configureTestingModule({
      imports: [AdminWorkspace],
      providers: [
        provideRouter([{ path: 'admin/marcas', component: AdminWorkspace, data: { section: 'marcas' } }]),
        { provide: AdminApi, useValue: api },
      ],
    });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin/marcas', AdminWorkspace);
    await harness.fixture.whenStable();
    expect(harness.routeNativeElement?.textContent).toContain('Alfa');

    (harness.routeNativeElement?.querySelector('button.primary-button') as HTMLButtonElement).click();
    await harness.fixture.whenStable();
    expect(harness.routeNativeElement?.querySelector('.dialog')).not.toBeNull();
    const input = harness.routeNativeElement?.querySelector('.dialog input') as HTMLInputElement;
    input.value = 'Lancia'; input.dispatchEvent(new Event('input', { bubbles: true }));
    await harness.fixture.whenStable();
    (harness.routeNativeElement?.querySelector('.dialog button[type="submit"]') as HTMLButtonElement).click();
    await harness.fixture.whenStable();

    expect(api.post).toHaveBeenCalledWith('admin/marcas', { nome: 'Lancia' });
    expect(harness.routeNativeElement?.textContent).toContain('Cadastro criado.');
  });
});
