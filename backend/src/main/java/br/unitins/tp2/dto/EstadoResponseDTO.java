package br.unitins.tp2.dto;

import br.unitins.tp2.model.Regiao;

public record EstadoResponseDTO (
    Long id,
    String nome,
    String sigla,
    Regiao regiao
) {
}
