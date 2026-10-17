# Motore — E-commerce de veículos

Aplicação de gestão para uma loja de veículos, com API Java/Quarkus e interface Angular. A área do cliente limita-se ao login e ao mostruário público de veículos; cadastros administrativos são protegidos por JWT.

- `backend/`: API Quarkus, PostgreSQL e integração de consulta de CEP com fallback manual.
- `frontend/`: aplicação Angular para gestão da loja, login e catálogo.

## Backend

Requisitos: Java 25 e PostgreSQL. Configure `DB_USERNAME`, `DB_PASSWORD` e `DB_URL` para apontar para seu banco local.

Para preparar automaticamente o PostgreSQL local e iniciar a aplicação, use:

```powershell
cd backend
./start-dev.ps1
```

O script cria o usuário `topicos2` e o banco `topicos2db` somente se faltarem, e inicia o Quarkus criando/atualizando as tabelas. Pode ser executado a cada inicialização: bancos, usuários e dados existentes são preservados. Requer acesso administrativo pelo usuário PostgreSQL `postgres`; se necessário, informe a senha quando solicitada ou configure `PGPASSWORD`/`pgpass.conf`. Não altera a senha de um usuário já existente. Para apenas preparar o usuário e o banco, use `./start-dev.ps1 -SetupOnly`. Use esse script apenas no ambiente local de desenvolvimento.

Alternativamente, siga a preparação manual:

Na primeira execução com um PostgreSQL vazio, crie o usuário e o banco usados pela configuração local (execute no `psql` como administrador):

```sql
CREATE ROLE topicos2 LOGIN PASSWORD '123456';
CREATE DATABASE topicos2db OWNER topicos2;
```

Se já existirem, não repita a criação. Para criar as tabelas no banco novo, execute uma vez:

```powershell
cd backend
./mvnw.cmd '-Dquarkus.hibernate-orm.schema-management.strategy=update' quarkus:dev
```

Após a inicialização, encerre com `Ctrl+C` e use o comando normal abaixo. A configuração padrão valida as tabelas existentes, mas não as cria. Esses dados de acesso são apenas para desenvolvimento local; para outro banco, configure as variáveis de ambiente acima. Bancos antigos devem seguir a migração descrita em `backend/README.md`.

```powershell
cd backend
./mvnw.cmd quarkus:dev
```

O backend fica em `http://localhost:8080`.

### Perfil de demonstração

Para preparar automaticamente o banco de demonstração e iniciar com as contas abaixo, execute `./start-dev.ps1 -Demo` dentro de `backend`. O comando sem `-Demo` usa o banco padrão, que não recebe essas contas.

Para uma apresentação, o perfil `demo` usa por padrão o banco local `ecommerce_veiculos_demo`, cria/atualiza o esquema nesse banco e insere os dados de exemplo na primeira inicialização. O banco precisa existir no PostgreSQL; use `DEMO_DB_URL`, `DEMO_DB_USERNAME` e `DEMO_DB_PASSWORD` para personalizar a conexão.

```powershell
createdb -U postgres ecommerce_veiculos_demo
cd backend
./mvnw.cmd -Dquarkus.profile=demo quarkus:dev
```

Contas de apresentação: administrador `admin@demo.veiculos.local` / `DemoAdmin@2026`; cliente `cliente@demo.veiculos.local` / `DemoCliente@2026`. O seed inclui fornecedor, cliente, três marcas, três veículos disponíveis e um adicional. O processo é idempotente: reiniciar a API não repete os cadastros. Use apenas com o banco dedicado de demonstração; em outros perfis, a carga fica desativada.

Endpoints principais:

No cadastro de Pessoas, marque os vínculos desejados antes de clicar em **Salvar cadastro**: pessoa física pode ser cliente e usuário; pessoa jurídica pode ser cliente e fornecedor. Ao criar usuário, informe senha de 15 a 128 caracteres e perfil de acesso; o e-mail da pessoa será o login. Pessoa e vínculos são salvos juntos, sem duplicar a pessoa. A edição de uma pessoa mantém seus vínculos existentes; os cadastros separados continuam disponíveis.

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
