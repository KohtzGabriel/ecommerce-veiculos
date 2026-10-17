import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../core/auth/auth-api.service';

export interface VeiculoCatalogo {
  id: number;
  marcaId: number;
  descricao: string;
  valorVenda: number;
  modelo: string;
  anoFabricacao: number;
  anoModelo: number;
  cor: string;
  marca: string;
  status: string;
}

@Injectable({ providedIn: 'root' })
export class CatalogoApiService {
  private readonly http = inject(HttpClient);

  listar(filters: { anoModelo?: number | null; modelo?: string; marcaId?: number | null } = {}): Observable<VeiculoCatalogo[]> {
    let params = new HttpParams();
    if (filters.anoModelo) params = params.set('anoModelo', filters.anoModelo);
    if (filters.modelo?.trim()) params = params.set('modelo', filters.modelo.trim());
    if (filters.marcaId) params = params.set('marcaId', filters.marcaId);
    return this.http.get<VeiculoCatalogo[]>(`${API_BASE_URL}/catalogo/veiculos`, { params });
  }
}
