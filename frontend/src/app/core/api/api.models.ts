export interface CepResponse {
  cep: string;
  logradouro: string;
  bairro: string;
  municipio: string;
  estado: string;
}

export interface ProblemDetails {
  detail?: string;
  errors?: Array<{ field: string; message: string }>;
}
