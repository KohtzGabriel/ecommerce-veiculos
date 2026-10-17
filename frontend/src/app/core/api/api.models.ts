export interface CepResponse {
  cep: string;
  logradouro: string;
  bairro: string;
  cidade: string;
  estado: string;
}

export interface ProblemDetails {
  detail?: string;
  errors?: Array<{ field: string; message: string }>;
}
