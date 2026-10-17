import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { AdminApi } from '../../core/api/admin-api';
import { CepApiService } from '../../core/api/cep-api.service';

type Section = 'pessoas' | 'clientes' | 'usuarios' | 'fornecedores' | 'marcas' | 'veiculos' | 'notas-fiscais' | 'enderecos';
type Row = Record<string, any> & {
  id?: number; ativo?: boolean; bairro?: string; chassi?: string; cidade?: string; cnpj?: string | null; complemento?: string;
  cor?: string; cpf?: string | null; cep?: string; dataEmissao?: string; dataNascimento?: string | null; descricao?: string;
  email?: string; endereco?: any; entrada?: boolean; genero?: string; logradouro?: string; marcaId?: number;
  marcaNome?: string; estado?: string; modelo?: string; nome?: string; nomeFantasia?: string;
  numero?: string; perfilId?: number; perfis?: any[]; pessoaJuridicaId?: number; placa?: string;
  produtoId?: number; quilometragem?: number; razaoSocial?: string | null; renavam?: string; senha?: string;
  serie?: string; status?: string; tipoPessoa?: string; telefone?: string; valorCusto?: number;
  valorTotal?: number; valorUnitario?: number; valorVenda?: number; veiculoNovo?: any; quantidade?: number;
  clienteId?: number; fornecedorId?: number; anoFabricacao?: number; anoModelo?: number; codigoArea?: string;
};
type Field = { name: string; label: string; type?: string; required?: boolean; maxLength?: number; minLength?: number; options?: { id: number | string; nome: string }[] };

const text = (name: string, label: string, required = false, maxLength?: number): Field => ({ name, label, required, maxLength });

const MODULES: Record<Section, { title: string; description: string; endpoint: string; columns: { key: string; label: string }[]; fields: Field[] }> = {
  pessoas: {
    title: 'Pessoas', description: 'Cadastros de pessoas físicas e jurídicas da loja.', endpoint: 'admin/pessoas',
    columns: [{ key: 'nome', label: 'Nome / razão social' }, { key: 'tipoPessoa', label: 'Tipo' }, { key: 'cpf', label: 'CPF' }, { key: 'cnpj', label: 'CNPJ' }, { key: 'email', label: 'E-mail' }],
    fields: [{ name: 'tipoPessoa', label: 'Tipo de pessoa', type: 'select', required: true, options: [{ id: 'FISICA', nome: 'Pessoa física' }, { id: 'JURIDICA', nome: 'Pessoa jurídica' }] }, text('nome', 'Nome', true, 120), text('email', 'E-mail', true, 120), text('genero', 'Gênero'), text('cpf', 'CPF'), { name: 'dataNascimento', label: 'Data de nascimento', type: 'date' }, text('cnpj', 'CNPJ'), text('razaoSocial', 'Razão social'), text('nomeFantasia', 'Nome fantasia'), text('cep', 'CEP'), text('logradouro', 'Logradouro'), text('numero', 'Número'), text('complemento', 'Complemento'), text('bairro', 'Bairro'), text('cidade', 'Cidade', false, 80), text('estado', 'UF', false, 2)],
  },
  clientes: { title: 'Clientes', description: 'Localize clientes pelo CPF ou associe uma pessoa já cadastrada.', endpoint: 'admin/clientes', columns: [], fields: [] },
  usuarios: {
    title: 'Usuários', description: 'Acessos administrativos e de clientes, vinculados a pessoas físicas.', endpoint: 'usuarios',
    columns: [{ key: 'nome', label: 'Pessoa' }, { key: 'login', label: 'Login' }, { key: 'cpf', label: 'CPF' }, { key: 'ativo', label: 'Situação' }, { key: 'perfis', label: 'Perfis' }],
    fields: [text('nome', 'Nome', true, 120), text('email', 'E-mail / login', true, 120), text('cpf', 'CPF', true, 11), { name: 'dataNascimento', label: 'Data de nascimento', type: 'date', required: true }, text('codigoArea', 'DDD', true, 2), text('telefone', 'Telefone', true, 9), { name: 'senha', label: 'Senha inicial (mínimo 15 caracteres)', type: 'password', required: true, minLength: 15, maxLength: 128 }, { name: 'perfilId', label: 'Perfil', type: 'select', required: true, options: [{ id: 1, nome: 'Administrador' }, { id: 2, nome: 'Cliente' }] }, { name: 'ativo', label: 'Situação', type: 'checkbox' }],
  },
  fornecedores: {
    title: 'Fornecedores', description: 'Empresas fornecedoras vinculadas a uma pessoa jurídica.', endpoint: 'admin/fornecedores',
    columns: [{ key: 'razaoSocial', label: 'Razão social' }, { key: 'cnpj', label: 'CNPJ' }, { key: 'ativo', label: 'Situação' }],
    fields: [{ name: 'pessoaJuridicaId', label: 'Pessoa jurídica', type: 'select', required: true, options: [] }, { name: 'ativo', label: 'Ativo', type: 'checkbox' }],
  },
  marcas: { title: 'Marcas', description: 'Marcas disponíveis para os veículos do estoque.', endpoint: 'admin/marcas', columns: [{ key: 'nome', label: 'Marca' }], fields: [text('nome', 'Nome da marca', true, 80)] },
  veiculos: { title: 'Veículos', description: 'Consulte o estoque. O cadastro de um veículo acontece na entrada de nota fiscal.', endpoint: 'admin/veiculos', columns: [{ key: 'modelo', label: 'Modelo' }, { key: 'marcaNome', label: 'Marca' }, { key: 'anoModelo', label: 'Ano' }, { key: 'status', label: 'Status' }, { key: 'valorVenda', label: 'Preço' }], fields: [] },
  'notas-fiscais': { title: 'Notas fiscais', description: 'Emita notas de entrada ou saída e consulte notas pelo identificador.', endpoint: 'admin/notas-fiscais', columns: [], fields: [] },
  enderecos: {
    title: 'Endereços', description: 'Mantenha os endereços e consulte o CEP quando o serviço estiver disponível.', endpoint: 'admin/enderecos',
    columns: [{ key: 'logradouro', label: 'Logradouro' }, { key: 'bairro', label: 'Bairro' }, { key: 'cidade', label: 'Cidade' }, { key: 'estado', label: 'UF' }, { key: 'cep', label: 'CEP' }],
    fields: [text('cep', 'CEP', true, 9), text('logradouro', 'Logradouro', true, 120), text('numero', 'Número', true, 20), text('complemento', 'Complemento', false, 120), text('bairro', 'Bairro', true, 80), text('cidade', 'Cidade', true, 80), text('estado', 'UF', true, 2)],
  },
};

@Component({
  selector: 'app-admin-workspace',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-workspace.html',
  styleUrl: './admin-workspace.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminWorkspace implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(AdminApi);
  private readonly cepApi = inject(CepApiService);
  private readonly fb = inject(FormBuilder);
  readonly section = this.route.snapshot.data['section'] as Section;
  readonly config = MODULES[this.section];
  readonly rows = signal<Row[]>([]);
  readonly carregando = signal(false);
  readonly salvando = signal(false);
  readonly modalAberto = signal(false);
  readonly erro = signal('');
  readonly sucesso = signal('');
  readonly busca = signal('');
  readonly filtroAno = signal('');
  readonly filtroModelo = signal('');
  readonly filtroMarca = signal('');
  readonly filtroStatus = signal('');
  readonly pessoaJuridica = signal<{ id: number; nome: string }[]>([]);
  readonly pessoasDisponiveis = signal<{ id: number; nome: string; tipoPessoa: string }[]>([]);
  readonly marcas = signal<{ id: number; nome: string }[]>([]);
  readonly resultadoCliente = signal<Row | null>(null);
  readonly pessoaIdCliente = signal('');
  readonly numeroNotaConsulta = signal('');
  readonly notaEncontrada = signal<Row | null>(null);
  readonly detalheVeiculo = signal<Row | null>(null);
  readonly cepAviso = signal('');
  readonly editingId = signal<number | null>(null);
  readonly detalheVeiculoCampos = [
    { key: 'anoFabricacao', label: 'Ano de fabricação' }, { key: 'anoModelo', label: 'Ano modelo' },
    { key: 'cor', label: 'Cor' }, { key: 'placa', label: 'Placa' }, { key: 'chassi', label: 'Chassi' },
    { key: 'renavam', label: 'Renavam' }, { key: 'quilometragem', label: 'Quilometragem' },
    { key: 'status', label: 'Status' }, { key: 'valorCusto', label: 'Custo' }, { key: 'valorVenda', label: 'Preço de venda' },
  ];
  readonly isEntrada = signal(true);
  readonly form = this.fb.group<Record<string, any>>({});
  readonly notaForm = this.fb.group({
    numero: ['', Validators.required], serie: ['', Validators.required], dataEmissao: [new Date().toISOString().slice(0, 10), Validators.required],
    entrada: [true, Validators.required], clienteId: [''], fornecedorId: [''], produtoId: [''], quantidade: [1, [Validators.required, Validators.min(1)]], valorUnitario: ['', [Validators.required, Validators.min(0)]],
    descricao: [''], valorCusto: [''], valorVenda: [''], modelo: [''], anoFabricacao: [''], anoModelo: [''], cor: [''], placa: [''], chassi: [''], renavam: [''], quilometragem: [0], marcaId: [''],
  });
  readonly tituloAcao = computed(() => this.editingId() ? 'Editar cadastro' : 'Novo cadastro');
  readonly filteredRows = computed(() => {
    const term = this.busca().trim().toLocaleLowerCase('pt-BR');
    return !term ? this.rows() : this.rows().filter((row) => Object.values(row).some((value) => String(value ?? '').toLocaleLowerCase('pt-BR').includes(term)));
  });

  ngOnInit(): void {
    if (this.section === 'clientes') { void this.carregarPessoas(); return; }
    if (this.section === 'notas-fiscais') { void this.carregarOpcoes(); return; }
    if (this.section === 'veiculos') { this.carregarVeiculos(); void this.carregarOpcoes(); return; }
    this.criarControles();
    this.carregarLista();
    if (this.section === 'fornecedores' || this.section === 'pessoas') void this.carregarOpcoes();
  }

  private criarControles(): void {
    this.config.fields.forEach((field) => {
      const validators = field.required ? [Validators.required] : [];
      if (field.type !== 'select' && field.type !== 'checkbox' && field.name.toLowerCase().includes('email')) validators.push(Validators.email);
      if (field.minLength) validators.push(Validators.minLength(field.minLength));
      this.form.addControl(field.name, this.fb.control(field.type === 'checkbox' ? false : '', validators));
    });
    if (this.section === 'fornecedores') this.form.get('ativo')?.setValue(true);
    if (this.section === 'usuarios') this.form.get('ativo')?.setValue(true);
  }

  fieldVisible(name: string): boolean {
    if (this.section === 'usuarios') {
      if (this.editingId()) return ['senha', 'perfilId', 'ativo'].includes(name);
      return name !== 'ativo';
    }
    if (this.section === 'pessoas') {
      const tipo = this.form.get('tipoPessoa')?.value;
      if (name === 'cpf' || name === 'dataNascimento') return tipo === 'FISICA';
      if (['cnpj', 'razaoSocial', 'nomeFantasia'].includes(name)) return tipo === 'JURIDICA';
      return true;
    }
    return true;
  }

  private carregarPessoas(): void {
    this.carregando.set(true); this.erro.set('');
    this.api.get<any[]>('admin/pessoas').subscribe({
      next: (pessoas) => {
        this.pessoasDisponiveis.set(pessoas.map((p) => ({ id: p.id, nome: p.nome || p.razaoSocial, tipoPessoa: p.tipoPessoa })));
        this.carregando.set(false);
      }, error: (e) => { this.carregando.set(false); this.erro.set(this.mensagemErro(e, 'Não foi possível carregar pessoas para associação.')); },
    });
  }

  carregarLista(): void {
    this.carregando.set(true); this.erro.set('');
    const query = this.section === 'usuarios' ? { page: 0, pageSize: 100 } : {};
    this.api.get<any>(this.config.endpoint, query).subscribe({
      next: (data) => { this.rows.set(Array.isArray(data) ? data : (data?.items ?? [])); this.carregando.set(false); },
      error: (e) => { this.rows.set([]); this.erro.set(this.mensagemErro(e, 'Não foi possível carregar os registros.')); this.carregando.set(false); },
    });
  }

  private carregarOpcoes(): Promise<void> {
    return new Promise((resolve) => {
      let pending = 0;
      const done = () => { if (--pending <= 0) resolve(); };
      if (['fornecedores', 'pessoas'].includes(this.section)) {
        pending++;
        this.api.get<any[]>('admin/pessoas').subscribe({ next: (pessoas) => {
          this.pessoaJuridica.set(pessoas.filter((p) => p.tipoPessoa === 'JURIDICA').map((p) => ({ id: p.id, nome: p.razaoSocial || p.nome })));
          if (this.section === 'fornecedores') this.config.fields[0].options = this.pessoaJuridica();
          done();
        }, error: () => done() });
      }
      if (this.section === 'notas-fiscais' || this.section === 'veiculos') {
        pending++;
        this.api.get<any[]>('admin/marcas').subscribe({ next: (list) => { this.marcas.set(list); done(); }, error: () => done() });
      }
      if (pending === 0) resolve();
    });
  }

  abrirNovo(): void {
    this.editingId.set(null); this.form.enable(); this.form.reset();
    this.config.fields.forEach((field) => {
      const validators = field.required ? [Validators.required] : [];
      if (field.type !== 'select' && field.type !== 'checkbox' && field.name.toLowerCase().includes('email')) validators.push(Validators.email);
      if (field.minLength) validators.push(Validators.minLength(field.minLength));
      this.form.get(field.name)?.setValidators(validators); this.form.get(field.name)?.updateValueAndValidity({ emitEvent: false });
    });
    if (this.section === 'fornecedores') this.form.get('ativo')?.setValue(true);
    this.modalAberto.set(true); this.erro.set(''); this.sucesso.set('');
  }

  editar(row: Row): void {
    this.editingId.set(row.id ?? null);
    const values: Row = { ...row };
    if (this.section === 'enderecos') { values.cidade = row.cidade; values.estado = row.estado; }
    if (this.section === 'pessoas') {
      values.cep = row.endereco?.cep ?? ''; values.logradouro = row.endereco?.logradouro ?? '';
      values.numero = row.endereco?.numero ?? ''; values.complemento = row.endereco?.complemento ?? '';
      values.bairro = row.endereco?.bairro ?? ''; values.cidade = row.endereco?.cidade ?? ''; values.estado = row.endereco?.estado ?? '';
    }
    if (this.section === 'usuarios') {
      values.perfilId = row.perfis?.[0]?.id ?? 2; values.senha = '';
      for (const field of this.config.fields) {
        const control = this.form.get(field.name);
        if (!control) continue;
        control.clearValidators();
        if (field.name === 'perfilId' || field.name === 'ativo') control.setValidators(Validators.required);
        control.updateValueAndValidity({ emitEvent: false });
      }
    }
    if (this.section === 'pessoas') this.form.get('tipoPessoa')?.disable();
    this.form.reset(values); this.modalAberto.set(true); this.erro.set('');
  }

  salvar(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.salvando()) return;
    const values = this.form.getRawValue() as Row;
    if (this.section === 'pessoas') {
      if (values.tipoPessoa === 'FISICA' && (!values.cpf || !values.dataNascimento)) { this.erro.set('CPF e data de nascimento são obrigatórios para pessoa física.'); return; }
      if (values.tipoPessoa === 'JURIDICA' && (!values.cnpj || !values.razaoSocial)) { this.erro.set('CNPJ e razão social são obrigatórios para pessoa jurídica.'); return; }
      if (values.cep && (!values.logradouro || !values.numero || !values.bairro || !values.cidade || !values.estado)) { this.erro.set('Complete os dados do endereço ou remova o CEP para salvar sem endereço.'); return; }
    }
    const body = this.payload(); this.salvando.set(true); this.erro.set('');
    const id = this.editingId();
    const request = id ? this.api.put(`${this.config.endpoint}/${id}`, body) : this.api.post(this.config.endpoint, body);
    request.subscribe({ next: () => {
      this.salvando.set(false); this.modalAberto.set(false); this.sucesso.set(id ? 'Cadastro atualizado.' : 'Cadastro criado.'); this.carregarLista();
    }, error: (e) => { this.salvando.set(false); this.erro.set(this.mensagemErro(e, 'Não foi possível salvar o cadastro.')); } });
  }

  private payload(): Row {
    const v = this.form.getRawValue() as Row;
    if (this.section === 'pessoas') {
      const endereco = v.cep ? { cep: v.cep, logradouro: v.logradouro, numero: v.numero, complemento: v.complemento || '', bairro: v.bairro, cidade: v.cidade, estado: v.estado } : null;
      return { tipoPessoa: v.tipoPessoa, nome: v.nome, email: v.email, genero: v.genero || '', cpf: v.tipoPessoa === 'FISICA' ? v.cpf : null, dataNascimento: v.tipoPessoa === 'FISICA' ? v.dataNascimento : null, cnpj: v.tipoPessoa === 'JURIDICA' ? v.cnpj : null, razaoSocial: v.tipoPessoa === 'JURIDICA' ? v.razaoSocial : null, nomeFantasia: v.nomeFantasia || '', endereco };
    }
    if (this.section === 'fornecedores') return { pessoaJuridicaId: Number(v.pessoaJuridicaId), ativo: Boolean(v.ativo) };
    if (this.section === 'enderecos') return { cep: v.cep, logradouro: v.logradouro, numero: v.numero, complemento: v.complemento || '', bairro: v.bairro, cidade: v.cidade, estado: v.estado };
    if (this.section === 'usuarios') {
      if (this.editingId()) return { ativo: Boolean(v.ativo), perfis: [Number(v.perfilId)], ...(v.senha ? { senha: v.senha } : {}) };
      return { nome: v.nome, email: v.email, cpf: String(v.cpf ?? '').replace(/\D/g, ''), dataNascimento: v.dataNascimento, genero: '', endereco: '', senha: v.senha, telefones: [{ codigoArea: String(v.codigoArea).replace(/\D/g, ''), numero: String(v.telefone).replace(/\D/g, ''), whatsapp: false }], perfis: [Number(v.perfilId)] };
    }
    return v;
  }

  excluir(row: Row): void {
    if (!row.id || !globalThis.confirm(`Excluir ${this.config.title.toLocaleLowerCase('pt-BR')} selecionado?`)) return;
    this.api.delete(`${this.config.endpoint}/${row.id}`).subscribe({ next: () => { this.sucesso.set('Registro removido.'); this.carregarLista(); }, error: (e) => this.erro.set(this.mensagemErro(e, 'Não foi possível remover. O registro pode estar vinculado a outro dado.')) });
  }

  buscarCliente(): void {
    const cpf = this.busca().replace(/\D/g, '');
    if (cpf.length !== 11) { this.erro.set('Informe um CPF com 11 dígitos.'); return; }
    this.erro.set(''); this.resultadoCliente.set(null); this.carregando.set(true);
    this.api.get<Row>(`admin/clientes/cpf/${cpf}`).subscribe({ next: (row) => { this.resultadoCliente.set(row); this.carregando.set(false); }, error: (e) => { this.erro.set(this.mensagemErro(e, 'Cliente não localizado para esse CPF.')); this.carregando.set(false); } });
  }

  criarCliente(): void {
    const pessoaId = Number(this.pessoaIdCliente());
    if (!pessoaId) { this.erro.set('Selecione uma pessoa para associar como cliente.'); return; }
    this.api.post('admin/clientes', { pessoaId, ativo: true }).subscribe({ next: (row: any) => { this.resultadoCliente.set(row); this.sucesso.set('Cliente associado com sucesso.'); this.erro.set(''); }, error: (e) => this.erro.set(this.mensagemErro(e, 'Não foi possível associar o cliente.')) });
  }

  excluirCliente(): void {
    const id = this.resultadoCliente()?.['id'];
    if (!id || !globalThis.confirm('Remover esta associação de cliente?')) return;
    this.api.delete(`admin/clientes/${id}`).subscribe({ next: () => { this.resultadoCliente.set(null); this.sucesso.set('Associação removida.'); }, error: (e) => this.erro.set(this.mensagemErro(e, 'Cliente com compra registrada não pode ser removido.')) });
  }

  carregarVeiculos(): void {
    this.carregando.set(true); this.erro.set('');
    this.api.get<Row[]>('admin/veiculos', { anoModelo: this.filtroAno(), modelo: this.filtroModelo(), marcaId: this.filtroMarca(), status: this.filtroStatus() }).subscribe({ next: (rows) => { this.rows.set(rows); this.carregando.set(false); }, error: (e) => { this.erro.set(this.mensagemErro(e, 'Não foi possível consultar o estoque.')); this.carregando.set(false); } });
  }

  mostrarVeiculo(row: Row): void { this.api.get<Row>(`admin/veiculos/${row.id}`).subscribe({ next: (v) => this.detalheVeiculo.set(v), error: (e) => this.erro.set(this.mensagemErro(e, 'Não foi possível carregar o veículo.')) }); }

  setTipoNota(entrada: boolean): void { this.isEntrada.set(entrada); this.notaForm.patchValue({ entrada, produtoId: '', valorUnitario: '' }); }

  salvarNota(): void {
    this.notaForm.markAllAsTouched();
    const v = this.notaForm.getRawValue();
    if (this.notaForm.invalid) return;
    if (v.entrada && !v.fornecedorId) { this.erro.set('Informe o ID do fornecedor da nota de entrada.'); return; }
    if (!v.entrada && (!v.clienteId || !v.produtoId)) { this.erro.set('Informe o ID do cliente e do veículo a vender.'); return; }
    if (v.entrada && (!v.descricao || !v.modelo || !v.marcaId || !v.anoFabricacao || !v.anoModelo || !v.cor || !v.chassi || v.valorCusto === '' || v.valorVenda === '')) { this.erro.set('Preencha os dados obrigatórios do veículo e da entrada.'); return; }
    const item: Row = { quantidade: Number(v.quantidade), valorUnitario: Number(v.valorUnitario) };
    if (v.entrada) {
      item.veiculoNovo = { descricao: v.descricao, valorCusto: Number(v.valorCusto), valorVenda: Number(v.valorVenda), modelo: v.modelo, anoFabricacao: Number(v.anoFabricacao), anoModelo: Number(v.anoModelo), cor: v.cor, placa: v.placa || null, chassi: v.chassi, renavam: v.renavam || null, quilometragem: Number(v.quilometragem), marcaId: Number(v.marcaId) };
      item.quantidade = 1;
    } else item.produtoId = Number(v.produtoId);
    const body = { numero: v.numero, serie: v.serie, dataEmissao: v.dataEmissao, entrada: v.entrada, clienteId: v.clienteId ? Number(v.clienteId) : null, fornecedorId: v.fornecedorId ? Number(v.fornecedorId) : null, itens: [item] };
    this.salvando.set(true); this.erro.set('');
    this.api.post<Row>('admin/notas-fiscais', body).subscribe({ next: (nf) => { this.salvando.set(false); this.sucesso.set(`Nota ${nf.numero} criada. ${nf.entrada ? 'Veículo registrado no estoque.' : 'Veículo marcado como vendido.'}`); this.notaEncontrada.set(nf); }, error: (e) => { this.salvando.set(false); this.erro.set(this.mensagemErro(e, 'Não foi possível emitir a nota fiscal.')); } });
  }

  buscarNota(): void {
    const id = Number(this.numeroNotaConsulta()); if (!id) return;
    this.api.get<Row>(`admin/notas-fiscais/${id}`).subscribe({ next: (nf) => { this.notaEncontrada.set(nf); this.erro.set(''); }, error: (e) => { this.notaEncontrada.set(null); this.erro.set(this.mensagemErro(e, 'Nota fiscal não encontrada.')); } });
  }

  excluirNota(): void {
    const id = this.notaEncontrada()?.['id']; if (!id || !globalThis.confirm('Excluir esta nota fiscal? A exclusão pode alterar o estoque do veículo.')) return;
    this.api.delete(`admin/notas-fiscais/${id}`).subscribe({ next: () => { this.notaEncontrada.set(null); this.sucesso.set('Nota excluída; o estoque foi atualizado conforme o tipo da nota.'); }, error: (e) => this.erro.set(this.mensagemErro(e, 'Não foi possível excluir. O veículo pode ter movimentação posterior.')) });
  }

  consultarCep(): void {
    const cep = String(this.form.get('cep')?.value ?? '').replace(/\D/g, '');
    if (cep.length !== 8) return;
    this.cepAviso.set('Consultando CEP…');
    this.cepApi.buscar(cep).subscribe({ next: (data) => {
      if (!data) { this.cepAviso.set('CEP não encontrado. Preencha o endereço manualmente.'); return; }
      this.form.patchValue({ logradouro: data.logradouro, bairro: data.bairro, cidade: data.cidade, estado: data.estado });
      this.cepAviso.set(`Endereço localizado: ${data.cidade} - ${data.estado}. Confira e complete os dados que faltam.`);
    }, error: () => this.cepAviso.set('Consulta indisponível. Preencha os dados do endereço manualmente.') });
  }

  fecharModal(): void { this.modalAberto.set(false); this.cepAviso.set(''); }
  valor(row: Row, key: string): string {
    const value = row[key];
    if (key === 'ativo') return value ? 'Ativo' : 'Inativo';
    if (key === 'status') return String(value ?? '').replaceAll('_', ' ').toLocaleLowerCase('pt-BR');
    if (key === 'perfis' && Array.isArray(value)) return value.map((p) => p.nome ?? p).join(', ');
    if (key === 'valorVenda') return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(Number(value ?? 0));
    return String(value ?? '—');
  }

  mensagemErro(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      const payload = error.error;
      if (typeof payload?.detail === 'string') return payload.detail;
      if (typeof payload === 'string' && payload.trim()) return payload;
      if (error.status === 409) return 'Operação bloqueada porque este registro possui vínculos ou movimentações.';
      if (error.status === 404) return 'Registro não encontrado.';
    }
    return fallback;
  }
}
