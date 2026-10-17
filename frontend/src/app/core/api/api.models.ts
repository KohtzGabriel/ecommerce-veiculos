export interface CepResponse {
  cep: string;
  logradouro: string;
  bairro: string;
  municipioId: number;
  municipio: string;
  estado: string;
}

export interface ClienteRequest {
  nome: string;
  email: string;
  telefones: TelefoneRequest[];
  cpf: string;
  dataNascimento: string;
  genero: string;
  endereco: EnderecoRequest;
}

export interface TelefoneRequest {
  codigoArea: string;
  numero: string;
  whatsapp: boolean;
}

export interface EnderecoRequest {
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string;
  bairro: string;
  municipioId: number;
}

export interface ClienteResponse {
  id: number;
  nome: string;
}

export interface ProblemDetails {
  detail?: string;
  errors?: Array<{ field: string; message: string }>;
}
