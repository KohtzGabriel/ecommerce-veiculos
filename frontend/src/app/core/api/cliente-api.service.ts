import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ClienteRequest, ClienteResponse } from './api.models';

@Injectable({ providedIn: 'root' })
export class ClienteApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8081';

  cadastrar(cliente: ClienteRequest): Observable<ClienteResponse> {
    return this.http.post<ClienteResponse>(`${this.apiUrl}/clientes`, cliente);
  }
}
