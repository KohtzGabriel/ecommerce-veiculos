package br.unitins.tp2.dto;

import jakarta.validation.constraints.NotNull;

public record ClienteAssociacaoRequest(@NotNull Long pessoaId, boolean ativo) { }
