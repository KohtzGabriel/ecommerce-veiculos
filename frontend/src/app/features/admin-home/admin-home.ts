import { CurrencyPipe, CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminApi } from '../../core/api/admin-api';

interface VehicleRow {
  id: number;
  modelo: string;
  marcaNome: string;
  anoModelo: number;
  status: string;
  valorVenda: number;
}

@Component({
  selector: 'app-admin-home',
  imports: [CommonModule, CurrencyPipe, RouterLink],
  template: `
    <section class="overview">
      <header class="welcome">
        <div><h1>Visão geral</h1><p>Bem-vindo à gestão da loja. Mantenha o estoque e os cadastros em dia.</p></div>
        <a class="primary" routerLink="/admin/notas-fiscais"><span aria-hidden="true">＋</span> Nova nota fiscal</a>
      </header>

      <a class="showroom-banner" routerLink="/admin/veiculos" aria-label="Abrir estoque de veículos">
        <img src="/assets/vehicles/coupe-illustrative.png" alt="" fetchpriority="high">
        <span class="banner-shade"></span>
        <span class="banner-copy"><span>ESTOQUE E OPERAÇÃO</span><strong>O próximo capítulo<br>começa na gestão.</strong><small>Consulte veículos e movimentações da loja <b>→</b></small></span>
      </a>

      <section class="recent" aria-labelledby="recent-heading">
        <header class="recent-heading"><div><h2 id="recent-heading">Veículos do estoque</h2><p>Consulta atual do inventário</p></div><a routerLink="/admin/veiculos">Ver estoque completo <span aria-hidden="true">→</span></a></header>
        @if (loading()) { <p class="loading" role="status">Carregando estoque…</p> }
        @else if (error()) { <p class="message" role="alert">Não foi possível carregar o estoque. Use a navegação para tentar novamente.</p> }
        @else if (vehicles().length === 0) { <p class="message">Ainda não há veículos cadastrados. Registre uma entrada na tela de notas fiscais.</p> }
        @else {
          <div class="table-wrap"><table><thead><tr><th>Veículo</th><th>Ano modelo</th><th>Status</th><th>Preço de venda</th></tr></thead><tbody>
            @for (vehicle of vehicles(); track vehicle.id) {
              <tr><td><strong>{{ vehicle.modelo }}</strong><small>{{ vehicle.marcaNome }}</small></td><td>{{ vehicle.anoModelo }}</td><td><span class="status" [class.sold]="vehicle.status === 'VENDIDO'"><i></i>{{ vehicle.status.replace('_',' ').toLocaleLowerCase('pt-BR') }}</span></td><td class="price">{{ vehicle.valorVenda | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</td></tr>
            }
          </tbody></table></div>
        }
      </section>
      <p class="illustrative-note">Imagem ilustrativa. Os dados do estoque são carregados da API.</p>
    </section>
  `,
  styles: `
    :host { display:block; --green:#0f3d3a; --red:#c61f2a; color:#172421; }
    .overview { max-width:1400px; margin:auto; }
    .welcome { display:flex; align-items:flex-end; justify-content:space-between; gap:24px; margin:0 0 24px; }
    h1 { margin:0; font:500 clamp(2.2rem,3.6vw,3.15rem)/1.05 Georgia,'Times New Roman',serif; letter-spacing:-.04em; }
    .welcome p { margin:10px 0 0; color:#76817b; font-size:.9rem; }
    .primary { display:inline-flex; min-height:44px; align-items:center; gap:8px; padding:0 17px; border-radius:5px; color:white; background:var(--green); font-size:.84rem; font-weight:750; text-decoration:none; transition:transform .16s,background .16s; }
    .primary:hover { background:#14534e; transform:translateY(-1px); }
    .showroom-banner { position:relative; display:block; height:clamp(250px,34vw,390px); overflow:hidden; border-radius:7px; color:white; background:#1b2623; text-decoration:none; }
    .showroom-banner img { width:100%; height:100%; object-fit:cover; object-position:center 56%; }
    .banner-shade { position:absolute; inset:0; background:linear-gradient(90deg,#101715de 0%,#121918a6 36%,transparent 78%); }
    .banner-copy { position:absolute; inset:0 auto 0 0; display:flex; width:min(52%,520px); flex-direction:column; justify-content:center; padding:clamp(24px,4vw,56px); }
    .banner-copy > span { margin-bottom:17px; color:#d8e8df; font-size:.68rem; font-weight:700; letter-spacing:.17em; }
    .banner-copy strong { font:500 clamp(2rem,4.5vw,3.8rem)/1.03 Georgia,'Times New Roman',serif; letter-spacing:-.04em; }
    .banner-copy small { margin-top:24px; color:#e1e9e3; font-size:.8rem; }.banner-copy b { padding-left:8px; color:#d5f0df; }
    .recent { margin-top:34px; padding:0; }
    .recent-heading { display:flex; align-items:flex-end; justify-content:space-between; gap:16px; margin-bottom:14px; }
    .recent-heading h2 { margin:0; font:500 1.55rem/1.2 Georgia,'Times New Roman',serif; }.recent-heading p { margin:5px 0 0; color:#7a8580; font-size:.78rem; }
    .recent-heading a { color:var(--green); font-size:.8rem; font-weight:750; text-decoration:none; }.recent-heading a:hover { text-decoration:underline; }
    .table-wrap { overflow:auto; border:1px solid #e4e8e4; border-radius:6px; background:white; }
    table { width:100%; border-collapse:collapse; text-align:left; font-size:.83rem; }th { padding:12px 15px; border-bottom:1px solid #e4e8e4; color:#7b8580; font-size:.66rem; letter-spacing:.1em; text-transform:uppercase; }td { padding:13px 15px; border-bottom:1px solid #edf0ed; color:#46524b; white-space:nowrap; }tbody tr:last-child td { border:0; }td strong,td small { display:block; }td strong { color:#26332d; }td small { margin-top:4px; color:#7a8580; font-size:.74rem; }.price { color:var(--green); font-weight:750; }
    .status { display:inline-flex; align-items:center; gap:7px; padding:5px 9px; border-radius:40px; color:#256945; background:#eaf5ed; font-size:.72rem; text-transform:capitalize; }.status i { width:7px; height:7px; border-radius:50%; background:currentColor; }.status.sold { color:#a4262e; background:#fff0ef; }
    .loading,.message { margin:0; padding:26px; border:1px solid #e4e8e4; border-radius:6px; color:#6c7871; background:white; text-align:center; font-size:.85rem; line-height:1.5; }
    .illustrative-note { margin:10px 0 0; color:#89938d; font-size:.68rem; }
    @media(max-width:650px) { .welcome { align-items:flex-start; flex-direction:column; }.primary { width:100%; justify-content:center; }.showroom-banner { height:310px; }.banner-copy { width:78%; }.banner-shade { background:linear-gradient(90deg,#101715d9 0%,#1219189c 58%,#12191812 100%); }.recent-heading { align-items:flex-start; flex-direction:column; } }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminHome implements OnInit {
  private readonly api = inject(AdminApi);
  readonly vehicles = signal<VehicleRow[]>([]);
  readonly loading = signal(true);
  readonly error = signal(false);

  ngOnInit(): void {
    this.api.get<VehicleRow[]>('admin/veiculos').subscribe({
      next: (vehicles) => { this.vehicles.set(vehicles.slice(0, 4)); this.loading.set(false); },
      error: () => { this.error.set(true); this.loading.set(false); },
    });
  }
}

