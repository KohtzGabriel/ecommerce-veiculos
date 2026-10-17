# Cadastro de cliente — Quarkus e Angular

O projeto está dividido em:

- `backend/`: API Quarkus com PostgreSQL em desenvolvimento, H2 isolado para testes, cadastro de cliente e consulta de CEP pela ViaCEP.
- `frontend/`: aplicação Angular com formulário reativo, validações e preenchimento automático/manual do endereço.

## Backend

Requisitos: Java 25 e PostgreSQL. O banco padrão do repositório de referência é `topicos2db`, com usuário `topicos2` e senha `123456`.

```powershell
cd backend
./mvnw.cmd quarkus:dev
```

O backend fica em `http://localhost:8080`.

Endpoints principais:

- `GET /ceps/{cep}` consulta a ViaCEP e resolve município/estado estruturados.
- `GET /municipios?page=0&pageSize=1000` lista municípios para o fallback manual.
- `POST /clientes` cadastra o cliente com `Pessoa`, `Endereco` e `Municipio`.

## Frontend

```powershell
cd frontend
npm start
```

O Angular fica em `http://localhost:4200`.

Se a ViaCEP estiver indisponível, a tela informa a indisponibilidade e mantém logradouro, número, bairro e município editáveis para o preenchimento manual.

## Verificação

```powershell
cd backend
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.2'
./mvnw.cmd test

cd ..\frontend
npm test -- --watch=false
npm run build
```
