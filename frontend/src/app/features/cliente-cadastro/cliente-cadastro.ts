import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { AbstractControl, FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ClienteApiService } from '../../core/api/cliente-api.service';
import { CepApiService } from '../../core/api/cep-api.service';
import { ClienteRequest, ProblemDetails } from '../../core/api/api.models';
import { MunicipioApiService, MunicipioOption } from '../../core/api/municipio-api.service';

@Component({
  selector: 'app-cliente-cadastro',
  imports: [FormsModule, ReactiveFormsModule],
  templateUrl: './cliente-cadastro.html',
  styleUrl: './cliente-cadastro.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClienteCadastro implements OnInit {
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly cepApi = inject(CepApiService);
  private readonly clienteApi = inject(ClienteApiService);
  private readonly municipioApi = inject(MunicipioApiService);

  protected readonly buscandoCep = signal(false);
  protected readonly statusCep = signal<'idle' | 'success' | 'warning'>('idle');
  protected readonly mensagemCep = signal('');
  protected readonly salvando = signal(false);
  protected readonly mensagemFormulario = signal('');
  protected readonly municipios = signal<MunicipioOption[]>([]);

  protected readonly form = this.formBuilder.group({
    nome: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email]],
    cpf: ['', [Validators.required, Validators.pattern(/^\d{11}$/)]],
    dataNascimento: ['', Validators.required],
    genero: ['', Validators.maxLength(30)],
    telefone: this.formBuilder.group({
      codigoArea: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(3)]],
      numero: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(15)]],
      whatsapp: [false],
    }),
    endereco: this.formBuilder.group({
      cep: ['', [Validators.required, Validators.pattern(/^\d{5}-?\d{3}$/)]],
      logradouro: ['', [Validators.required, Validators.maxLength(120)]],
      numero: ['', [Validators.required, Validators.maxLength(20)]],
      complemento: ['', Validators.maxLength(120)],
      bairro: ['', [Validators.required, Validators.maxLength(80)]],
      municipioId: this.formBuilder.control<number | null>(null, Validators.required),
      municipio: [{ value: '', disabled: true }],
      estado: [{ value: '', disabled: true }],
    }),
  });

  protected get enderecoForm() {
    return this.form.controls.endereco;
  }

  ngOnInit(): void {
    this.municipioApi.listar().subscribe({
      next: (page) => this.municipios.set(page.items),
      error: () => this.municipios.set([]),
    });
  }

  protected buscarCep(): void {
    const cepControl = this.enderecoForm.controls.cep;
    cepControl.markAsTouched();
    if (cepControl.invalid) {
      this.statusCep.set('warning');
      this.mensagemCep.set('Informe um CEP válido para consultar automaticamente.');
      return;
    }

    this.buscandoCep.set(true);
    this.statusCep.set('idle');
    this.mensagemCep.set('Consultando o CEP...');

    this.cepApi.buscar(cepControl.value).subscribe({
      next: (endereco) => {
        this.enderecoForm.patchValue({
          cep: endereco.cep,
          logradouro: endereco.logradouro,
          bairro: endereco.bairro,
          municipioId: endereco.municipioId,
          municipio: endereco.municipio,
          estado: endereco.estado,
        });
        this.atualizarMunicipio(endereco.municipioId);
        this.buscandoCep.set(false);
        this.statusCep.set('success');
        this.mensagemCep.set('Endereço preenchido pela ViaCEP. Confira e complemente os campos.');
      },
      error: (error: unknown) => {
        this.buscandoCep.set(false);
        this.statusCep.set('warning');
        this.mensagemCep.set(this.mensagemDaApi(error));
      },
    });
  }

  protected atualizarMunicipio(id: number | null): void {
    const municipio = this.municipios().find((item) => item.id === id);
    if (municipio) {
      this.enderecoForm.patchValue({ municipio: municipio.nome, estado: municipio.estado.sigla });
    }
  }

  protected cadastrar(): void {
    this.mensagemFormulario.set('');
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      this.mensagemFormulario.set('Revise os campos obrigatórios antes de salvar.');
      return;
    }

    const value = this.form.getRawValue();
    if (value.endereco.municipioId === null) {
      this.mensagemFormulario.set('Informe o município do endereço.');
      return;
    }

    const payload: ClienteRequest = {
      nome: value.nome,
      email: value.email,
      cpf: value.cpf,
      dataNascimento: value.dataNascimento,
      genero: value.genero,
      telefones: [{ ...value.telefone, whatsapp: value.telefone.whatsapp }],
      endereco: {
        cep: value.endereco.cep,
        logradouro: value.endereco.logradouro,
        numero: value.endereco.numero,
        complemento: value.endereco.complemento,
        bairro: value.endereco.bairro,
        municipioId: value.endereco.municipioId,
      },
    };

    this.salvando.set(true);
    this.clienteApi.cadastrar(payload).subscribe({
      next: () => {
        this.salvando.set(false);
        this.mensagemFormulario.set('Cliente cadastrado com sucesso.');
        this.form.reset({
          nome: '', email: '', cpf: '', dataNascimento: '', genero: '',
          telefone: { codigoArea: '', numero: '', whatsapp: false },
          endereco: {
            cep: '', logradouro: '', numero: '', complemento: '', bairro: '',
            municipioId: null, municipio: '', estado: '',
          },
        });
        this.statusCep.set('idle');
        this.mensagemCep.set('');
      },
      error: (error: unknown) => {
        this.salvando.set(false);
        this.mensagemFormulario.set(this.mensagemDaApi(error));
      },
    });
  }

  protected mensagemCampo(controlName: string, group: AbstractControl = this.form): string {
    const control = group.get(controlName);
    if (!control || !control.touched || !control.errors) {
      return '';
    }
    if (control.errors['required']) return 'Campo obrigatório.';
    if (control.errors['email']) return 'Informe um e-mail válido.';
    if (control.errors['pattern']) return 'Informe um valor válido.';
    if (control.errors['minlength']) return 'O valor informado é muito curto.';
    if (control.errors['maxlength']) return 'O valor informado é muito longo.';
    return 'Valor inválido.';
  }

  private mensagemDaApi(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const details = error.error as ProblemDetails | null;
      if (details?.detail) return details.detail;
    }
    return 'A consulta automática está indisponível. Preencha manualmente os dados do endereço.';
  }
}
