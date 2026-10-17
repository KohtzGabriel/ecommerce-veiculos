package br.unitins.tp2.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EnderecoRequestDTO(
        @NotBlank(message = "O CEP deve ser informado.")
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "O CEP deve conter 8 dígitos.")
        String cep,

        @NotBlank(message = "O logradouro deve ser informado.")
        @Length(max = 120, message = "O logradouro deve conter no máximo 120 caracteres.")
        String logradouro,

        @NotBlank(message = "O número deve ser informado.")
        @Length(max = 20, message = "O número deve conter no máximo 20 caracteres.")
        String numero,

        @Length(max = 120, message = "O complemento deve conter no máximo 120 caracteres.")
        String complemento,

        @NotBlank(message = "O bairro deve ser informado.")
        @Length(max = 80, message = "O bairro deve conter no máximo 80 caracteres.")
        String bairro,

        @NotBlank(message = "A cidade deve ser informada.")
        @Length(max = 80, message = "A cidade deve conter no máximo 80 caracteres.")
        String cidade,

        @NotBlank(message = "A UF deve ser informada.")
        @Pattern(regexp = "[A-Za-z]{2}", message = "A UF deve conter duas letras.")
        String estado) {
}
