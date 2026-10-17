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

Se a ViaCEP estiver indisponível, a tela mantém logradouro, número, bairro, cidade e UF editáveis para o preenchimento manual. Cidades/UF são associadas internamente ao endereço; não há endpoints de manutenção geográfica.

## Verificação

```powershell
cd backend
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.2'
./mvnw.cmd test

cd ..\frontend
npm test -- --watch=false
npm run build
```
