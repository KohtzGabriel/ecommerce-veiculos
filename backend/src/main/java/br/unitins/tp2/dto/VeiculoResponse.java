package br.unitins.tp2.dto;

import java.math.BigDecimal;

import br.unitins.tp2.model.StatusVeiculo;

public record VeiculoResponse(Long id, String descricao, BigDecimal valorCusto, BigDecimal valorVenda,
        boolean ativo, String modelo, Integer anoFabricacao, Integer anoModelo, String cor,
        String placa, String chassi, String renavam, Integer quilometragem,
        Long marcaId, String marca, StatusVeiculo status) {}
