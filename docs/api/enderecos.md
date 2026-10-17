# Endereços e consulta de CEP

## Consulta de CEP

`GET /ceps/{cep}` consulta a ViaCEP e retorna os campos disponíveis:

```json
{
  "cep": "77001000",
  "logradouro": "Praça dos Girassóis",
  "bairro": "Plano Diretor Sul",
  "municipio": "Palmas",
  "estado": "TO"
}
```

Se a ViaCEP estiver indisponível, a API responde com erro controlado e o formulário deve permitir que o usuário preencha o endereço manualmente.

## Cadastro de endereço

Os endpoints administrativos de pessoa/endereço recebem `cidade` e `estado` (UF) no objeto de endereço. Exemplo:

```json
{
  "cep": "77001000",
  "logradouro": "Praça dos Girassóis",
  "numero": "100",
  "complemento": "",
  "bairro": "Plano Diretor Sul",
  "cidade": "Palmas",
  "estado": "TO"
}
```

O backend associa ou cria os registros geográficos internamente. Não há rotas REST para pacientes, psicólogos, estados, municípios ou regiões.
