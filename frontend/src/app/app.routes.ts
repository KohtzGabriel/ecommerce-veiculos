import { Routes } from '@angular/router';
import { roleGuard } from './core/auth/auth.guards';

export const routes: Routes = [
  {
    path: '', redirectTo: 'loja', pathMatch: 'full',
  },
  {
    path: 'admin/login',
    data: { role: 'ADMINISTRADOR' },
    loadComponent: () => import('./features/login/login').then((component) => component.LoginPage),
  },
  {
    path: 'cliente/login',
    data: { role: 'CLIENTE' },
    loadComponent: () => import('./features/login/login').then((component) => component.LoginPage),
  },
  {
    path: 'admin',
    canActivate: [roleGuard],
    data: { role: 'ADMINISTRADOR' },
    loadComponent: () => import('./features/admin-shell/admin-shell/admin-shell').then((component) => component.AdminShell),
    children: [
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: 'home', loadComponent: () => import('./features/admin-home/admin-home').then((component) => component.AdminHome), title: 'Visão geral | Motore' },
      { path: 'pessoas', data: { section: 'pessoas' }, loadComponent: () => import('./features/admin-workspace/admin-workspace').then((component) => component.AdminWorkspace), title: 'Pessoas | Motore' },
      { path: 'clientes', data: { section: 'clientes' }, loadComponent: () => import('./features/admin-workspace/admin-workspace').then((component) => component.AdminWorkspace), title: 'Clientes | Motore' },
      { path: 'usuarios', data: { section: 'usuarios' }, loadComponent: () => import('./features/admin-workspace/admin-workspace').then((component) => component.AdminWorkspace), title: 'Usuários | Motore' },
      { path: 'fornecedores', data: { section: 'fornecedores' }, loadComponent: () => import('./features/admin-workspace/admin-workspace').then((component) => component.AdminWorkspace), title: 'Fornecedores | Motore' },
      { path: 'marcas', data: { section: 'marcas' }, loadComponent: () => import('./features/admin-workspace/admin-workspace').then((component) => component.AdminWorkspace), title: 'Marcas | Motore' },
      { path: 'veiculos', data: { section: 'veiculos' }, loadComponent: () => import('./features/admin-workspace/admin-workspace').then((component) => component.AdminWorkspace), title: 'Veículos | Motore' },
      { path: 'notas-fiscais', data: { section: 'notas-fiscais' }, loadComponent: () => import('./features/admin-workspace/admin-workspace').then((component) => component.AdminWorkspace), title: 'Notas fiscais | Motore' },
      { path: 'enderecos', data: { section: 'enderecos' }, loadComponent: () => import('./features/admin-workspace/admin-workspace').then((component) => component.AdminWorkspace), title: 'Endereços | Motore' },
    ],
  },
  {
    path: 'loja',
    loadComponent: () => import('./features/catalogo/catalogo').then((component) => component.CatalogoPage),
  },
  { path: '**', redirectTo: 'loja' },
];
