package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

public record PsicologoRequestDTO(
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

    @Length(max = 20, message = "O CRP deve conter no máximo 20 caracteres.")
    @NotBlank(message = "O CRP deve ser informado.")
    String crp,

    @Length(max = 120, message = "A especialidade deve conter no máximo 120 caracteres.")
    @NotBlank(message = "A especialidade deve ser informada.")
    String especialidade,

    String bio,

    @DecimalMin(value = "0.0", inclusive = false, message = "O valor da consulta deve ser maior que zero.")
    @NotNull(message = "O valor da consulta deve ser informado.")
    BigDecimal valorConsulta,

    @Positive(message = "A duração da consulta deve ser maior que zero.")
    @NotNull(message = "A duração da consulta deve ser informada.")
    Integer duracaoConsulta,

    @NotNull(message = "O campo ativo deve ser informado.")
    Boolean ativo
) {
}