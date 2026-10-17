package br.unitins.tp2.dto;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PessoaAdministrativaRequest(@NotBlank String tipoPessoa, @NotBlank String nome,
        @NotBlank @Email String email, String genero, String cpf, LocalDate dataNascimento,
        String cnpj, String razaoSocial, String nomeFantasia,
        @Valid EnderecoRequestDTO endereco, List<@Valid TelefoneRequestDTO> telefones) { }
