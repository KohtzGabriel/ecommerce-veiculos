# Plano de telas — Gestão da loja de veículos

> Revisado em 2026-10-16 com base nos recursos Quarkus existentes e nas rotas Angular atuais.

## Escopo e premissas

- A aplicação é para gestão de uma loja que vende veículos.
- A área administrativa é protegida pelo perfil ADMINISTRADOR.
- Para cliente, haverá somente login e mostruário público, sem cadastro, compra, carrinho, reserva ou pedidos.
- Ações listadas como disponíveis correspondem às rotas existentes. Itens marcados como pendentes exigem endpoint antes de a tela oferecer a ação; não simular persistência.
- Rotas administrativas devem enviar JWT.

## Rotas Angular existentes

| Caminho | Tela | Acesso |
|---|---|---|
| /admin/login | Login administrativo | Público |
| /admin/home | Início administrativo | JWT + ADMINISTRADOR |
| /cliente/login | Login do cliente | Público |
| /loja | Mostruário de veículos | Público; logout quando cliente autenticado |

As telas de gestão abaixo são necessárias para completar o front-end, mas ainda não possuem rota Angular dedicada.

## Telas

### 1. Login administrativo — /admin/login

- **Formulário:** login e senha; validações obrigatórias, estado de envio e mensagem de falha genérica.
- **POST existente:** POST /auth/login com { login, senha }; resposta contém JWT, expiração e roles.
- **GET/PUT/DELETE:** não se aplicam.
- Ao autenticar, exigir role ADMINISTRADOR e navegar para /admin/home. Sessão JWT permanece em sessionStorage; logout limpa sessão.

### 2. Início administrativo — /admin/home

- **Formulários:** nenhum; atalhos para pessoas, clientes, usuários, fornecedores, marcas, estoque e notas fiscais; logout.
- **GET:** nenhum endpoint agregado de indicadores disponível.
- **POST/PUT/DELETE:** não se aplicam.
- Não apresentar contadores até existir endpoint agregado ou consultas de coleção adequadas.

### 3. Login do cliente — /cliente/login

- **Formulário:** login e senha, validações e mensagem de autenticação inválida.
- **POST existente:** POST /auth/login; aceitar apenas role CLIENTE.
- **GET/PUT/DELETE:** não se aplicam.
- Após autenticar, navegar para /loja. Não incluir cadastro nem recuperação de senha, pois não há rotas correspondentes.

### 4. Mostruário — /loja

- **Formulários:** nenhum. Filtros visuais opcionais por ano-modelo, modelo e marca.
- **GET existente:** GET /catalogo/veiculos?anoModelo=&modelo=&marcaId=; sem filtros retorna veículos ativos e disponíveis. Resposta pública não expõe custo nem identificadores/documentos internos.
- **POST/PUT/DELETE:** nenhum. Sem ações de compra.
- Exibir descrição, modelo, ano, cor, marca e preço de venda, além de estados de carregamento, erro e lista vazia. Se CLIENTE estiver autenticado, oferecer logout.

### 5. Pessoas — PF e PJ

- **Formulário:** tipo PF/PJ; campos comuns nome, e-mail, telefone e endereço; PF: CPF e data de nascimento; PJ: CNPJ, razão social e nome fantasia. Não permitir trocar o subtipo após a criação.
- **GET existente:** GET /admin/pessoas, GET /admin/pessoas/{id}.
- **POST existente:** POST /admin/pessoas.
- **PUT existente:** PUT /admin/pessoas/{id}.
- **DELETE existente:** DELETE /admin/pessoas/{id}; mostrar conflito/validação se estiver vinculada a Cliente.
- Endereço é editado dentro da Pessoa no fluxo usual; consulta de CEP é detalhada na tela Endereços.

### 6. Clientes

- **Formulário:** associar uma Pessoa existente (PF ou PJ) e situação ativa. Fluxo encadeado poderá abrir cadastro de Pessoa antes da associação. Busca por CPF para pessoas físicas.
- **GET existente:** GET /admin/clientes/{id}, GET /admin/clientes/cpf/{cpf}.
- **POST existente:** POST /admin/clientes com referência à Pessoa.
- **PUT:** não existe; não mostrar edição até implementação do endpoint.
- **DELETE existente:** DELETE /admin/clientes/{id}. Se houver compra/nota de saída associada, a API impede a exclusão; manter registro e explicar o conflito.
- **Pendente para lista:** GET de coleção/paginação de Clientes.

### 7. Usuários e perfis

- **Formulário:** login, senha inicial, Pessoa vinculada e perfis permitidos; edição de dados/situação conforme contrato de atualização. Não armazenar nem retornar senha em telas.
- **GET existente:** GET /usuarios?page=&pageSize=, GET /usuarios/{id}, GET /usuarios/nome/{nome}, GET /usuarios/cpf/{cpf}.
- **POST existente:** POST /usuarios.
- **PUT existente:** PUT /usuarios/{id}.
- **DELETE existente:** DELETE /usuarios/{id}.
- **Opções existentes:** GET /admin/perfis; perfis válidos Administrador e Cliente. Não há Vendedor.
- A API usa o prefixo /usuarios (não /admin/usuarios); proteger e autorizar as operações de gestão.

### 8. Fornecedores

- **Formulário:** vincular Pessoa Jurídica existente, situação ativa; exibir CNPJ/razão social.
- **GET existente:** GET /admin/fornecedores, GET /admin/fornecedores/{id}.
- **POST existente:** POST /admin/fornecedores.
- **PUT existente:** PUT /admin/fornecedores/{id}.
- **DELETE existente:** DELETE /admin/fornecedores/{id}; API retorna conflito se houver nota fiscal associada.

### 9. Marcas

- **Formulário:** nome da marca.
- **GET existente:** GET /admin/marcas, GET /admin/marcas/{id}.
- **POST existente:** POST /admin/marcas.
- **PUT existente:** PUT /admin/marcas/{id}.
- **DELETE existente:** DELETE /admin/marcas/{id}; impedir remoção quando houver veículo vinculado.

### 10. Estoque de veículos

- **Formulário de entrada:** integrado ao cadastro de NF de entrada (ver tela 12), com descrição, custo, preço de venda, modelo, anos de fabricação/modelo, cor, placa, chassi, Renavam, quilometragem e marca.
- **GET existente:** GET /admin/veiculos/{id}; GET /admin/veiculos?anoModelo=&modelo=&marcaId=&status=.
- **POST de veículo isolado:** não existe. O veículo é criado junto com ItemNotaFiscal de uma NF de entrada.
- **PUT/DELETE de veículo:** não existem; status e exclusão são controlados pelo ciclo de notas fiscais.
- **Opções existentes:** GET /admin/status-veiculos e GET /admin/marcas.
- A tela deve apresentar filtros combináveis e detalhes do veículo, mas não oferecer edição ou remoção direta.

### 11. Endereços e consulta de CEP

- **Formulário:** CEP, logradouro, número, complemento, bairro e município/UF. Consultar ao sair do campo CEP; quando o serviço falhar ou não encontrar CEP, manter preenchimento manual.
- **GET existentes:** GET /ceps/{cep} (ViaCEP com tratamento de indisponibilidade); GET /admin/enderecos, GET /admin/enderecos/{id}; consultas auxiliares GET /estados e GET /municipios com paginação/filtros por nome.
- **POST existente:** POST /admin/enderecos.
- **PUT existente:** PUT /admin/enderecos/{id}.
- **DELETE existente:** DELETE /admin/enderecos/{id}; endereço usado por Pessoa não pode ser removido.
- Gestão isolada é para manutenção excepcional; no cadastro normal, compor endereço na tela Pessoa.

### 12. Notas fiscais e itens

- **Formulário:** booleano entrada; número, série, data de emissão; fornecedor para entrada ou cliente para saída; itens repetíveis com quantidade e valor unitário.
- **Entrada:** item pode selecionar produto existente ou informar veiculoNovo; o veículo é cadastrado dentro da mesma operação da NF. Campos incluem marca e dados de identificação.
- **Saída:** selecionar veículo existente; a API altera automaticamente seu status para VENDIDO. Não permitir vender veículo já vendido.
- **GET existente:** GET /admin/notas-fiscais/{id}.
- **POST existente:** POST /admin/notas-fiscais cria NF e itens; entrada de veículo cria o veículo junto.
- **DELETE existente:** DELETE /admin/notas-fiscais/{id}. Excluir NF de entrada remove veículos criados por ela somente se não houver uso posterior; caso contrário há conflito. Excluir NF de saída restaura os veículos para DISPONIVEL.
- **PUT:** não existe; NFs são imutáveis pela API atual.
- **Pendente para lista/pesquisa:** GET de coleção de NFs. Até sua implementação, tela pode consultar por identificador, não exibir tabela fictícia.

### 13. Adicionais

- **Formulário planejado:** código, descrição, custo, preço de venda, quantidade em estoque e ativo.
- **GET/POST/PUT/DELETE:** não há recurso/rotas administrativos para Adicional na implementação atual.
- A tela CRUD de adicionais depende da criação desses endpoints. Não confundir com estoque de veículos nem fingir que itens de NF fornecem CRUD completo de produto.

## Navegação administrativa proposta

    /admin/login
    /admin/home
    /admin/pessoas
    /admin/clientes
    /admin/usuarios
    /admin/fornecedores
    /admin/marcas
    /admin/veiculos
    /admin/notas-fiscais
    /admin/enderecos (manutenção excepcional)
    /admin/adicionais (após implementação da API)

A navegação não deve renderizar links para telas ainda indisponíveis; até serem construídas, manter apenas o que existe no front-end: login, home, login cliente e catálogo.

## Dependências de API para experiência completa

1. GET de coleção de Clientes (para tabela e pesquisa além do lookup por CPF).
2. GET de coleção de Notas Fiscais (para histórico/listagem).
3. CRUD de Adicional e, se desejado, de produto isolado.
4. PUT de Cliente, caso a gestão precise editar situação/vínculo.
5. Endpoints de métricas somente se o painel precisar mostrar contagens/indicadores.

Não são dependências: entrada de veículo (já é feita junto à NF), transição para vendido (automática na NF de saída), exclusão de NF, lookup de Cliente por CPF, filtros e detalhe de veículo.

## Critérios para implementar o front-end

- Formulários e ações usam os DTOs reais; a tela não inventa campos para substituir contratos.
- Ocultar POST/PUT/DELETE que não existam no recurso indicado.
- Mostrar erros de validação, autorização, not-found e conflito (400/401/403/404/409) em linguagem de usuário.
- JWT administrativo deve ser enviado nas rotas protegidas e sessão deve terminar no logout/expiração.
- Cliente só acessa login e catálogo; o catálogo jamais exibe custo ou ações de compra.

