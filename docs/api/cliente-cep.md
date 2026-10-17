# API de cliente e CEP

## Consulta de CEP

`GET /ceps/{cep}`

O CEP pode ser enviado com ou sem máscara. A resposta usa o município já cadastrado no banco:

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

Se a ViaCEP estiver indisponível, o endpoint retorna `502` com `application/problem+json`. O frontend então mantém o formulário em modo manual. CEP inexistente ou município não cadastrado retorna erro controlado, sem criar município automaticamente.

## Cadastro de cliente

`POST /clientes`

Exemplo de payload:

```json
{
  "nome": "Maria da Silva",
  "email": "maria@example.com",
  "telefones": [
    { "codigoArea": "63", "numero": "999999999", "whatsapp": true }
  ],
  "cpf": "12345678909",
  "dataNascimento": "1990-01-20",
  "genero": "Feminino",
  "endereco": {
    "cep": "77001000",
    "logradouro": "Praça dos Girassóis",
    "numero": "100",
    "complemento": "",
    "bairro": "Plano Diretor Sul",
    "municipioId": 1
  }
}
```

Validações são aplicadas no Angular e novamente no Quarkus: campos obrigatórios, e-mail, CPF com 11 dígitos, data passada, telefone, CEP, endereço e município existente. CPF e e-mail também são únicos.
