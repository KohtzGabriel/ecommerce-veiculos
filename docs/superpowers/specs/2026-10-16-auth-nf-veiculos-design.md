# Autenticação, notas fiscais e consultas de veículos — especificação

## Objetivo

Adicionar login para os perfis Administrador e Cliente com JWT persistido durante a sessão do navegador; completar operações administrativas de nota fiscal e consultas de cliente/veículos; e garantir que inclusão/exclusão de notas fiscais mantenha estoque e status de veículos coerentes.

## Estado atual observado

- Angular é standalone e possui apenas a rota de cadastro de Cliente; `provideHttpClient()` está configurado sem interceptor.
- O backend protege várias rotas com `@RolesAllowed`, possui validação JWT configurada em produção, mas não foi encontrada rota de autenticação que emita tokens.
- `Usuario` guarda login, hash de senha, situação ativa e perfis. O cadastro de usuários associa o login ao e-mail da Pessoa.
- `NotaFiscal` tem `entrada`, fornecedor ou cliente e itens vinculados a Produto. A criação atual exige Produto existente; há POST e GET por ID, mas não DELETE.
- `Veiculo` é subtipo de `Produto`, possui identificadores próprios e status `DISPONIVEL`, `RESERVADO` ou `VENDIDO`. Não há resource/repository para consultas REST de veículos.
- Cliente possui GET administrativo por ID, sem busca por CPF.

## Decisões de produto

1. Criar páginas separadas em `/admin/login` e `/cliente/login`, compartilhando a mesma infraestrutura de autenticação. Administrador autenticado vai à gestão; Cliente autenticado vai ao mostruário existente ou à rota configurada para ele. Nenhuma tela de compra, carrinho ou pedidos é incluída.
2. Persistir o JWT em `sessionStorage`, para sobreviver a reloads e ser limpo ao encerrar a sessão da aba. O token seguirá nas chamadas protegidas via interceptor. A aplicação validará o perfil no guard, mas a autorização real continua sendo do backend.
3. O endpoint de login consulta usuário ativo por login/e-mail, valida a senha contra o hash armazenado e emite JWT assinado com identidade, `groups`/roles, issuer e expiração. Cliente e Administrador precisam ter o perfil correspondente; credenciais inválidas/inativas não emitem token. Não aceitar papel escolhido pelo navegador.
4. Em NF de entrada, cada carro é informado individualmente com os campos obrigatórios de Veiculo e os dados de Produto; o backend cria Veiculo/Produto e ItemNotaFiscal na mesma transação e vincula cada veículo criado à nota. Para carros individualizados, cada linha de carro representa uma unidade.
5. Em NF de saída, cada veículo vendido deve ser referenciado como produto existente. Na mesma transação, veículos de linhas de venda passam a `VENDIDO`; tentar vender um já vendido é conflito. Itens de outros subtipos de Produto não alteram status de veículo.
6. Excluir NF de entrada remove os itens e os veículos criados por ela somente se não tiverem sido vendidos ou usados por outra nota. Havendo dependência posterior, retornar HTTP 409 e não alterar a nota nem o estoque.
7. Excluir NF de saída remove nota/itens e retorna a `DISPONIVEL` os veículos cuja venda era representada por aquela nota. A operação deve ser atômica.
8. Adicionar busca de Cliente por CPF e consulta administrativa de veículos por ID/listagem filtrável. Filtros são opcionais e combináveis: ano de fabricação ou modelo (campo/definição explicitada no contrato), modelo textual, marcaId e status.

## API proposta

- `POST /auth/login` — entrada `{ login, senha }`; resposta `{ accessToken, tokenType, expiresAt, perfil/roles }`. Pública, sem aceitar perfil no corpo.
- `GET /admin/clientes/cpf/{cpf}` — busca Cliente associado a Pessoa Física; não encontrado retorna 404.
- `GET /admin/veiculos/{id}` — consulta veículo por ID.
- `GET /admin/veiculos?ano=&modelo=&marcaId=&status=` — lista filtrada; parâmetros podem ser combinados. `ano` deve ser documentado como ano de modelo ou fabricação no contrato final.
- `POST /admin/notas-fiscais` — aceitar Produto existente para linhas comuns e dados de novo veículo para carros em nota de entrada; saída continua referenciando veículo existente. Persiste a nota, itens e transições de status numa única transação.
- `DELETE /admin/notas-fiscais/{id}` — remove NF e itens segundo as regras 6 e 7; conflito é HTTP 409.

## Angular

- `AuthService`: login/logout, leitura de sessão no `sessionStorage`, perfil ativo e redirect pós-login.
- Interceptor HTTP: inclui `Authorization: Bearer <JWT>` somente quando houver sessão; em 401 limpa sessão e manda para login adequado.
- Guards funcionais para sessão e perfil; não considerar guard uma barreira de segurança para dados/ações.
- Login standalone reutilizável com Reactive Forms, erros 400/401, estado carregando e acessibilidade básica; rotas públicas `/admin/login` e `/cliente/login`.
- Manter fluxo do mostruário de leitura para o perfil Cliente; não implementar ações de compra como parte desta alteração.

## Testes e critérios de aceite

- Testes de serviço/endpoint: senha correta/incorreta, usuário inativo, roles corretas, token assinado; CPF existente/inexistente; filtros individuais e combinados de Veiculo.
- NF entrada cria veículo, produto e item atomicamente; erro de validação deixa tudo sem persistir.
- NF saída muda veículos elegíveis para VENDIDO e impede nova venda de veículo já vendido.
- DELETE de entrada remove carro ainda não vendido, mas responde 409 e preserva tudo se carro já estiver em saída; DELETE de saída restaura veículo a DISPONIVEL e apaga NF/itens.
- Testes Angular: formulários, persistência após reconstrução/reload do serviço, interceptor, guards por perfil, logout e erros de login.
- Rodar suíte Maven e testes/build Angular.

## Pontos a manter explícitos

- Configurar chave de assinatura/verificação fora do código e fornecer mecanismo seguro de desenvolvimento/testes; não incluir segredo privado no repositório.
- Há diferenças de schema PostgreSQL possíveis: alterações JPA requerem migrations versionadas. Toda mudança de persistência criada por esta implementação deve ser coberta por migration; não aplicar mudanças diretamente em banco compartilhado.
- O campo `ano` na consulta será definido no DTO/contrato como `anoModelo` (recomendado para pesquisa comercial) ou `anoFabricacao`, sem ambiguidade para o consumidor.
