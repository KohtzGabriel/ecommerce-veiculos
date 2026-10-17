package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record NotaFiscalRequest(
        @NotBlank String numero,
        @NotBlank String serie,
        @NotNull LocalDate dataEmissao,
        @NotNull Boolean entrada,
        Long clienteId,
        Long fornecedorId,
        @NotEmpty List<@Valid Item> itens) {

    public record Item(
            @Positive Long produtoId,
            @NotNull @Positive Integer quantidade,
            @NotNull @PositiveOrZero @Digits(integer = 17, fraction = 2) BigDecimal valorUnitario,
            @Valid VeiculoNovo veiculoNovo) {}

    public record VeiculoNovo(
            @NotBlank @Size(max = 500) String descricao,
            @NotNull @PositiveOrZero @Digits(integer = 17, fraction = 2) BigDecimal valorCusto,
            @NotNull @PositiveOrZero @Digits(integer = 17, fraction = 2) BigDecimal valorVenda,
            @NotBlank @Size(max = 100) String modelo,
            @NotNull @Positive Integer anoFabricacao,
            @NotNull @Positive Integer anoModelo,
            @NotBlank @Size(max = 40) String cor,
            @NotBlank @Size(max = 17) String chassi,
            @Size(max = 10) String placa,
            @Size(max = 11) String renavam,
            @NotNull @PositiveOrZero Integer quilometragem,
            @NotNull @Positive Long marcaId) {}
}
