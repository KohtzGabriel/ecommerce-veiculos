package br.unitins.tp2.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MunicipioRequestDTO(
    @Length(min = 2, max = 60, message = "O campo deve conter entre 2 e 60 caracteres.")
    @NotBlank(message = "O campo nome deve ser informado.")
    String nome,

    @NotNull(message = "O estado deve ser informado.")
    Long idEstado
) {
}