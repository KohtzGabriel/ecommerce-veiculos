package br.unitins.tp2.dto;
public record PessoaAdministrativaResponse(Long id, String tipoPessoa, String nome, String email,
        String cpf, java.time.LocalDate dataNascimento, String cnpj, String razaoSocial,
        String nomeFantasia, EnderecoResponseDTO endereco) { }
