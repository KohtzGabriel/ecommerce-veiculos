import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthRole } from '../../core/auth/auth.models';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly role = this.route.snapshot.data['role'] as AuthRole;
  readonly isAdmin = this.role === 'ADMINISTRADOR';
  readonly enviando = signal(false);
  readonly erro = signal('');
  readonly form = this.fb.group({
    login: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
    senha: ['', [Validators.required, Validators.minLength(1)]],
  });

  entrar(): void {
    this.erro.set('');
    this.form.markAllAsTouched();
    if (this.form.invalid || this.enviando()) return;

    this.enviando.set(true);
    this.auth.login(this.form.getRawValue(), this.role).subscribe({
      next: () => {
        this.enviando.set(false);
        void this.router.navigateByUrl(this.isAdmin ? '/admin/home' : '/loja');
      },
      error: (error: unknown) => {
        this.enviando.set(false);
        this.erro.set(error instanceof HttpErrorResponse && error.status === 401
          ? 'Login ou senha inválidos.'
          : error instanceof Error && error.message.includes('acesso')
            ? error.message
            : 'Não foi possível entrar agora. Tente novamente.');
      },
    });
  }
}
