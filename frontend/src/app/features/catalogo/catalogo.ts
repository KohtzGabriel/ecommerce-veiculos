import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { CatalogoApiService, VeiculoCatalogo } from './catalogo-api.service';

@Component({
  selector: 'app-catalogo',
  imports: [CurrencyPipe, RouterLink, ReactiveFormsModule],
  templateUrl: './catalogo.html',
  styleUrl: './catalogo.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CatalogoPage implements OnInit {
  private readonly api = inject(CatalogoApiService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(NonNullableFormBuilder);

  readonly veiculos = signal<VeiculoCatalogo[]>([]);
  readonly carregando = signal(true);
  readonly falha = signal(false);
  readonly filtros = this.fb.group({ modelo: [''], anoModelo: [''], marcaId: [''] });
  readonly logado = computed(() => this.auth.hasRole('CLIENTE'));
  readonly marcas = computed(() => [...new Map(this.veiculos().map((v) => [v.marcaId, v.marca])).entries()]
    .map(([id, nome]) => ({ id, nome })).sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR')));

  sair(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/cliente/login');
  }

  consultar(): void {
    const values = this.filtros.getRawValue();
    this.carregando.set(true); this.falha.set(false);
    this.api.listar({ modelo: values.modelo, anoModelo: values.anoModelo ? Number(values.anoModelo) : null, marcaId: values.marcaId ? Number(values.marcaId) : null }).subscribe({
      next: (veiculos) => { this.veiculos.set(veiculos); this.carregando.set(false); },
      error: () => { this.falha.set(true); this.carregando.set(false); },
    });
  }

  limparFiltros(): void { this.filtros.reset({ modelo: '', anoModelo: '', marcaId: '' }); this.consultar(); }

  imagem(id: number): string {
    const photos = ['/assets/vehicles/coupe-illustrative.png', '/assets/vehicles/suv-illustrative.png', '/assets/vehicles/sedan-illustrative.png'];
    return photos[Math.abs(id) % photos.length];
  }

  ngOnInit(): void {
    this.api.listar().subscribe({
      next: (veiculos) => { this.veiculos.set(veiculos); this.carregando.set(false); },
      error: () => { this.falha.set(true); this.carregando.set(false); },
    });
  }
}
