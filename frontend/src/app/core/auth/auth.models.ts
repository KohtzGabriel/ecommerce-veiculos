export type AuthRole = 'ADMINISTRADOR' | 'CLIENTE';

export interface AuthCredentials {
  login: string;
  senha: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresAt: string;
  roles: AuthRole[];
}

export interface AuthSession extends AuthResponse {}
