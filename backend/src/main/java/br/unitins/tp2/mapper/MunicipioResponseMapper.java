package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.MunicipioResponseDTO;
import br.unitins.tp2.model.Municipio;

public final class MunicipioResponseMapper {

    private MunicipioResponseMapper() {
    }

    public static MunicipioResponseDTO toResponse(Municipio municipio) {
        return new MunicipioResponseDTO(
                municipio.getId(),
                municipio.getNome(),
                EstadoResponseMapper.toResponse(municipio.getEstado()));
    }
}