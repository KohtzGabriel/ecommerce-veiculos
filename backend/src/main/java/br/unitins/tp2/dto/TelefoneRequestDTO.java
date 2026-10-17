package br.unitins.tp2.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TelefoneRequestDTO(
    @Length(min = 2, max = 3, message = "O código de área deve conter entre 2 e 3 caracteres.")
    @NotBlank(message = "O código de área deve ser informado.")
    String codigoArea,

    @Length(min = 8, max = 15, message = "O número deve conter entre 8 e 15 caracteres.")
    @NotBlank(message = "O número deve ser informado.")
    String numero,

    @NotNull(message = "O campo whatsapp deve ser informado.")
    Boolean whatsapp
) {
}