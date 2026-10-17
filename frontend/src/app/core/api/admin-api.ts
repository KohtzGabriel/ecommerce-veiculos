import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../auth/auth-api.service';

@Injectable({
  providedIn: 'root',
})
export class AdminApi {
  private readonly http = inject(HttpClient);

  get<T>(path: string, query: Record<string, string | number | boolean | null | undefined> = {}): Observable<T> {
    let params = new HttpParams();
    for (const [key, value] of Object.entries(query)) {
      if (value !== null && value !== undefined && value !== '') params = params.set(key, String(value));
    }
    return this.http.get<T>(`${API_BASE_URL}/${path.replace(/^\//, '')}`, { params });
  }

  post<T>(path: string, body: unknown): Observable<T> {
    return this.http.post<T>(`${API_BASE_URL}/${path.replace(/^\//, '')}`, body);
  }

  put<T>(path: string, body: unknown): Observable<T> {
    return this.http.put<T>(`${API_BASE_URL}/${path.replace(/^\//, '')}`, body);
  }

  delete<T = void>(path: string): Observable<T> {
    return this.http.delete<T>(`${API_BASE_URL}/${path.replace(/^\//, '')}`);
  }
}
