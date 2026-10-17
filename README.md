# Motore — E-commerce de veículos

Aplicação de gestão para uma loja de veículos, com API Java/Quarkus e interface Angular. A área do cliente limita-se ao login e ao mostruário público de veículos; cadastros administrativos são protegidos por JWT.

- `backend/`: API Quarkus, PostgreSQL e integração de consulta de CEP com fallback manual.
- `frontend/`: aplicação Angular para gestão da loja, login e catálogo.

## Backend

Requisitos: Java 25 e PostgreSQL. Configure `DB_USERNAME`, `DB_PASSWORD` e `DB_URL` para apontar para seu banco local.

```powershell
cd backend
./mvnw.cmd quarkus:dev
```

O backend fica em `http://localhost:8080`.

### Perfil de demonstração

Para uma apresentação, o perfil `demo` usa por padrão o banco local `ecommerce_veiculos_demo`, cria/atualiza o esquema nesse banco e insere os dados de exemplo na primeira inicialização. O banco precisa existir no PostgreSQL; use `DEMO_DB_URL`, `DEMO_DB_USERNAME` e `DEMO_DB_PASSWORD` para personalizar a conexão.

```powershell
createdb -U postgres ecommerce_veiculos_demo
cd backend
./mvnw.cmd -Dquarkus.profile=demo quarkus:dev
```

Contas de apresentação: administrador `admin@demo.veiculos.local` / `DemoAdmin@2026`; cliente `cliente@demo.veiculos.local` / `DemoCliente@2026`. O seed inclui fornecedor, cliente, três marcas, três veículos disponíveis e um adicional. O processo é idempotente: reiniciar a API não repete os cadastros. Use apenas com o banco dedicado de demonstração; em outros perfis, a carga fica desativada.

Endpoints principais:

- `POST /auth/login` autentica e retorna JWT.
- `GET /catalogo/veiculos` lista veículos disponíveis.
- Rotas de gestão ficam sob `/admin` e exigem perfil `ADMINISTRADOR`.
- `GET /ceps/{cep}` consulta a ViaCEP; indisponibilidade não impede o preenchimento manual.

## Frontend

```powershell
cd frontend
npm start
```

O Angular fica em `http://localhost:4200`.

Se a ViaCEP estiver indisponível, a tela mantém logradouro, número, bairro, cidade e UF editáveis para o preenchimento manual. Cidade e UF são persistidas diretamente no endereço.

## Verificação

```powershell
cd backend
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.2'
./mvnw.cmd test

cd ..\frontend
npm test -- --watch=false
npm run build
```
