# Cadastro de Cliente com Consulta de CEP — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Entregar um backend Quarkus/PostgreSQL e um frontend Angular para cadastrar uma pessoa cliente, consultar CEP via ViaCEP e persistir o endereço usando `Municipio` e `Estado` estruturados.

**Architecture:** O backend será baseado no repositório de referência SGA TP2, preservando `dto`, `exception`, `mapper`, `model`, `repository`, `resource` e `service`. A consulta ao ViaCEP ficará no backend para centralizar normalização, validação e resolução de `Municipio`/`Estado`; o Angular consumirá apenas os endpoints do backend.

**Tech Stack:** Quarkus 3.38.3, Java 25 conforme o projeto de referência, Hibernate ORM/Panache, PostgreSQL, Bean Validation, REST Client Jackson, Angular CLI/Angular atual, TypeScript, Signals e testes Vitest/Quarkus.

**Spec:** Requisitos da solicitação do usuário nesta conversa; referência estrutural e exemplos em `https://github.com/janiojunior/sga-tp2-2026`.

## Global Constraints

- Preservar a estrutura fixa `dto`, `exception`, `mapper`, `model`, `repository`, `resource`, `service` no backend.
- O endereço persistido deve referenciar `Municipio`; `Municipio` deve referenciar `Estado`; não salvar cidade/UF apenas como texto no endereço.
- O backend deve consultar uma API externa de CEP, inicialmente ViaCEP (`https://viacep.com.br/ws/{cep}/json/`).
- Validar o formulário no Angular e no backend com mensagens por campo.
- Não expor entidades JPA diretamente como contrato de API.
- Não gravar credenciais no código; manter configuração de banco e ViaCEP em propriedades de ambiente/configuração.

## Review Focus

- CEP com máscara, caracteres inválidos ou quantidade diferente de 8 dígitos → teste de validação front/back e resposta `400`.
- CEP inexistente ou ViaCEP retornando `erro: true` → mensagem de não encontrado e nenhuma persistência.
- Município/UF retornados pelo ViaCEP sem registro correspondente em `Municipio`/`Estado` → erro de domínio explícito, sem criar cadastro implícito.
- Falha, timeout ou resposta inválida do ViaCEP → `502`/erro de integração controlado, sem stack trace no contrato público.
- Cliente com campos obrigatórios ausentes, e-mail inválido, CPF inválido ou data futura → bloqueio no front e rejeição independente no back.

### Task 1: Preparar a entrega e fixar contratos

**Files:**
- Create: `backend/` como cópia de trabalho do repositório de referência.
- Create: `frontend/` via Angular CLI, com routing e SCSS.
- Create: `docs/api/cliente-cep.md` com payloads e códigos de erro.
- Test: comandos de baseline do backend e `frontend`.

**Interfaces:**
- Produces: backend em `backend/` e Angular em `frontend/`, ambos executáveis separadamente.

- [ ] **Step 1: Write the failing contract tests**
  - Backend: teste de contrato para `POST /clientes` com endereço estruturado e `GET /ceps/{cep}`.
  - Frontend: teste do componente de cadastro verificando estado inicial inválido e chamada de consulta ao CEP.

- [ ] **Step 2: Run baseline tests**
  - Run: `backend/mvnw.cmd test` e `frontend/npm test -- --run`.
  - Expected: os testes novos falham porque os endpoints/componente ainda não existem.

- [ ] **Step 3: Scaffold the Angular application**
  - Run: `npx ng version`; if no CLI is installed, use `npx @angular/cli@latest`.
  - Create: `frontend` with `--routing --style=scss --interactive=false --ai-config=agent`.

- [ ] **Step 4: Document the API contract**
  - Define `CepResponse`, `EnderecoRequest`, `ClienteRequest`, `ClienteResponse` and error shape `{ field, message }[]`.

### Task 2: Modelar endereço estruturado e gateway ViaCEP

**Files:**
- Modify: `backend/src/main/java/br/unitins/tp2/model/Pessoa.java`.
- Create: `backend/src/main/java/br/unitins/tp2/model/Endereco.java`.
- Modify: `backend/src/main/java/br/unitins/tp2/model/Municipio.java` only if needed to preserve the existing `Estado` relation.
- Create: `backend/src/main/java/br/unitins/tp2/dto/CepResponseDTO.java`.
- Create: `backend/src/main/java/br/unitins/tp2/dto/EnderecoRequestDTO.java`.
- Create: `backend/src/main/java/br/unitins/tp2/dto/EnderecoResponseDTO.java`.
- Create: `backend/src/main/java/br/unitins/tp2/service/CepService.java` and `CepServiceImpl.java`.
- Create: `backend/src/main/java/br/unitins/tp2/service/viacep/ViaCepClient.java`.
- Create: `backend/src/main/java/br/unitins/tp2/service/viacep/ViaCepException.java`.
- Create: `backend/src/main/java/br/unitins/tp2/resource/CepResource.java`.
- Create: `backend/src/main/java/br/unitins/tp2/repository/EnderecoRepository.java`.
- Modify: `backend/pom.xml` and `backend/src/main/resources/application.properties`.
- Test: `backend/src/test/java/br/unitins/tp2/service/CepServiceTest.java` and `CepResourceTest.java`.

**Interfaces:**
- `CepService.buscar(String cep): CepResponseDTO`.
- `CepResource.buscar(@PathParam("cep") String cep): CepResponseDTO` at `GET /ceps/{cep}`.
- `EnderecoRequestDTO(String cep, String logradouro, String numero, String complemento, String bairro, Long municipioId)`; logradouro/bairro/municipio serão preenchidos/conferidos pelo backend.

- [ ] **Step 1: Write failing tests**
  - Valid CEP is normalized before the gateway call and maps ViaCEP `localidade`/`uf` to the existing `Municipio`/`Estado`.
  - Invalid CEP and ViaCEP `erro=true` raise the mapped domain error.
  - Município inexistente for returned UF does not create a new `Municipio`.

- [ ] **Step 2: Run tests to verify RED**
  - Run: `backend/mvnw.cmd -Dtest=CepServiceTest test`.
  - Expected: compilation/test failure because the gateway/service/model are missing.

- [ ] **Step 3: Implement the model and gateway**
  - Add `Endereco` with `cep`, `logradouro`, `numero`, `complemento`, `bairro` and mandatory `@ManyToOne Municipio`.
  - Add `Pessoa.endereco` as a structured one-to-one association without retaining the previous free-text address as the source of truth.
  - Configure a typed REST client with base URL `https://viacep.com.br/ws` and JSON mapping for `cep`, `logradouro`, `bairro`, `localidade`, `uf`, `erro`.

- [ ] **Step 4: Implement normalization and municipality resolution**
  - Normalize `12345-678` to `12345678`, require exactly eight digits, call ViaCEP, resolve `Estado` by `uf`, then `Municipio` by name and state.
  - Return a DTO containing the normalized CEP, address fields, `municipioId`, `municipioNome`, `estadoId`, `estadoNome`, and `estadoSigla`.

- [ ] **Step 5: Run tests to verify GREEN**
  - Run: `backend/mvnw.cmd -Dtest=CepServiceTest,CepResourceTest test`.
  - Expected: all CEP tests pass and no external API is called by unit tests.

### Task 3: Implementar cadastro de Cliente com validação no backend

**Files:**
- Create: `backend/src/main/java/br/unitins/tp2/model/Cliente.java`.
- Create: `backend/src/main/java/br/unitins/tp2/dto/ClienteRequestDTO.java` and `ClienteResponseDTO.java`.
- Create: `backend/src/main/java/br/unitins/tp2/mapper/ClienteResponseMapper.java`.
- Create: `backend/src/main/java/br/unitins/tp2/repository/ClienteRepository.java`.
- Create: `backend/src/main/java/br/unitins/tp2/service/ClienteService.java` and `ClienteServiceImpl.java`.
- Create: `backend/src/main/java/br/unitins/tp2/resource/ClienteResource.java`.
- Modify: `backend/src/main/java/br/unitins/tp2/exception/ValidationExceptionMapper.java` if needed for field errors.
- Test: `backend/src/test/java/br/unitins/tp2/service/ClienteServiceTest.java` and `ClienteResourceTest.java`.

**Interfaces:**
- `POST /clientes` consumes `ClienteRequestDTO` and returns `ClienteResponseDTO` with `201`.
- `GET /clientes/{id}` returns the client and structured address.
- `ClienteRequestDTO` validates name, e-mail, CPF, birth date, at least one telephone, active flag, and nested address.

- [ ] **Step 1: Write failing tests**
  - Valid request persists `Pessoa`, `Endereco`, and references the existing `Municipio`/`Estado`.
  - Missing name/CPF/e-mail/telephone/address, invalid e-mail/CPF, and future birth date are rejected with field messages.
  - Duplicate CPF/e-mail are rejected without partial persistence.

- [ ] **Step 2: Run tests to verify RED**
  - Run: `backend/mvnw.cmd -Dtest=ClienteServiceTest,ClienteResourceTest test`.
  - Expected: failure because `Cliente` and its endpoint do not exist.

- [ ] **Step 3: Implement DTOs, model, mapper, repository, service and resource**
  - Follow the reference `Paciente` flow but use `Cliente` names and nested structured address.
  - Keep all business validation in the service; use Bean Validation annotations on DTOs for request shape.
  - Persist the client in one transaction and return DTOs only.

- [ ] **Step 4: Run tests to verify GREEN**
  - Run: `backend/mvnw.cmd -Dtest=ClienteServiceTest,ClienteResourceTest test`.
  - Expected: all validation and persistence tests pass.

### Task 4: Implementar formulário Angular com consulta de CEP

**Files:**
- Create: `frontend/src/app/features/clientes/cliente-cadastro/cliente-cadastro.ts`.
- Create: `frontend/src/app/features/clientes/cliente-cadastro/cliente-cadastro.html`.
- Create: `frontend/src/app/features/clientes/cliente-cadastro/cliente-cadastro.scss`.
- Create: `frontend/src/app/core/api/cliente-api.ts`.
- Create: `frontend/src/app/core/api/cep-api.ts`.
- Create: `frontend/src/app/core/models/cliente.ts` and `endereco.ts`.
- Modify: `frontend/src/app/app.routes.ts` and global styles/configuration as needed.
- Test: component and API service specs under the same feature/core paths.

**Interfaces:**
- `CepApi.buscar(cep: string): Observable<CepResponse>` or equivalent signal-based async state.
- `ClienteApi.criar(request: ClienteRequest): Observable<ClienteResponse>`.
- Form submission is disabled while invalid or loading; CEP lookup fills read-only `logradouro`, `bairro`, `municipio`, and `estado` fields.

- [ ] **Step 1: Write failing frontend tests**
  - Form starts invalid.
  - CEP lookup normalizes the mask and populates address fields.
  - Invalid CEP displays an accessible field error.
  - Submit is blocked until required fields are valid and sends the nested address payload.

- [ ] **Step 2: Run tests to verify RED**
  - Run: `cd frontend; npm test -- --run`.
  - Expected: failures because the component and services are missing.

- [ ] **Step 3: Implement Angular service and component**
  - Load `angular-developer` references for HTTP, signals, forms, components, naming and testing before editing.
  - Use Angular Signals for loading/result/error state and the project-appropriate form strategy.
  - Use `HttpClient` with a configurable API base URL; never call ViaCEP directly from the browser.
  - Render field-level errors with accessible labels, `aria-invalid`, and an error summary/live region.

- [ ] **Step 4: Run frontend tests and build**
  - Run: `cd frontend; npm test -- --run` and `npx ng build`.
  - Expected: tests pass and production build completes without errors.

### Task 5: Integrar, documentar e verificar a entrega

**Files:**
- Modify: `backend/src/main/resources/application.properties` with environment-safe API configuration.
- Modify: `backend/README.md` with PostgreSQL, backend and test commands.
- Modify: `frontend/README.md` with API URL, install, test and build commands.
- Modify: `docs/api/cliente-cep.md` with examples and error responses.

- [ ] **Step 1: Run the complete backend suite**
  - Run: `backend/mvnw.cmd test`.
  - Expected: all existing and new tests pass.

- [ ] **Step 2: Run the complete frontend suite and build**
  - Run: `cd frontend; npm test -- --run; npx ng build`.
  - Expected: all tests pass and the build succeeds.

- [ ] **Step 3: Perform an integration smoke check**
  - Start PostgreSQL/backend, call `GET /ceps/01001000`, then submit one valid `POST /clientes` using the returned municipality/state IDs.
  - Expected: CEP response is structured, client is persisted, and `GET /clientes/{id}` returns the same structured address.

- [ ] **Step 4: Review for scope and secrets**
  - Confirm no hardcoded production credentials, no direct entity responses, no free-text-only address persistence, and no direct browser call to ViaCEP.

