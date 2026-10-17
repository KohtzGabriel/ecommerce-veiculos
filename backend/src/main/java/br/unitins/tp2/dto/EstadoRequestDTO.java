package br.unitins.tp2.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EstadoRequestDTO(
    @Length(min = 2, max = 60, message = "O campo deve conter entre 2 e 60 caracteres.")
    @NotBlank(message = "O campo nome deve ser informado.")
    String nome,

    @Length(min = 2, max = 2, message = "O campo deve ter 2 caracteres.")
    @NotBlank(message = "O campo sigla deve ser informado.")
    String sigla,

    @NotNull(message = "O campo regiao deve ser informado.")
    Long idRegiao
) {
}