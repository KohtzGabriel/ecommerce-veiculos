package br.unitins.tp2.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(
    @Length(min = 2, max = 120, message = "O nome deve conter entre 2 e 120 caracteres.")
    @NotBlank(message = "O nome deve ser informado.")
    String nome,

    @Email(message = "O e-mail informado é inválido.")
    @NotBlank(message = "O e-mail deve ser informado.")
    String email,

    @NotEmpty(message = "Ao menos um telefone deve ser informado.")
    List<@Valid TelefoneRequestDTO> telefones,

    @Length(min = 11, max = 11, message = "O CPF deve conter 11 caracteres.")
    @NotBlank(message = "O CPF deve ser informado.")
    String cpf,

    @NotNull(message = "A data de nascimento deve ser informada.")
    @Past(message = "A data de nascimento deve ser anterior à data atual.")
    LocalDate dataNascimento,

    @Length(max = 30, message = "O gênero deve conter no máximo 30 caracteres.")
    String genero,

    @Length(max = 255, message = "O endereço deve conter no máximo 255 caracteres.")
    String endereco,

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @NotBlank(message = "A senha deve ser informada.")
        @Size(min = 15, max = 128, message = "A senha deve conter entre 15 e 128 caracteres.")
        String senha,

        @NotEmpty(message = "Ao menos um perfil deve ser informado.")
        Set<@NotNull @Positive Long> perfis) {

    @Override
    public String toString() {
        return "UsuarioRequestDTO[nome=" + nome + ", email=" + email + ", cpf=" + cpf
                + ", senha=[PROTEGIDA], perfis=" + perfis + "]";
    }
}
