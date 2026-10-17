import { isPlatformBrowser } from '@angular/common';
import { inject, Injectable, PLATFORM_ID, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { AuthApiService } from './auth-api.service';
import { AuthCredentials, AuthRole, AuthSession } from './auth.models';

const SESSION_KEY = 'tp2.auth.session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(AuthApiService);
  private readonly platformId = inject(PLATFORM_ID);
  readonly session = signal<AuthSession | null>(this.readSession());

  login(credentials: AuthCredentials, expectedRole: AuthRole): Observable<AuthSession> {
    return this.api.login(credentials).pipe(tap((response) => {
      if (!response.roles.includes(expectedRole)) {
        this.logout();
        throw new Error('Este usuário não possui acesso a esta área.');
      }
      const session: AuthSession = { ...response };
      this.session.set(session);
      this.writeSession(session);
    }));
  }

  isAuthenticated(): boolean {
    const session = this.session();
    if (!session) return false;
    if (!session.accessToken || Date.parse(session.expiresAt) <= Date.now()) {
      this.logout();
      return false;
    }
    return true;
  }

  hasRole(role: AuthRole): boolean {
    return this.isAuthenticated() && this.session()?.roles.includes(role) === true;
  }

  accessToken(): string | null {
    return this.isAuthenticated() ? this.session()?.accessToken ?? null : null;
  }

  logout(): void {
    this.session.set(null);
    this.storage()?.removeItem(SESSION_KEY);
  }

  private readSession(): AuthSession | null {
    const value = this.storage()?.getItem(SESSION_KEY);
    if (!value) return null;
    try {
      const session = JSON.parse(value) as AuthSession;
      if (typeof session.accessToken !== 'string' || !Array.isArray(session.roles)
          || !Number.isFinite(Date.parse(session.expiresAt))) {
        this.storage()?.removeItem(SESSION_KEY);
        return null;
      }
      if (Date.parse(session.expiresAt) <= Date.now()) {
        this.storage()?.removeItem(SESSION_KEY);
        return null;
      }
      return session;
    } catch {
      this.storage()?.removeItem(SESSION_KEY);
      return null;
    }
  }

  private writeSession(session: AuthSession): void {
    this.storage()?.setItem(SESSION_KEY, JSON.stringify(session));
  }

  private storage(): Storage | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    try { return globalThis.sessionStorage; }
    catch { return null; }
  }
}
