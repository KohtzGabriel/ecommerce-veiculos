package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.TelefoneRequestDTO;
import br.unitins.tp2.dto.TelefoneResponseDTO;
import br.unitins.tp2.model.Telefone;

public final class TelefoneMapper {

    private TelefoneMapper() {
    }

    public static Telefone toModel(TelefoneRequestDTO dto) {
        Telefone telefone = new Telefone();
        telefone.setCodigoArea(dto.codigoArea());
        telefone.setNumero(dto.numero());
        telefone.setWhatsapp(dto.whatsapp());
        return telefone;
    }

    public static TelefoneResponseDTO toResponse(Telefone telefone) {
        return new TelefoneResponseDTO(
                telefone.getCodigoArea(),
                telefone.getNumero(),
                telefone.isWhatsapp());
    }
}