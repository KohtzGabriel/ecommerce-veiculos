package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.EstadoResponseDTO;
import br.unitins.tp2.model.Estado;

public final class EstadoResponseMapper {

    private EstadoResponseMapper() {
    }

    public static EstadoResponseDTO toResponse(Estado estado) {
        return new EstadoResponseDTO(estado.getId(), estado.getNome(), estado.getSigla(), estado.getRegiao());
    }
}