package br.unitins.tp2.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateDTO(
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @Size(min = 15, max = 128, message = "A senha deve conter entre 15 e 128 caracteres.")
        String senha,

        @NotNull(message = "O campo ativo deve ser informado.")
        Boolean ativo,

        @NotEmpty(message = "Ao menos um perfil deve ser informado.")
        Set<@NotNull @Positive Long> perfis) {

    @Override
    public String toString() {
        return "UsuarioUpdateDTO[senha=[PROTEGIDA], ativo=" + ativo + ", perfis=" + perfis + "]";
    }
}
