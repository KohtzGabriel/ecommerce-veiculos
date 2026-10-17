package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NotaFiscalResponse(Long id, String numero, String serie, LocalDate dataEmissao,
        boolean entrada, Long clienteId, Long fornecedorId, BigDecimal valorTotal) {}
