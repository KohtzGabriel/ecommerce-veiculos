# Autenticação, notas fiscais e veículos — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Implement login JWT para Administrador e Cliente, registrar/estornar veículos junto às notas fiscais, permitir excluir NFs, buscar Cliente por CPF e consultar veículos com filtros, mantendo o mostruário do cliente somente leitura.

**Architecture:** Backend Quarkus usa DTO → Resource → Service → Repository, emite JWT com papéis derivados do usuário e mantém as mudanças de NF/estoque na mesma transação. Angular standalone usa um serviço de sessão em `sessionStorage`, interceptor e guards; o catálogo é somente leitura e recebe apenas campos públicos.

**Tech Stack:** Java 25, Quarkus 3.38, Hibernate ORM/Panache, JWT/SmallRye, PostgreSQL, Angular 21, Reactive Forms, Vitest.

---

## Arquivos e responsabilidades

Backend (`backend/src/main/java/br/unitins/tp2`):

- `dto/AuthRequest.java`, `AuthResponse.java`: contrato de login e resposta sem dados sensíveis.
- `service/AuthService.java`, `AuthServiceImpl.java`: validação de usuário ativo/senha/perfil e emissão do token.
- `resource/AuthResource.java`: `POST /auth/login` público.
- `repository/ClienteRepository.java`, `service/ClienteService.java`, `ClienteServiceImpl.java`, `resource/ClienteAdministrativoResource.java`: consulta Cliente por CPF.
- `repository/VeiculoRepository.java`, `dto/VeiculoResponse.java`, `service/VeiculoService.java`, `VeiculoServiceImpl.java`, `resource/VeiculoAdministrativoResource.java`: leitura por ID e lista com filtros; endpoint público com projeção segura para catálogo.
- `dto/NotaFiscalRequest.java`, `service/NotaFiscalService.java`, `NotaFiscalServiceImpl.java`, `repository/NotaFiscalRepository.java`, `resource/NotaFiscalAdministrativoResource.java`: entrada de veículo, venda, exclusão e reversão atômica.
- `src/main/resources/application.properties`: issuer e localizações de chaves por ambiente, sem incluir chave privada de produção.
- Testes backend correspondentes em `backend/src/test/java/br/unitins/tp2/{service,resource}`; chaves exclusivas de teste ficam em `src/test/resources` somente se necessárias.

Frontend (`frontend/src/app`):

- `core/auth/auth.models.ts`, `auth.service.ts`, `auth.interceptor.ts`, `auth.guards.ts`: contratos, armazenamento em `sessionStorage`, bearer token e proteção por perfil.
- `core/api/api.models.ts` e `auth-api.service.ts`: contrato e HTTP de login.
- `features/login/login.ts/html/scss`: componente compartilhado, configurado para contexto Administrativo ou Cliente pela rota.
- `features/catalogo/catalogo.ts/html/scss`: catálogo público somente leitura.
- `app.routes.ts`, `app.config.ts`: rotas lazy, guards e interceptor funcional.
- Testes Vitest de AuthService, interceptor, guard e login/catálogo.

## [x] Tarefa 1: Autenticação JWT no backend

**Testes:** criar teste de recurso para credenciais corretas, senha incorreta, usuário inativo, perfil ausente e claims `groups` adequados; rodar `cd backend; .\\mvnw.cmd "-Dtest=AuthResourceTest" test -q` e confirmar falha por rota ausente.

**Implementação:** adicionar DTOs de request/response, `AuthService`, `AuthServiceImpl` e `POST /auth/login`. Buscar `Usuario` via `UsuarioRepository.findByLogin`, validar hash com a API de `SenhaService`, não revelar se login ou senha falhou e emitir JWT contendo subject/login e `groups` com nomes `ADMINISTRADOR`/`CLIENTE`, issuer e expiração configurável. Recusar usuário inativo e autenticação para perfil que não está associado ao usuário. Configurar issuer e chave de assinatura de produção via variáveis/paths já previstos, sem segredo versionado. Fazer os testes passarem e executar `cd backend; .\\mvnw.cmd test -q`.

## [x] Tarefa 2: Cliente por CPF e consultas de Veiculo

**Testes:** adicionar testes de endpoint para CPF cadastrado/não cadastrado e consulta de veículo por ID; lista com `anoModelo`, trecho de `modelo`, `marcaId`, `status` independentes e combinados. Executar os testes focados e confirmar falhas por rotas ausentes.

**Implementação:** adicionar `GET /admin/clientes/cpf/{cpf}` sem quebrar GET por ID; normalizar CPF para dígitos e buscar Cliente associado a Pessoa Física. Criar repositório e service de Veiculo; expor `GET /admin/veiculos/{id}` e `GET /admin/veiculos?anoModelo=&modelo=&marcaId=&status=`. Aplicar filtros opcionais em conjunto, validar ano/status e retornar DTO, não entidade JPA. Executar testes focados e suíte backend.

## [x] Tarefa 3: Nota de entrada cria carros e nota de saída vende veículos

**Testes:** atualizar/adicionar testes para que uma nota de entrada com veículo embutido persista Veiculo/Produto e ItemNotaFiscal atomicamente; dados de marca/veículo inválidos não podem deixar NF ou produto órfão. Nota de saída deve mudar Veiculo `DISPONIVEL`/`RESERVADO` para `VENDIDO`; recusar veículo `VENDIDO`; linha de Adicional não muda status.

**Implementação:** expandir o item de `NotaFiscalRequest` para aceitar exatamente uma opção: `produtoId` para produto existente ou dados completos de novo Veículo em nota de entrada (`descricao`, `valorCusto`, `valorVenda`, `modelo`, `anoFabricacao`, `anoModelo`, `cor`, `placa`, `chassi`, `renavam`, `quilometragem`, `marcaId`). Rejeitar `veiculoNovo` em nota de saída e rejeitar produto existente como veículo criado por entrada quando os dados de origem não forem do fluxo previsto. Persistir o veículo antes do item e deixar a transação reverter tudo em erro. Em nota de saída, garantir tipo Veiculo antes da transição para `VENDIDO`; recusar venda duplicada. Preservar total calculado pelos itens. Executar teste focado e suíte backend.

## [x] Tarefa 4: Exclusão de Nota Fiscal com integridade do estoque

**Testes:** DELETE de NF entrada remove a NF, seus itens e veículos criados que não tenham referências posteriores; entrada cujos veículos tenham nota de saída retorna 409 e mantém tudo. DELETE de NF saída remove NF/itens e restaura seus veículos vendidos para `DISPONIVEL`. ID inexistente retorna 404.

**Implementação:** adicionar `delete(long id)` ao service e `DELETE /admin/notas-fiscais/{id}` protegido por `ADMINISTRADOR`. Fazer verificações antes de mutar; para entrada, distinguir produtos criados pela linha `veiculoNovo`, verificar todas as referências além da NF em exclusão e bloquear com 409 se houver uso posterior; remover itens/NF e, em seguida, os Veículos/Produtos novos sem dependências dentro da mesma transação. Para saída, reverter status somente para veículos que pertencem aos itens daquela venda. Garantir flush/ordem de remoção compatível com FKs. Rodar testes focados e suíte backend.

## [x] Tarefa 5: Login Angular, armazenamento JWT e proteção por perfil

**Testes:** AuthService persiste/carrega/remove sessão em `sessionStorage`; interceptor envia `Authorization` quando autenticado, não o envia no login e limpa token em 401; guards permitem o perfil correto e redirecionam perfil incorreto/não autenticado.

**Implementação:** adicionar modelos e `AuthApiService`; `AuthService` com signals para sessão/perfil; interceptor funcional configurado por `provideHttpClient(withInterceptors(...))`; guards funcionais para sessão e role. Criar login standalone com Reactive Forms para `/admin/login` e `/cliente/login`, mostrar erro genérico para credenciais recusadas e navegar apenas quando o JWT contiver o papel esperado. Logout limpa token. Evitar armazenar senha, não confiar no guard como autorização de backend. Rodar testes Vitest focados.

## [x] Tarefa 6: Mostruário somente leitura para Cliente

**Testes:** endpoint público retorna apenas veículos ativos e disponíveis e não inclui `valorCusto`, fornecedor ou dados pessoais; Angular apresenta itens, estado vazio e falha HTTP, sem ações de compra.

**Implementação:** adicionar uma projeção pública reduzida, exposta em `GET /catalogo/veiculos`, separada do endpoint administrativo. Carregar catálogo em `/loja`, com descrição/modelo/ano/cor/marca/preço de venda e status comercial apropriado. Login do perfil Cliente redireciona para `/loja`; não adicionar carrinho, pedido, reserva ou compra. Rodar teste backend e Angular.

## [x] Tarefa 7: Verificação final

- Executar `cd backend; .\\mvnw.cmd test -q` e confirmar código de saída 0, zero falhas/erros.
- Executar `cd frontend; npm test -- --watch=false` e `npm run build`; confirmar zero falhas e build concluído.
- Conferir diff/status do workspace (não é Git checkout); verificar ausência de segredo privado de produção, credenciais ou token real.
- Atualizar este plano com caixas concluídas e atualizar `docs/superpowers/plans/progress.md` com comandos e contagem real de testes.

## Revisão do plano

- As decisões aprovadas na especificação têm tarefas explícitas: JWT, sessão Angular, lookup de CPF, filtros de Veiculo, criação de veículos na entrada, mudança de status na saída e regras de DELETE para ambos os sentidos.
- O mostruário público está incluído por ser o destino do login Cliente e não oferece compra.
- Os nomes de filtros e payload são consistentes: `anoModelo`, `marcaId`, `status`, `produtoId` ou `veiculoNovo`.
- Nenhuma alteração de schema foi prevista; os campos e relacionamentos persistentes já existem. Se testes provarem necessidade de schema novo, parar e criar migration versionada antes de alterar configuração de produção.
