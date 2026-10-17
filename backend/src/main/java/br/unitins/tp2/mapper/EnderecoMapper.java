package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.EnderecoResponseDTO;
import br.unitins.tp2.model.Endereco;

public final class EnderecoMapper {

    private EnderecoMapper() {
    }

    public static EnderecoResponseDTO toResponse(Endereco endereco) {
        return new EnderecoResponseDTO(
                endereco.getCep(),
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getEstado());
    }
}
