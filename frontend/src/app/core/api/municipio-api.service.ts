import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface MunicipioOption {
  id: number;
  nome: string;
  estado: { sigla: string };
}

interface MunicipioPage {
  items: MunicipioOption[];
}

@Injectable({ providedIn: 'root' })
export class MunicipioApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8081';

  listar(): Observable<MunicipioPage> {
    return this.http.get<MunicipioPage>(`${this.apiUrl}/municipios?page=0&pageSize=1000`);
  }
}
