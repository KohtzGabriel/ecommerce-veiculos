# Cadastros administrativos — API de e-commerce de carros

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implementar no backend existente os modelos de cadastro administrativo, operações CRUD e consulta de endereço por CEP com preenchimento manual disponível quando a ViaCEP estiver indisponível.

**Architecture:** Evoluir o módulo Quarkus atual em `backend/`, mantendo o pacote `br.unitins.tp2` e suas camadas `model`, `dto`, `mapper`, `repository`, `service`, `resource` e `exception`. Os resources expõem DTOs; serviços concentram validações e transações; repositórios Panache acessam PostgreSQL; Flyway aplica as mudanças de esquema sem recriar dados. O recurso existente de CEP fica encapsulado atrás de um serviço de consulta, com respostas que permitem ao consumidor continuar manualmente.

**Tech Stack:** Java 25, Quarkus 3.38.3, Maven Wrapper, Hibernate ORM/Panache, Quarkus REST/Jackson, Jakarta Validation, PostgreSQL, Flyway, SmallRye JWT, JUnit e REST Assured. Versões observadas em `backend/pom.xml`.

---

## 1. Escopo desta etapa

### Classes a implementar

- Nota Fiscal e Item Nota Fiscal
- Produto (classe base abstrata), Veículo e Adicional
- Marca e `StatusVeiculo`
- Pessoa (classe base abstrata), Pessoa Física e Pessoa Jurídica
- Cliente, Fornecedor e Usuário
- Endereço
- `Perfil`

“Pessoa” apareceu repetida na solicitação; será implementada uma única vez.

### Requisitos confirmados

- Persistir e expor CRUD simples para os cadastros listados, pela API Quarkus.
- Usar `Pessoa` abstrata, com subclasses PF e PJ. `Cliente` associa-se a uma `Pessoa`, que pode ser PF ou PJ. `Fornecedor` associa-se a PJ.
- Guardar endereço estruturado em `Pessoa`, relacionado à classe/entidade `Endereco`.
- Consultar CEP na API ViaCEP. Resposta válida preenche os campos retornados; falha, timeout, indisponibilidade ou CEP sem resultado deixam o preenchimento dos demais campos disponível manualmente.
- `Perfil` contém `ADMINISTRADOR` e `CLIENTE`; não adicionar `VENDEDOR` nem classe `Vendedor`.
- Nota fiscal é uma classe concreta, com um único atributo obrigatório `boolean entrada`: `true` entrada, `false` saída. Não adicionar enum de entrada/saída, subclasses de nota fiscal ou atributo `saida`.
- Manter nomes de moeda em `BigDecimal`, documentos como `String` e o ID Long herdado de `DefaultEntity`.

### Limites da etapa

Implementar cadastro, consulta, atualização e inativação dos registros, incluindo linhas de `ItemNotaFiscal`. Registros que já tenham referência histórica não serão removidos fisicamente; o DELETE lógico será aplicado quando o modelo oferecer `ativo`, e a API responderá `409 Conflict` para remoções incompatíveis. Endpoints escritos de administração exigirão autenticação de administrador.

Ficam para etapa posterior: carrinho, itens de carrinho, pedido, itens de pedido, checkout, entrega, transições de pedido, pagamentos, integração bancária, cupons, reserva e baixa de estoque comercial, emissão fiscal oficial e integrações SEFAZ. `StatusPedido`, `TipoCartao`, `Pagamento`, `Cartao`, `Pix` e `Boleto` também ficam fora.

`StatusVeiculo` e `Perfil` são enums fixos, não cadastros CRUD. A API fornecerá leitura das opções e os aceitará nos recursos associados; nenhuma rota cria/remove valores de enum. `Produto` é abstrata: seu CRUD será atendido por veículos e adicionais, com listagem conjunta opcional em `/admin/produtos`.

**Nota fiscal nesta etapa:** CRUD de nota e itens com total calculado no servidor e vínculo ao fornecedor para entrada. Como `Pedido` está fora do escopo, a nota de saída fica registrada como documento administrativo com `entrada=false`, sem FK para pedido ou baixa de estoque. A ligação com vendas é uma extensão posterior.

**CEP nesta etapa:** reutilizar o frontend Angular existente. Ele já mantém campos de endereço editáveis quando a busca ViaCEP falha; preservar esse comportamento e verificar o contrato no teste de integração. Não construir um novo frontend ou recriar o cliente ViaCEP.

## 2. Contexto verificado no projeto

Raiz: `C:/Users/GABRIEL/Desktop/Unitins/Topicos 2`.

- `backend/` é a aplicação Quarkus existente e usa pacote base `br.unitins.tp2`, Java 25, Quarkus 3.38.3, Panache, PostgreSQL e H2 de teste.
- `backend/src/main/java/br/unitins/tp2/model/Pessoa.java` atualmente é concreta e guarda CPF, nascimento e endereço em texto. Tem coleção de telefones.
- `Cliente` guarda relações diretas com `Pessoa` e `Endereco` e tem subclasses PF/PJ; essas subclasses precisam ser removidas ao transferir a especialização para Pessoa.
- `Endereco` já tem CEP, logradouro, número, complemento, bairro e vínculo a `Municipio`; Município e Estado já estão no projeto.
- Há cliente ViaCEP em `backend/src/main/java/br/unitins/tp2/client/ViaCepClient.java`; consulta `/ceps/{cep}`, normalização de CEP, mapeamento de indisponibilidade e tratamento de município já existem. Preservar e adaptar o serviço existente.
- Há serviço de hash de senha e modelos/rotas de usuário existentes. A ativação/uso da autenticação administrativa deve aproveitar estes componentes e o suporte JWT já declarado no POM.
- Atualmente o enum `Perfil` contém `ADMINISTRADOR`, `ATENDENTE`, `PACIENTE` e `PSICOLOGO`; será reduzido ao conjunto confirmado nesta especificação. Preservar/sinalizar valores existentes durante migração.
- `DefaultEntity` fornece ID `Long` e timestamps. Não duplicar `dataCadastro` nas entidades que a estendem.
- `backend/src/main/resources/application.properties` atualmente usa `drop-and-create`; substituir configuração normal por migrações antes de executar aplicação contra dados a preservar. Testes H2 podem manter recriação apenas na base em memória.
- A raiz não é um checkout Git. Não assumir commits disponíveis.
- `diagrama-classes-pessoas.puml` é um desenho anterior; as decisões consolidadas nesta especificação prevalecem.

O projeto de referência e os guias foram revistos na etapa anterior: [convenções de pacote do projeto UNITINS](https://github.com/janiojunior/sga-tp2-2026/tree/88741d34af63dc0fc3011a46ae89185d574e273a) e [Panache](https://quarkus.io/guides/hibernate-orm-panache/), [Flyway](https://quarkus.io/guides/flyway/) e [JWT RBAC](https://quarkus.io/guides/security-jwt/).

## 3. Relações e regras de dados

```text
Pessoa (abstract)
├── PessoaFisica (cpf único, dataNascimento)
└── PessoaJuridica (cnpj único, razaoSocial, nomeFantasia)
     ├── Cliente ─────── Usuario (zero ou um usuário por pessoa)
     └── Fornecedor

Pessoa ── 1:1 ── Endereco ── N:1 ── Municipio ── N:1 ── Estado

Produto (abstract)
├── Veiculo ── N:1 Marca; StatusVeiculo
└── Adicional

NotaFiscal (entrada: boolean)
├── 1:N ItemNotaFiscal ── N:1 Produto
└── 0:1 Fornecedor (obrigatório para entrada; ausente para saída nesta etapa)
```

- Usar estratégia JPA `JOINED` para as hierarquias `Pessoa` e `Produto`.
- Uma pessoa pode ter no máximo um Cliente, um Fornecedor quando for PJ e um Usuário. Cliente/fornecedor compartilham a mesma PJ sem duplicar seus dados.
- FK de endereço fica em `Pessoa` (`endereco_id`, única). `Cliente` não mantém outra FK para o endereço.
- Telefone permanece associado à Pessoa, mantendo o modelo atual.
- PF requer CPF e data de nascimento. PJ requer CNPJ e razão social; nome fantasia é opcional. Email, quando preenchido conforme regra atual da API, permanece único. Não criar CPF obrigatório para PJ.
- `Usuario` guarda senha somente em hash e não expõe senha/hash em DTO. Usuário não recebe perfil administrativo por cadastro público.
- `Veiculo` requer Marca; começa como `DISPONIVEL` apenas por ação autorizada. Km e valores não podem ser negativos; ano modelo deve ser maior ou igual ao de fabricação; chassi é único. O plano não cria reserva nem altera disponibilidade por venda.
- `Adicional.quantidadeEstoque` é inteiro não negativo. O CRUD administrativo permite sua alteração por administrador; a futura implantação de notas/estoque deve tornar os movimentos auditáveis.
- `Produto` possui preço de custo, preço de venda, descrição e ativo. Persistir preços com precisão 19, escala 2, validar preços não negativos e rejeitar mais de duas casas decimais.
- `NotaFiscal.entrada` é atributo primitivo obrigatório. DTO de escrita usa `Boolean` com `@NotNull` para rejeitar propriedade ausente ou `null`, depois converte ao booleano persistido.
- `ItemNotaFiscal` exige nota, produto, quantidade positiva e valor unitário com duas casas. O total da nota é calculado pelo servidor a partir dos itens; o JSON não determina total.
- Não executar atualização de estoque ao criar nota fiscal nesta fase; a API só registra a nota. Não existe pedido para compor a nota de saída ainda.
- Enum persistido como `EnumType.STRING`. Não depender de ordinal sujeito à ordem dos valores.
- Atualização integral usa `PUT /{id}`; busca paginada usa `PageResponse` existente e `page`, `pageSize`. IDs desconhecidos retornam 404; validação 400; conflito de unicidade/estado 409; sucesso de criação 201 e `Location`; leitura/alteração 200; remoção 204.

## 4. Contrato de rotas administrativas

Todas as rotas abaixo são prefixadas por `/admin` e exigem JWT válido com perfil `ADMINISTRADOR`, exceto as consultas de enum (`GET` também protegida por administrador nesta etapa). Prefixos correspondem ao recurso, e a resposta sempre é DTO, nunca uma entidade JPA.

`POST /auth/login` é público e retorna o JWT de acesso administrativo/usuário existente; usuário inativo ou senha incorreta recebe 401. Cadastro público de conta está fora desta fase.

| Recurso | Rotas |
|---|---|
| Pessoas PF/PJ | `GET/POST /admin/pessoas`; `GET/PUT/DELETE /admin/pessoas/{id}`. Criar/alterar exige `tipoPessoa=FISICA` ou `JURIDICA`, com campos específicos do tipo. DELETE só quando sem vínculos; caso contrário 409. |
| Endereços | `GET/POST /admin/enderecos`; `GET/PUT/DELETE /admin/enderecos/{id}`. A atualização de endereço principal sempre atualiza a relação de Pessoa; não duplicar endereços. |
| Clientes | `GET/POST /admin/clientes`; `GET/PUT/DELETE /admin/clientes/{id}`. Criação associa `pessoaId` existente; não instancia PF/PJ duplicada. DELETE respeita dependências. |
| Usuários | `GET/POST /admin/usuarios`; `GET/PUT/DELETE /admin/usuarios/{id}`. Operações atribuem somente `ADMINISTRADOR` ou `CLIENTE`; usuário não se associa a mais de uma Pessoa. DELETE de usuário que contenha histórico é desativação lógica. |
| Fornecedores | `GET/POST /admin/fornecedores`; `GET/PUT/DELETE /admin/fornecedores/{id}`. Exige `pessoaJuridicaId`; não duplica PJ. |
| Marcas | `GET/POST /admin/marcas`; `GET/PUT/DELETE /admin/marcas/{id}`. Marca em uso não pode ser fisicamente apagada. |
| Produtos (união) | `GET /admin/produtos` e `GET /admin/produtos/{id}` retornam `tipoProduto` e os campos comuns. Consulta por subtipo e operações de escrita são feitas em `/veiculos` e `/adicionais`. |
| Veículos | `GET/POST /admin/veiculos`; `GET/PUT/DELETE /admin/veiculos/{id}`. Status é aceito/retornado como string enum. Delete lógico por ativo. |
| Adicionais | `GET/POST /admin/adicionais`; `GET/PUT/DELETE /admin/adicionais/{id}`. Delete lógico quando referenciado. |
| Notas fiscais | `GET/POST /admin/notas-fiscais`; `GET/PUT/DELETE /admin/notas-fiscais/{id}`. Filtro de listagem `entrada=true|false`. Notas/itens referenciados não são apagados; documento lançado não pode mudar de entrada para saída. |
| Itens de nota | `GET/POST /admin/notas-fiscais/{notaId}/itens`; `GET/PUT/DELETE /admin/notas-fiscais/{notaId}/itens/{itemId}`. O ID de nota da rota prevalece; item de outra nota dá 404. Alterações recalculam o total na mesma transação. |
| Perfis | `GET /admin/perfis` lista `ADMINISTRADOR`, `CLIENTE`; não há escrita de valores fixos. |
| Situação de veículo | `GET /admin/status-veiculos` lista os valores de `StatusVeiculo`; não há escrita de valores fixos. |
| CEP | `GET /ceps/{cep}` preserva o endpoint existente. Consulta pública para permitir preencher endereço antes de autenticar. |

Para criar ou editar Pessoa, o `tipoPessoa` discrimina o formato. A resposta comum traz `id`, `tipoPessoa`, nome, email, telefones e endereço; o subtipo PF inclui CPF/nascimento e o PJ inclui CNPJ/razão/nome fantasia. Rejeitar campo de subtipo incompatível, em vez de ignorá-lo.

## 5. Comportamento da consulta de CEP

1. Angular envia CEP com ou sem máscara para `GET /ceps/{cep}`.
2. Resource valida e normaliza para oito dígitos e chama uma abstração `CepConsultaService`; o resource não conhece URL, timeout ou implementação ViaCEP.
3. Implementação ViaCEP tem timeouts explícitos de conexão e leitura. Validar indicador de erro e existência de logradouro/bairro retornados; consultar município no banco sem criá-lo silenciosamente.
4. CEP encontrado e município reconhecido: retornar dados estruturados para autopreenchimento. O usuário ainda pode corrigir os campos antes de salvar.
5. CEP inexistente: erro controlado e campos continuam editáveis.
6. Timeout, falha DNS/rede, resposta malformada ou ViaCEP 5xx: resposta `502 application/problem+json` controlada, sem stack trace; campos continuam editáveis e a submissão manual não depende de uma segunda chamada à ViaCEP.
7. Município não encontrado localmente: retornar erro de domínio que identifica o município/estado não mapeado, manter endereço manual editável e não inserir município automaticamente.
8. POST/PUT de endereço exige CEP de oito dígitos, logradouro, número, bairro e `municipioId` existente. O consumidor preenche estes campos manualmente quando a consulta não retorna dados.
9. A API fornece semântica estável em `application/problem+json`; o Angular mostra mensagem de indisponibilidade, deixa os campos abertos e conserva o CEP digitado. Não apagar texto que o usuário já digitou durante uma tentativa malsucedida.

Exemplo de resposta de sucesso:

```json
{
  "cep": "77001000",
  "logradouro": "Praça dos Girassóis",
  "bairro": "Plano Diretor Sul",
  "municipioId": 1,
  "municipio": "Palmas",
  "estado": "TO"
}
```

Exemplo de consulta indisponível mantém o Problem Details que a aplicação já usa; o cliente continua editando o formulário e envia o endereço completo manualmente:

```json
{
  "title": "Serviço de CEP indisponível",
  "status": 502,
  "detail": "Consulte o CEP mais tarde ou preencha o endereço manualmente."
}
```

## 6. Arquivos e responsabilidades

Preservar a estrutura já usada em `backend/src/main/java/br/unitins/tp2/`:

- `model/`: Pessoa, PessoaFisica, PessoaJuridica, Endereco, Cliente, Usuario, Perfil, Fornecedor, Produto, Veiculo, Adicional, Marca, StatusVeiculo, NotaFiscal e ItemNotaFiscal.
- `dto/`: um DTO de criação/atualização e um de resposta para cada aggregate/recurso, com validação Jakarta; DTO Pessoa discriminado por tipo.
- `mapper/`: projeção explícita de modelos, incluindo DTO diferente para campos administrativos de Produto.
- `repository/`: consultas Panache, CPF/CNPJ/email únicos, filtros e paginação.
- `service/`: interfaces e implementações de casos de uso; validações de relações, inativação e limite da resposta de CEP.
- `resource/`: resources REST administrativos e resource CEP existente; autorização e status HTTP.
- `exception/`: erro de conflito e mapeadores para Problem Details.
- `client/ViaCepClient.java`: cliente externo existente, mantido com timeout/error handling.

Arquivos de configuração/documentação/migração a criar ou alterar:

- `backend/pom.xml`
- `backend/src/main/resources/application.properties`
- `backend/src/main/resources/db/migration/V1__baseline_legado.sql`
- `backend/src/main/resources/db/migration/V2__pessoa_fisica_juridica_endereco.sql`
- `backend/src/main/resources/db/migration/V3__perfis.sql`
- `backend/src/main/resources/db/migration/V4__catalogo_fornecedor.sql`
- `backend/src/main/resources/db/migration/V5__nota_fiscal.sql`
- `backend/src/test/resources/application.properties`
- `backend/src/test/resources/import-test.sql`
- `backend/src/test/java/br/unitins/tp2/PostgresTestProfile.java`
- `backend/src/test/java/br/unitins/tp2/resource/PessoaResourceTest.java`
- `backend/src/test/java/br/unitins/tp2/resource/EnderecoResourceTest.java`
- `backend/src/test/java/br/unitins/tp2/resource/CepResourceTest.java`
- `backend/src/test/java/br/unitins/tp2/resource/ClienteResourceTest.java`
- `backend/src/test/java/br/unitins/tp2/resource/UsuarioResourceTest.java`
- `backend/src/test/java/br/unitins/tp2/resource/FornecedorResourceTest.java`
- `backend/src/test/java/br/unitins/tp2/resource/CatalogoResourceTest.java`
- `backend/src/test/java/br/unitins/tp2/resource/NotaFiscalResourceTest.java`
- `backend/src/test/java/br/unitins/tp2/repository/MigracaoPostgresTest.java`
- `docs/api/ecommerce-carros.md`
- `docs/api/migracao-ecommerce.md`

O plano de implementação deve inspecionar a árvore do backend antes de criar testes novos e encaixá-los nos diretórios existentes. Os nomes explicitados acima identificam os arquivos alvo e podem ser ajustados somente para seguir convenção já estabelecida no projeto; manter responsabilidades separadas por camada.

## 7. Etapas de execução

Para cada endpoint, adotar TDD: adicionar teste de contrato, rodar isoladamente para ver a falha esperada, implementar o mínimo, rodar o teste isolado, depois a suíte da etapa. Os comandos são executados dentro de `backend/`. H2 serve para regra/HTTP; migrações, constraints e comportamento de PostgreSQL precisam ser provados com o perfil PostgreSQL em banco de teste descartável.

### Tarefa 1 — Preparar banco, segurança e testes

**Arquivos:** `backend/pom.xml`, `backend/src/main/resources/application.properties`, `backend/src/test/resources/application.properties`, `backend/src/test/resources/import-test.sql`, `backend/src/test/java/br/unitins/tp2/PostgresTestProfile.java`, testes existentes em `backend/src/test/java/br/unitins/tp2/`.

- [ ] Rodar `java -version`, `./mvnw.cmd --version` e `./mvnw.cmd test` em `backend/`; registrar se os testes atuais passam antes de alterar as entidades.
- [ ] Declarar Quarkus Flyway e driver database PostgreSQL compatíveis com o BOM no POM. Aproveitar SmallRye JWT existente; adicionar apenas suporte JWT de teste se a dependência não existir.
- [ ] Adicionar teste que prove que uma rota administrativa sem token responde 401. Rodar `./mvnw.cmd "-Dtest=UsuarioResourceTest" test`; deve falhar com a rota atualmente pública.
- [ ] Configurar autenticação JWT com chave/issuer via ambiente para execução normal, e chaves isoladas em fixtures de teste. Recursos administrativos validam papel ADMINISTRADOR; não deixar segredo ou chave privada de produção no repositório.
- [ ] Desativar `drop-and-create` na configuração normal, restringir qualquer recriação a banco H2 efêmero e desligar carga automática de `import.sql` legado durante os testes de migração. Não iniciar a aplicação contra o banco de desenvolvimento entre essa alteração e a aplicação validada da baseline.
- [ ] Criar `PostgresTestProfile` que lê somente `TEST_DB_URL`, `TEST_DB_USERNAME`, `TEST_DB_PASSWORD`, usa PostgreSQL, schema de teste descartável, `database.generation=validate` e executa Flyway. Se as variáveis não existem, o teste falha claramente sem tentar banco de desenvolvimento.
- [ ] Criar `import-test.sql` para as fixtures pequenas já necessárias à suíte H2. Nenhuma pessoa de demonstração nem conta admin de senha fixa é criada em migração de produção.
- [ ] Acrescentar `ConflictExceptionMapper` retornando 409 com o Problem Details existente; manter 400/404/502 já implementados.
- [ ] Com JWT ativo, verificar que teste de H2 e testes administrativos autenticados continuam executáveis: `./mvnw.cmd test` deve terminar em `BUILD SUCCESS`.

**Aceite:** linha de base registrada, escrita administrativa protegida e nenhuma configuração normal apaga tabelas.

### Tarefa 2 — Migrar Pessoa, subtipos, endereço e cliente

**Modificar:**

- `backend/src/main/java/br/unitins/tp2/model/Pessoa.java`
- `backend/src/main/java/br/unitins/tp2/model/Cliente.java`
- `backend/src/main/java/br/unitins/tp2/model/Endereco.java`
- `backend/src/main/java/br/unitins/tp2/service/ClienteService.java`
- `backend/src/main/java/br/unitins/tp2/service/ClienteServiceImpl.java`
- `backend/src/main/java/br/unitins/tp2/repository/PessoaRepository.java`
- `backend/src/main/java/br/unitins/tp2/mapper/ClienteResponseMapper.java`
- `backend/src/main/java/br/unitins/tp2/resource/ClienteResource.java`

**Criar:**

- `backend/src/main/java/br/unitins/tp2/model/PessoaFisica.java`
- `backend/src/main/java/br/unitins/tp2/model/PessoaJuridica.java`
- `backend/src/main/java/br/unitins/tp2/repository/PessoaFisicaRepository.java`
- `backend/src/main/java/br/unitins/tp2/repository/PessoaJuridicaRepository.java`
- `backend/src/main/java/br/unitins/tp2/dto/PessoaRequestDTO.java`
- `backend/src/main/java/br/unitins/tp2/dto/PessoaResponseDTO.java`
- `backend/src/main/java/br/unitins/tp2/dto/EnderecoRequestDTO.java`
- `backend/src/main/java/br/unitins/tp2/dto/EnderecoResponseDTO.java`
- `backend/src/main/java/br/unitins/tp2/dto/ClienteRequestDTO.java`
- `backend/src/main/java/br/unitins/tp2/dto/ClienteResponseDTO.java`
- `backend/src/main/java/br/unitins/tp2/mapper/PessoaResponseMapper.java`
- `backend/src/main/java/br/unitins/tp2/mapper/EnderecoMapper.java`
- `backend/src/main/java/br/unitins/tp2/service/PessoaService.java`
- `backend/src/main/java/br/unitins/tp2/service/PessoaServiceImpl.java`
- `backend/src/main/java/br/unitins/tp2/service/EnderecoService.java`
- `backend/src/main/java/br/unitins/tp2/service/EnderecoServiceImpl.java`
- `backend/src/main/java/br/unitins/tp2/resource/PessoaResource.java`
- `backend/src/main/java/br/unitins/tp2/resource/EnderecoResource.java`
- `backend/src/main/resources/db/migration/V1__baseline_legado.sql`
- `backend/src/main/resources/db/migration/V2__pessoa_fisica_juridica_endereco.sql`
- `backend/src/test/java/br/unitins/tp2/resource/PessoaResourceTest.java`
- `backend/src/test/java/br/unitins/tp2/repository/PessoaMappingPostgresTest.java`

- [ ] Criar teste de persistência de PessoaFisica com CPF/endereço e PessoaJuridica sem CPF/dataNascimento. Assertar que Hibernate recupera a classe concreta correta e que `pessoa.endereco_id` identifica exatamente um endereço.
- [ ] Rodar `./mvnw.cmd "-Dtest=PessoaMappingPostgresTest" test`; esperado falhar pois as classes/mapeamentos ainda não existem.
- [ ] Criar baseline verificável em `V1` usando DDL do banco PostgreSQL atual antes de alterar entidades. Para banco existente, comparar esquema e contagens, registrar backup e aplicar baseline deliberadamente; não habilitar `baseline-on-migrate` global.
- [ ] Converter Pessoa em `@Entity @Inheritance(strategy = InheritanceType.JOINED) abstract`. Manter campos comuns/email/telefones. Mover CPF e nascimento para PessoaFisica; adicionar CNPJ, razão social e nome fantasia em PessoaJuridica. Manter genero opcional se já consumido pelo frontend.
- [ ] Associar `Pessoa.endereco` por `@OneToOne`, `@JoinColumn(name="endereco_id", unique=true)`. Remover de Cliente sua FK de endereço e seu papel de classe base dos antigos subtipos.
- [ ] Criar V2 com tabela/discriminador JOINED esperado por Hibernate, migrar IDs e registros PF/PJ, copiar `Cliente.id_endereco` para `Pessoa.endereco_id`, e manter tabelas/colunas antigas no banco para conferência. Relaxar `NOT NULL` de colunas CPF/nascimento legadas depois de copiar dados; PJ não pode ser obrigada a preencher colunas PF desmapeadas.
- [ ] Se registros legados não tiverem endereço estruturado válido ou houver classificação PF/PJ ambígua, gerar relatório e interromper a migração antes de obrigar a relação. Não inventar número, CEP, município nem apagar registros para satisfazer constraints.
- [ ] Criar validação de Pessoa no DTO, normalização de CPF/CNPJ como string, unicidade consultiva e constraints no PostgreSQL. Validar município existente ao salvar endereço.
- [ ] Implementar endpoints de Pessoa com `tipoPessoa` validado e resposta compatível. Bloquear alteração do subtipo no PUT. Preservar o payload `POST /clientes` PF que o frontend usa; atualizar Cliente para apontar a Pessoa já persistida, sem criar segunda cópia dos dados.
- [ ] Rodar `./mvnw.cmd "-Dtest=PessoaResourceTest,PessoaMappingPostgresTest,ClienteResourceTest" test` e depois `./mvnw.cmd test`; esperado cadastro/consulta/alteração PF e PJ, unicidade e FK de endereço corretos.

**Aceite:** Pessoa tem endereço associado; Cliente aceita PF ou PJ; registros atuais podem ser migrados sem preencher dados inventados.

### Tarefa 3 — CRUD de endereço e fallback resiliente ViaCEP

**Arquivos:** inspecionar e adaptar `backend/src/main/java/br/unitins/tp2/client/ViaCepClient.java`, `service/CepService.java`, `service/CepServiceImpl.java`, `service/CepNormalizer.java`, `resource/CepResource.java`, `dto/CepResponseDTO.java`, `dto/ViaCepResponseDTO.java`, `exception/CepUnavailableException.java`, `exception/CepUnavailableExceptionMapper.java`, `exception/Problem.java`, `mapper/EnderecoMapper.java`, `service/EnderecoService.java`, `service/EnderecoServiceImpl.java`, `resource/EnderecoResource.java`, `backend/src/main/resources/application.properties` e o formulário Angular já existente.

**Testes:** `backend/src/test/java/br/unitins/tp2/resource/CepResourceTest.java`, `EnderecoResourceTest.java`, e os testes ViaCEP já presentes. Testes Angular existentes em `frontend/src/app/core/api/cep-api.service.spec.ts` e próximos a `frontend/src/app`.

- [ ] Escrever testes com cliente ViaCEP simulado: CEP encontrado preenche resposta; CEP inválido retorna validação; `erro=true` não cria endereço; município ausente não cria município; timeout/5xx retorna 502 Problem Details.
- [ ] Rodar `./mvnw.cmd "-Dtest=CepResourceTest,EnderecoResourceTest" test`; registrar falha do cenário indisponível antes de adicionar fallback, caso a implementação atual não passe.
- [ ] Manter `GET /ceps/{cep}` com normalizador para máscara/sem máscara, timeout de conexão e leitura finitos configurados e tratamento de exceções de rede/conversão. O resource chama a interface `CepService`; não invoca ViaCEP diretamente.
- [ ] Distinguir erro do usuário de indisponibilidade externa. CEP inexistente resulta em erro controlado 404/400 conforme contrato existente; falta de município local gera resposta com campo/mensagem própria; conexão indisponível ou payload inválido gera 502 sem stack trace.
- [ ] Garantir que falha no GET de CEP não bloqueie o `POST /admin/enderecos` ou cadastro PF/PJ: a escrita valida campos manualmente e `municipioId`, sem chamar a ViaCEP. Aceitar logradouro, número, bairro, CEP e município preenchidos pelo usuário.
- [ ] Criar teste do formulário: quando ViaCEP retorna dados, campos recebem valores editáveis; quando responde 502 ou demora, mensagem orienta preenchimento manual, campos seguem habilitados e texto já digitado permanece. Não fazer retry obrigatório durante submit.
- [ ] Criar testes CRUD de endereço: criar em município existente, buscar, PUT e DELETE permitido; endereço associado à Pessoa não é removido fisicamente. Confirmar que atualizar o endereço principal atualiza a relação da Pessoa.
- [ ] Executar `./mvnw.cmd "-Dtest=CepResourceTest,EnderecoResourceTest" test`; executar o teste Angular focal se foi alterado e `npm test -- --watch=false` em `frontend/`. Esperado sucesso nos dois projetos.

**Aceite:** autopreenchimento quando há resposta válida; preenchimento manual utilizável em falha externa; nenhum município criado implicitamente.

### Tarefa 4 — CRUD de Cliente, Usuário, Perfil e Fornecedor

**Modificar:** `backend/src/main/java/br/unitins/tp2/model/Cliente.java`, `model/Usuario.java`, `model/Perfil.java`, `service/ClienteService.java`, `service/ClienteServiceImpl.java`, `service/UsuarioService.java`, `service/UsuarioServiceImpl.java`, `repository/PessoaRepository.java`, `repository/UsuarioRepository.java`, `resource/ClienteResource.java`, `resource/UsuarioResource.java`, `backend/src/main/resources/db/migration/V3__perfis.sql`.

**Criar:** `backend/src/main/java/br/unitins/tp2/model/Fornecedor.java`, `repository/ClienteRepository.java`, `repository/FornecedorRepository.java`, DTOs request/response de Cliente, Usuario e Fornecedor conforme os arquivos da seção 6, mappers dedicados, serviços/interfaces de Fornecedor e resource correspondente; testes `ClienteResourceTest.java`, `UsuarioResourceTest.java` e `FornecedorResourceTest.java` em `backend/src/test/java/br/unitins/tp2/resource/`.

- [ ] Testar os fluxos CRUD como `ADMINISTRADOR`; testar token ausente como 401, perfil CLIENTE como 403 e IDs não existentes como 404.
- [ ] CRUD de Cliente recebe `pessoaId` existente. Não aceita uma representação de Pessoa inline que crie duplicidade. Não apagar a Pessoa quando Cliente for excluído/inativado.
- [ ] CRUD de Usuario associa uma única Pessoa. Hash pela implementação `SenhaService` existente; nem request/response administrativo nem `toString()` de DTO retornam senha/hash. Validar login único e perfil permitido.
- [ ] Reduzir Perfil a `ADMINISTRADOR` e `CLIENTE`. Usar persistência por nome (`EnumType.STRING`) ou conversão com IDs estáveis; V3 mapeia os perfis antigos com relatório explícito e não promove papéis antigos a administrador. Preservar os IDs legados de `ADMINISTRADOR`; mapear `ATENDENTE`, `PACIENTE` ou `PSICOLOGO` a `CLIENTE` somente se a pessoa tiver cadastro de Cliente, e inativar/documentar as demais contas sem as apagar. Atualizar consumers/tests de `Perfil` encontrados por `rg -n 'Perfil\\.' backend/src`. Manter teste para array/conversor se o esquema atual usar array PostgreSQL.
- [ ] Separar `POST /auth/login` da administração, reutilizando verificação de senha e emissão JWT existente/declarada. Login é necessário para acessar os CRUDs. Não criar registro de administrador com senha fixa; documentar bootstrap controlado via hash/segredo de ambiente.
- [ ] `Fornecedor` é entidade própria ligada 1:1 a `PessoaJuridica`. Recebe ID de PJ existente; rejeita Pessoa Física; permite que a PJ também seja Cliente sem clonagem de Pessoa.
- [ ] Fazer consultas em páginas, ordenadas por ID e limitadas a `pageSize` entre 1 e 100. Implementar GET por ID, POST 201+Location, PUT idempotente e DELETE lógico/204 ou 409 quando houver dependências.
- [ ] Testar uniqueness em concorrência no banco por constraints, não só preconsulta: CPF, CNPJ, email e login não podem duplicar.
- [ ] Executar `./mvnw.cmd "-Dtest=ClienteResourceTest,UsuarioResourceTest,FornecedorResourceTest" test` e suíte geral. Aceite: PF/PJ, cliente e fornecedor compartilhando Pessoa; apenas os perfis definidos; resources administrativos protegidos.

### Tarefa 5 — CRUD de marca, produtos, veículos e adicionais

**Criar em `backend/src/main/java/br/unitins/tp2/`:**

- Modelo: `model/Produto.java`, `model/Veiculo.java`, `model/Adicional.java`, `model/Marca.java`, `model/StatusVeiculo.java`.
- Repositórios: `repository/ProdutoRepository.java`, `repository/VeiculoRepository.java`, `repository/AdicionalRepository.java`, `repository/MarcaRepository.java`.
- DTOs: `dto/ProdutoResponseDTO.java`, `dto/VeiculoRequestDTO.java`, `dto/VeiculoResponseDTO.java`, `dto/AdicionalRequestDTO.java`, `dto/AdicionalResponseDTO.java`, `dto/MarcaRequestDTO.java`, `dto/MarcaResponseDTO.java`.
- Mappers: `mapper/ProdutoResponseMapper.java`, `mapper/VeiculoResponseMapper.java`, `mapper/AdicionalResponseMapper.java`, `mapper/MarcaResponseMapper.java`.
- Serviços: `service/ProdutoService.java`, `service/ProdutoServiceImpl.java`, `service/VeiculoService.java`, `service/VeiculoServiceImpl.java`, `service/AdicionalService.java`, `service/AdicionalServiceImpl.java`, `service/MarcaService.java`, `service/MarcaServiceImpl.java`.
- Resources: `resource/ProdutoResource.java`, `resource/VeiculoResource.java`, `resource/AdicionalResource.java`, `resource/MarcaResource.java`.

**Outros arquivos:** `backend/src/main/resources/db/migration/V4__catalogo_fornecedor.sql`, `backend/src/test/java/br/unitins/tp2/resource/CatalogoResourceTest.java`, `backend/src/test/java/br/unitins/tp2/repository/CatalogoPostgresTest.java`.

- [ ] Escrever testes POST/GET/PUT/DELETE para Marca, Veiculo e Adicional, usando JWT admin. Testar IDs de marca inexistentes, campo obrigatório, placa/chassi/código duplicado, preço/quilometragem negativos, ano inconsistente e DELETE em item referenciado.
- [ ] Rodar `./mvnw.cmd "-Dtest=CatalogoResourceTest" test`; esperado 404 nas novas rotas.
- [ ] Implementar Produto abstrata com estratégia JOINED; dados comuns vivem em Produto. Veiculo e Adicional não repetem campos herdados. StatusVeiculo usa valor de enum persistido como string e associação obrigatória com marca.
- [ ] `POST /admin/veiculos` e `POST /admin/adicionais` criam somente o respectivo subtipo; `GET /admin/produtos` apresenta ambos usando `tipoProduto`. Não instanciar nem expor uma tabela de Produto concreta.
- [ ] Implementar Marca CRUD com nome único e estado ativo. Marca referenciada pode ser inativada, mas a exclusão física retorna 409.
- [ ] Implementar campos de Veiculo do diagrama: modelo, anoFabricacao, anoModelo, cor, placa, chassi, renavam, quilometragem, Produto comum e marca/status. Validar números e unicidade de chassi; permitir que placa/renavam ausentes sejam nulos.
- [ ] Implementar Adicional: código único, quantidadeEstoque inteira e não negativa, valores de produto comuns. Se existir endpoint de ajuste de estoque, requer administrador e não aceita número negativo; não criar movimento de estoque nem relacionar ao checkout nesta etapa.
- [ ] Aplicar migrations PostgreSQL e `@Version` nas entidades atualizáveis. Mapeadores públicos não são necessários nesta fase: todos os CRUDs de produto são administrativos e autenticados.
- [ ] Rodar `./mvnw.cmd "-Dtest=CatalogoResourceTest,CatalogoPostgresTest" test`; esperado verdes e constraints únicas/relacionamentos comprovados em PostgreSQL.

### Tarefa 6 — CRUD de nota fiscal e itens, documentação e validação final

**Criar em `backend/src/main/java/br/unitins/tp2/`:**

- `model/NotaFiscal.java`, `model/ItemNotaFiscal.java`.
- `repository/NotaFiscalRepository.java`, `repository/ItemNotaFiscalRepository.java`.
- `dto/NotaFiscalRequestDTO.java`, `dto/NotaFiscalResponseDTO.java`, `dto/ItemNotaFiscalRequestDTO.java`, `dto/ItemNotaFiscalResponseDTO.java`.
- `mapper/NotaFiscalResponseMapper.java`, `mapper/ItemNotaFiscalResponseMapper.java`.
- `service/NotaFiscalService.java`, `service/NotaFiscalServiceImpl.java`, `service/ItemNotaFiscalService.java`, `service/ItemNotaFiscalServiceImpl.java`.
- `resource/NotaFiscalResource.java`, `resource/ItemNotaFiscalResource.java` se itens necessitarem resource próprio; preferir subresource `/{notaId}/itens` para verificar o vínculo.

**Outros arquivos:** `backend/src/main/resources/db/migration/V5__nota_fiscal.sql`, `backend/src/test/java/br/unitins/tp2/resource/NotaFiscalResourceTest.java`, `backend/src/test/java/br/unitins/tp2/model/NotaFiscalTest.java`, `docs/api/ecommerce-carros.md`, `docs/api/migracao-ecommerce.md`, `README.md`, `backend/README.md`.

- [ ] Criar teste de validação que confirma ausência/nulo em `entrada` dá 400, `true` é entrada, `false` é saída, e nenhuma classe enum/subclasse de entrada ou saída existe.
- [ ] Rodar `./mvnw.cmd "-Dtest=NotaFiscalResourceTest,NotaFiscalTest" test`; esperado falhar até implementar os novos resources/modelos.
- [ ] Implementar entidade concreta NotaFiscal com campos `numero`, `serie`, `dataEmissao`, `valorTotal`, `boolean entrada`, Fornecedor opcional e composição de ItemNotaFiscal. Não criar relação com Pedido durante esta etapa.
- [ ] Regras por booleano: entrada requer fornecedor PJ existente e ativo; saída não exige nem recebe fornecedor nesta etapa. DTO usa `@NotNull Boolean entrada`, serviço valida e entidade persiste `boolean` não anulável. Não armazenar `saida` separadamente.
- [ ] Implementar ItemNotaFiscal com nota e produto existentes, quantidade positiva e valor unitário `BigDecimal`. Calcular total no serviço somando subtotais e salvar na mesma transação. Cliente não fornece total calculado.
- [ ] Implementar POST/GET/PUT/DELETE de nota e itens administrativos. Não permitir alterar entrada↔saída depois da criação; item de nota diferente da URL retorna 404; quantidade/preço inválidos 400; número/série duplicados conforme regra de unicidade documentada retornam 409.
- [ ] Não movimentar estoque ao cadastrar nota: estabelecer que nesta primeira versão ela é somente o registro CRUD dos dados fornecidos pelo administrador. Atualizar, se necessário, a documentação para explicar que movimentos serão ligados à futura implementação de compras/vendas.
- [ ] Criar migrations com FKs, índices/constraints e tabelas JOINED. Constraints refletirão escopo desta etapa: entrada exige fornecedor; saída não associa pedido. Executar migrations em PostgreSQL de teste sem `clean`.
- [ ] Atualizar OpenAPI e documentação com endpoints, DTOs PF/PJ, enum fixo, boolean de nota fiscal, erros, endereço manual e códigos 502/404 da ViaCEP.
- [ ] Confirmar que integração Angular existente continua aceitando o contrato `POST /clientes` PF e que consulta de CEP mantém os campos editáveis após falha; execução do build/test apenas se houver mudança no frontend.
- [ ] Executar `./mvnw.cmd test`, `./mvnw.cmd verify` e `npm test -- --watch=false` em `frontend/` se o contrato Angular mudou. Esperado `BUILD SUCCESS` no backend e testes Angular verdes. Executar migração vazia e migração contra uma cópia isolada do esquema legado.
- [ ] Revisar OpenAPI e comparar todos os nomes de classe/enum com a lista consolidada. Verificar ausência de `Vendedor`, `Perfil.VENDEDOR`, `TipoNotaFiscal`, `NotaFiscalEntrada`, `NotaFiscalSaida` e booleano `saida`.

**Aceite:** CRUD das classes deste recorte, busca de CEP resiliente, corpo manual funcional, dados de Pessoa/endereço corretos, permissões administrativas e testes de migração passando.

## 7. Ordem e verificação

| Etapa | Conteúdo | Dependência |
|---|---|---|
| 1 | Banco, auth e testes | Nenhuma |
| 2 | Pessoa PF/PJ, endereço e cliente | 1 |
| 3 | CRUD de endereço e CEP tolerante a falhas | 1–2 |
| 4 | Cliente, usuário e perfil | 2 |
| 5 | Fornecedor, marca e catálogo de produto | 2–4 |
| 6 | Nota fiscal e itens; documentação | 4–5 |

Não executar em paralelo alterações de esquema ou `Pessoa` com seus consumers. Depois de fixar o contrato do modelo, DTOs e testes de resources independentes podem ser desenvolvidos separadamente, com coordenação para evitar editar migrations compartilhadas ao mesmo tempo.

Checklist final:

- [ ] Todos os resources administrativos exigem perfil ADMINISTRADOR.
- [ ] Perfil enum contém somente `ADMINISTRADOR` e `CLIENTE`.
- [ ] `Pessoa` abstrata comporta PF e PJ; `Cliente` não duplica Pessoa/endereço.
- [ ] Endereço estruturado é associado à Pessoa e aceita dados inseridos manualmente.
- [ ] Consulta CEP tem sucesso, CEP inexistente, município não cadastrado, timeout, rede fora e payload inesperado cobertos.
- [ ] Frontend preserva edição manual durante indisponibilidade da ViaCEP.
- [ ] Marca, veículo, adicional, fornecedor, usuário, cliente, pessoa, endereço, nota e itens têm CRUD documentado e coberto.
- [ ] `StatusVeiculo` e `Perfil` têm endpoint de leitura, sem escrita dinâmica.
- [ ] Produto é abstrato e CRUD acontece pelos subtipos veículo/adicional.
- [ ] NotaFiscal concreta persiste somente `boolean entrada`, e total vem dos itens.
- [ ] Nenhuma criação automática de schema apaga dados; PostgreSQL executa migrations reproduzíveis.
- [ ] Testes existentes e suíte focal executados com Maven Wrapper.

## 8. Referências e handoff

- Projeto de referência: [sga-tp2-2026](https://github.com/janiojunior/sga-tp2-2026/tree/88741d34af63dc0fc3011a46ae89185d574e273a).
- [Hibernate ORM com Panache](https://quarkus.io/guides/hibernate-orm-panache/), [Flyway no Quarkus](https://quarkus.io/guides/flyway/), [JWT RBAC no Quarkus](https://quarkus.io/guides/security-jwt/).
- A implementação deve conferir as orientações com o BOM 3.38.3 fixado em `backend/pom.xml`; não mudar a versão do Quarkus como parte desta entrega.
- Este arquivo define apenas a primeira fase administrativa. Implementação de carrinho, pedido, pagamento ou frontend novo necessita de um plano de fase posterior.
