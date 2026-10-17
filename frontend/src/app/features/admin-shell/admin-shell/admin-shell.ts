import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-admin-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './admin-shell.html',
  styleUrl: './admin-shell.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminShell {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly menuAberto = signal(false);
  readonly links = [
    { label: 'Visão geral', path: '/admin/home', icon: '⌂' },
    { label: 'Pessoas', path: '/admin/pessoas', icon: '♙' },
    { label: 'Clientes', path: '/admin/clientes', icon: '◎' },
    { label: 'Usuários', path: '/admin/usuarios', icon: '♧' },
    { label: 'Fornecedores', path: '/admin/fornecedores', icon: '▱' },
    { label: 'Marcas', path: '/admin/marcas', icon: '◇' },
    { label: 'Veículos', path: '/admin/veiculos', icon: '▰' },
    { label: 'Notas fiscais', path: '/admin/notas-fiscais', icon: '▤' },
    { label: 'Endereços', path: '/admin/enderecos', icon: '⌖' },
  ];

  sair(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/admin/login');
  }
}
