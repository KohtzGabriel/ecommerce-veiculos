package br.unitins.tp2.dto;

import java.math.BigDecimal;

public record CatalogoVeiculoResponse(Long id, String descricao, BigDecimal valorVenda, Long marcaId,
        String modelo, Integer anoFabricacao, Integer anoModelo, String cor,
        String marca, String status) {}
