import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CepResponse } from './api.models';

@Injectable({ providedIn: 'root' })
export class CepApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080';

  buscar(cep: string): Observable<CepResponse> {
    const normalizedCep = cep.replace(/\D/g, '');
    return this.http.get<CepResponse>(`${this.apiUrl}/ceps/${normalizedCep}`);
  }
}
