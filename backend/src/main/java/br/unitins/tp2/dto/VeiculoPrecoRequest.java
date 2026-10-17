package br.unitins.tp2.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record VeiculoPrecoRequest(
        @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal valorVenda) {}
