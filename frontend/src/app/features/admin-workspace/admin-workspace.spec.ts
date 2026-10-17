import { TestBed } from '@angular/core/testing';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';
import { AdminApi } from '../../core/api/admin-api';
import { AdminWorkspace } from './admin-workspace';

registerLocaleData(localePt);

describe('AdminWorkspace', () => {
  it('edita apenas o preço do veículo e só envia ao clicar em salvar', async () => {
    const carro = { id: 9, modelo: 'Carro teste', valorVenda: 12000, valorCusto: 10000 };
    const api = { get: vi.fn((path: string) => of(path === 'admin/veiculos' ? [carro] : [])), put: vi.fn(() => of({})), post: vi.fn(), delete: vi.fn() };
    TestBed.configureTestingModule({ imports: [AdminWorkspace], providers: [provideRouter([{ path: 'admin/veiculos', component: AdminWorkspace, data: { section: 'veiculos' } }]), { provide: AdminApi, useValue: api }] });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin/veiculos', AdminWorkspace);
    const root = harness.routeNativeElement!;
    root.querySelector<HTMLButtonElement>('[aria-label="Editar preço de Carro teste"]')!.click();
    harness.detectChanges(); await harness.fixture.whenStable();
    const inputs = root.querySelectorAll<HTMLInputElement>('.dialog input');
    expect(inputs.length).toBe(1);
    expect(inputs[0].value).toBe('12000');
    inputs[0].value = '-1'; inputs[0].dispatchEvent(new Event('input', { bubbles: true }));
    root.querySelector<HTMLButtonElement>('.dialog .primary-button')!.click();
    expect(api.put).not.toHaveBeenCalled();
    inputs[0].value = '13500.50'; inputs[0].dispatchEvent(new Event('input', { bubbles: true }));
    root.querySelector('.dialog form')!.dispatchEvent(new Event('submit', { cancelable: true }));
    expect(api.put).not.toHaveBeenCalled();
    root.querySelector<HTMLButtonElement>('.dialog .primary-button')!.click();
    expect(api.put).toHaveBeenCalledWith('admin/veiculos/9', { valorVenda: 13500.5 });
    expect(api.post).not.toHaveBeenCalled();
  });

  it('consulta todas as notas com campo vazio e uma nota quando informa o ID', async () => {
    const notas = [{ id: 7, numero: 'NF-007', serie: '1', entrada: true, dataEmissao: '2026-01-01', valorTotal: 100 }, { id: 8, numero: 'NF-008', serie: '2', entrada: false, dataEmissao: '2026-01-02', valorTotal: 150 }];
    const api = { get: vi.fn((path: string) => of(path === 'admin/notas-fiscais' ? notas : path === 'admin/notas-fiscais/7' ? notas[0] : [])), post: vi.fn(), put: vi.fn(), delete: vi.fn() };
    TestBed.configureTestingModule({ imports: [AdminWorkspace], providers: [provideRouter([{ path: 'admin/notas-fiscais', component: AdminWorkspace, data: { section: 'notas-fiscais' } }]), { provide: AdminApi, useValue: api }] });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin/notas-fiscais', AdminWorkspace);
    const root = harness.routeNativeElement!;
    const search = root.querySelector('.lookup-panel form')!;
    search.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    harness.detectChanges(); await harness.fixture.whenStable();
    expect(api.get).toHaveBeenCalledWith('admin/notas-fiscais');
    expect(root.textContent).toContain('NF-007'); expect(root.textContent).toContain('NF-008');
    const input = root.querySelector('#nota-id') as HTMLInputElement;
    input.value = '7'; input.dispatchEvent(new Event('input', { bubbles: true }));
    search.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    harness.detectChanges(); await harness.fixture.whenStable();
    expect(api.get).toHaveBeenCalledWith('admin/notas-fiscais/7');
    expect(root.textContent).toContain('NF-007'); expect(root.textContent).not.toContain('NF-008');
    expect(api.post).not.toHaveBeenCalled();
  });

  it('troca para pessoa jurídica e salva cliente e fornecedor sem acesso de usuário', async () => {
    const api = { get: vi.fn(() => of([])), post: vi.fn(() => of({ id: 4 })), put: vi.fn(), delete: vi.fn() };
    TestBed.configureTestingModule({ imports: [AdminWorkspace], providers: [provideRouter([{ path: 'admin/pessoas', component: AdminWorkspace, data: { section: 'pessoas' } }]), { provide: AdminApi, useValue: api }] });
    const harness = await RouterTestingHarness.create();
    const component = await harness.navigateByUrl('/admin/pessoas', AdminWorkspace);
    component.abrirNovo(); harness.detectChanges(); await harness.fixture.whenStable();
    const root = harness.routeNativeElement!;
    const usuario = Array.from(root.querySelectorAll('label')).find((label) => label.textContent?.includes('Criar acesso de usuário'))!.querySelector('input')!;
    usuario.click(); harness.detectChanges(); await harness.fixture.whenStable();
    const tipo = root.querySelector('select')!;
    tipo.value = 'JURIDICA'; tipo.dispatchEvent(new Event('change', { bubbles: true }));
    harness.detectChanges(); await harness.fixture.whenStable();
    expect(root.textContent).not.toContain('Criar acesso de usuário');
    for (const text of ['Cadastrar como cliente', 'Cadastrar como fornecedor']) {
      Array.from(root.querySelectorAll('label')).find((label) => label.textContent?.includes(text))!.querySelector('input')!.click();
    }
    harness.detectChanges(); await harness.fixture.whenStable();
    component.form.patchValue({ nome: 'Empresa teste', email: 'empresa@example.com', cnpj: '11222333000181', razaoSocial: 'Empresa teste' });
    root.querySelector<HTMLButtonElement>('.dialog .primary-button')!.click();
    expect(api.post).toHaveBeenCalledWith('admin/pessoas', expect.objectContaining({ tipoPessoa: 'JURIDICA', cadastrarCliente: true, cadastrarFornecedor: true, usuario: null }));
  });

  it('cadastra pessoa física com cliente e usuário somente no Salvar cadastro', async () => {
    const api = { get: vi.fn(() => of([])), post: vi.fn(() => of({ id: 4 })), put: vi.fn(), delete: vi.fn() };
    TestBed.configureTestingModule({ imports: [AdminWorkspace], providers: [provideRouter([{ path: 'admin/pessoas', component: AdminWorkspace, data: { section: 'pessoas' } }]), { provide: AdminApi, useValue: api }] });
    const harness = await RouterTestingHarness.create();
    const component = await harness.navigateByUrl('/admin/pessoas', AdminWorkspace);
    component.abrirNovo(); harness.detectChanges(); await harness.fixture.whenStable();
    const root = harness.routeNativeElement!;
    const labelInput = (text: string) => Array.from(root.querySelectorAll('label')).find((label) => label.textContent?.includes(text))?.querySelector('input') as HTMLInputElement;
    const cliente = labelInput('Cadastrar como cliente');
    const usuario = labelInput('Criar acesso de usuário');
    expect(cliente).toBeTruthy(); expect(usuario).toBeTruthy();
    cliente.click(); usuario.click(); harness.detectChanges(); await harness.fixture.whenStable();
    component.form.patchValue({ nome: 'Pessoa vinculada', email: 'pessoa@example.com', cpf: '52998224725', dataNascimento: '1990-01-01', senha: 'SenhaDeTeste@2026', perfilId: 2 });
    expect(api.post).not.toHaveBeenCalled();
    root.querySelector<HTMLButtonElement>('.dialog .primary-button')!.click();
    expect(api.post).toHaveBeenCalledWith('admin/pessoas', expect.objectContaining({ cadastrarCliente: true, cadastrarFornecedor: false, usuario: { senha: 'SenhaDeTeste@2026', perfilId: 2 } }));
    expect(api.post).toHaveBeenCalledTimes(1);
  });

  it('lista fornecedores cadastrados e envia o ID do fornecedor escolhido ao salvar a nota', async () => {
    const api = {
      get: vi.fn((path: string) => of(path === 'admin/fornecedores'
        ? [{ id: 7, pessoaJuridicaId: 700, razaoSocial: 'Fornecedor cadastrado', cnpj: '11222333000181', ativo: true }]
        : [{ id: 3, nome: 'Marca teste' }])),
      post: vi.fn(() => of({ id: 5, numero: '123', entrada: true })), put: vi.fn(), delete: vi.fn(),
    };
    TestBed.configureTestingModule({ imports: [AdminWorkspace], providers: [provideRouter([{ path: 'admin/notas-fiscais', component: AdminWorkspace, data: { section: 'notas-fiscais' } }]), { provide: AdminApi, useValue: api }] });
    const harness = await RouterTestingHarness.create();
    const component = await harness.navigateByUrl('/admin/notas-fiscais', AdminWorkspace);
    await harness.fixture.whenStable();
    const root = harness.routeNativeElement!;
    const select = root.querySelector('#invoice-supplier') as HTMLSelectElement;
    expect(select).not.toBeNull();
    expect(select.textContent).toContain('Fornecedor cadastrado');
    select.value = '7'; select.dispatchEvent(new Event('change', { bubbles: true }));
    component.notaForm.patchValue({ numero: '123', serie: '1', descricao: 'Veículo teste', modelo: 'Modelo teste', marcaId: '3', anoFabricacao: '2025', anoModelo: '2026', cor: 'Preto', chassi: '9BWZZZ377VT004299', valorCusto: '100', valorVenda: '150', valorUnitario: '100' });
    expect(api.post).not.toHaveBeenCalled();
    Array.from(root.querySelectorAll('button')).find((button) => button.textContent?.includes('Salvar nota fiscal'))!.click();
    expect(api.post).toHaveBeenCalledWith('admin/notas-fiscais', expect.objectContaining({ fornecedorId: 7, entrada: true }));
  });

  it('vincula o usuário ao cliente usando a pessoa do mesmo CPF somente ao salvar', async () => {
    const api = {
      get: vi.fn((path: string) => of(path === 'admin/pessoas'
        ? [{ id: 17, nome: 'Pessoa existente', tipoPessoa: 'FISICA', cpf: '52998224725' }]
        : { items: [{ id: 80, nome: 'Pessoa existente', login: 'usuario@example.com', cpf: '52998224725' }], totalPages: 1 })),
      post: vi.fn(() => of({ id: 5, pessoaId: 17, tipoPessoa: 'FISICA', ativo: true })), put: vi.fn(), delete: vi.fn(),
    };
    TestBed.configureTestingModule({ imports: [AdminWorkspace], providers: [provideRouter([{ path: 'admin/clientes', component: AdminWorkspace, data: { section: 'clientes' } }]), { provide: AdminApi, useValue: api }] });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin/clientes', AdminWorkspace);
    await harness.fixture.whenStable();
    const root = harness.routeNativeElement!;
    const select = root.querySelector('#client-user-select') as HTMLSelectElement;
    expect(select).not.toBeNull();
    expect(select.textContent).toContain('usuario@example.com');
    select.value = '17'; select.dispatchEvent(new Event('change', { bubbles: true }));
    expect(api.post).not.toHaveBeenCalled();
    Array.from(root.querySelectorAll('button')).find((button) => button.textContent?.includes('Salvar cliente'))!.click();
    expect(api.post).toHaveBeenCalledWith('admin/clientes', { pessoaId: 17, ativo: true });
    expect(api.post).toHaveBeenCalledTimes(1);
  });

  for (const section of ['pessoas', 'usuarios', 'fornecedores', 'marcas', 'enderecos']) {
    it(`salva ${section} somente ao clicar no botão Salvar`, async () => {
      const api = { get: vi.fn(() => of([])), post: vi.fn(() => of({ id: 4 })), put: vi.fn(), delete: vi.fn() };
      TestBed.configureTestingModule({
        imports: [AdminWorkspace],
        providers: [provideRouter([{ path: `admin/${section}`, component: AdminWorkspace, data: { section } }]), { provide: AdminApi, useValue: api }],
      });
      const harness = await RouterTestingHarness.create();
      const component = await harness.navigateByUrl(`/admin/${section}`, AdminWorkspace);
      component.abrirNovo();
      harness.detectChanges();
      await harness.fixture.whenStable();
      component.form.patchValue({ tipoPessoa: 'FISICA', nome: 'Pessoa teste', email: 'teste@example.com', cpf: '52998224725', dataNascimento: '1990-01-01', senha: 'SenhaDeTeste@2026', codigoArea: '63', telefone: '999999999', perfilId: 1, pessoaJuridicaId: 3, ativo: true, cep: '77001000', logradouro: 'Rua teste', numero: '10', bairro: 'Centro', cidade: 'Palmas', estado: 'TO' });
      const form = harness.routeNativeElement!.querySelector('.dialog form')!;
      form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
      expect(api.post).not.toHaveBeenCalled();
      expect(api.put).not.toHaveBeenCalled();
      const save = Array.from(form.querySelectorAll('button')).find((button) => button.textContent?.includes('Salvar'))!;
      save.click();
      expect(api.post).toHaveBeenCalledTimes(1);
    });
  }

  it('seleciona cliente sem salvar e persiste apenas no botão Salvar cliente', async () => {
    const api = { get: vi.fn(() => of([{ id: 2, nome: 'Cliente teste', tipoPessoa: 'FISICA' }])), post: vi.fn(() => of({ id: 4 })), put: vi.fn(), delete: vi.fn() };
    TestBed.configureTestingModule({ imports: [AdminWorkspace], providers: [provideRouter([{ path: 'admin/clientes', component: AdminWorkspace, data: { section: 'clientes' } }]), { provide: AdminApi, useValue: api }] });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin/clientes', AdminWorkspace);
    const root = harness.routeNativeElement!;
    const select = root.querySelector('#person-select') as HTMLSelectElement;
    select.value = '2'; select.dispatchEvent(new Event('change', { bubbles: true }));
    expect(api.post).not.toHaveBeenCalled();
    const save = Array.from(root.querySelectorAll('button')).find((button) => button.textContent?.includes('Salvar cliente'));
    expect(save).toBeDefined();
    save!.click();
    expect(api.post).toHaveBeenCalledWith('admin/clientes', { pessoaId: 2, ativo: true });
  });

  it('salva nota apenas pelo botão Salvar nota fiscal', async () => {
    const api = { get: vi.fn(() => of([])), post: vi.fn(() => of({ id: 4, numero: '123', entrada: false })), put: vi.fn(), delete: vi.fn() };
    TestBed.configureTestingModule({ imports: [AdminWorkspace], providers: [provideRouter([{ path: 'admin/notas-fiscais', component: AdminWorkspace, data: { section: 'notas-fiscais' } }]), { provide: AdminApi, useValue: api }] });
    const harness = await RouterTestingHarness.create();
    const component = await harness.navigateByUrl('/admin/notas-fiscais', AdminWorkspace);
    const saida = Array.from(harness.routeNativeElement!.querySelectorAll('button')).find((button) => button.textContent?.trim() === 'Saída')!;
    saida.click();
    harness.detectChanges();
    await harness.fixture.whenStable();
    component.notaForm.patchValue({ numero: '123', serie: '1', clienteId: '2', produtoId: '3', valorUnitario: '100' });
    expect(Object.entries(component.notaForm.controls).filter(([, control]) => control.invalid).map(([name]) => name)).toEqual([]);
    const form = harness.routeNativeElement!.querySelector('.invoice-panel form')!;
    form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    expect(api.post).not.toHaveBeenCalled();
    const save = Array.from(form.querySelectorAll('button')).find((button) => button.textContent?.includes('Salvar nota fiscal'));
    expect(save).toBeDefined(); save!.click();
    expect(api.post).toHaveBeenCalledWith('admin/notas-fiscais', expect.objectContaining({ numero: '123', entrada: false }));
  });

  it('abre pessoa física com campos obrigatórios e alterna para os campos de pessoa jurídica', async () => {
    const api = { get: vi.fn(() => of([])), post: vi.fn(() => of({ id: 4 })), put: vi.fn(), delete: vi.fn() };
    TestBed.configureTestingModule({
      imports: [AdminWorkspace],
      providers: [
        provideRouter([{ path: 'admin/pessoas', component: AdminWorkspace, data: { section: 'pessoas' } }]),
        { provide: AdminApi, useValue: api },
      ],
    });
    const harness = await RouterTestingHarness.create();
    const component = await harness.navigateByUrl('/admin/pessoas', AdminWorkspace);
    component.abrirNovo();
    harness.detectChanges();
    await harness.fixture.whenStable();
    const root = harness.routeNativeElement!;
    const inputFor = (label: string) => Array.from(root.querySelectorAll('label')).find((element) => element.textContent?.trim().startsWith(label))?.querySelector('input') ?? null;
    expect(inputFor('CPF')).not.toBeNull();
    expect(inputFor('Data de nascimento')).not.toBeNull();
    expect(inputFor('CPF')?.hasAttribute('required')).toBe(true);
    expect(inputFor('Data de nascimento')?.hasAttribute('required')).toBe(true);
    expect(inputFor('CNPJ')).toBeNull();

    const tipo = root.querySelector('select') as HTMLSelectElement;
    tipo.value = 'JURIDICA';
    tipo.dispatchEvent(new Event('change', { bubbles: true }));
    harness.detectChanges();
    await harness.fixture.whenStable();
    expect(inputFor('CPF')).toBeNull();
    expect(inputFor('CNPJ')?.hasAttribute('required')).toBe(true);
    expect(inputFor('Razão social')?.hasAttribute('required')).toBe(true);
    component.form.patchValue({ nome: 'Empresa teste', email: 'empresa@example.com' });
    component.salvar();
    expect(api.post).not.toHaveBeenCalled();
  });

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
    (harness.routeNativeElement?.querySelector('.dialog .primary-button') as HTMLButtonElement).click();
    await harness.fixture.whenStable();

    expect(api.post).toHaveBeenCalledWith('admin/marcas', { nome: 'Lancia' });
    expect(harness.routeNativeElement?.textContent).toContain('Cadastro criado.');
  });
});
