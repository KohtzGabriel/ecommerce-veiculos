package br.unitins.tp2.dto;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PessoaAdministrativaRequest(@NotBlank String tipoPessoa, @NotBlank String nome,
        @NotBlank @Email String email, String genero, String cpf, LocalDate dataNascimento,
        String cnpj, String razaoSocial, String nomeFantasia,
        @Valid EnderecoRequestDTO endereco, List<@Valid TelefoneRequestDTO> telefones,
        boolean cadastrarCliente, boolean cadastrarFornecedor, @Valid UsuarioCadastro usuario) {
    public record UsuarioCadastro(
            @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
            @NotBlank @Size(min = 15, max = 128) String senha,
            @NotNull Long perfilId) {
        @Override public String toString() { return "UsuarioCadastro[senha=[PROTEGIDA], perfilId=" + perfilId + "]"; }
    }
}
