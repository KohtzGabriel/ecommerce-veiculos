package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.PsicologoResponseDTO;
import br.unitins.tp2.model.Pessoa;
import br.unitins.tp2.model.Psicologo;

public final class PsicologoResponseMapper {

    private PsicologoResponseMapper() {
    }

    public static PsicologoResponseDTO toResponse(Psicologo psicologo) {
        return new PsicologoResponseDTO(
                psicologo.getId(),
                psicologo.getPessoa().getNome(),
                psicologo.getPessoa().getEmail(),
                psicologo.getPessoa().getTelefones().stream().map(TelefoneMapper::toResponse).toList(),
                psicologo.getPessoa().getCpf(),
                psicologo.getPessoa().getDataNascimento(),
                psicologo.getPessoa().getGenero(),
                psicologo.getPessoa().getEnderecoLegado(),
                psicologo.getCrp(),
                psicologo.getEspecialidade(),
                psicologo.getBio(),
                psicologo.getValorConsulta(),
                psicologo.getDuracaoConsulta(),
                psicologo.getDataCadastro(),
                psicologo.isAtivo());
    }

    public static PsicologoResponseDTO toResponse(Pessoa pessoa) {
        return new PsicologoResponseDTO(
                null,
                pessoa.getNome(),
                pessoa.getEmail(),
                pessoa.getTelefones().stream().map(TelefoneMapper::toResponse).toList(),
                pessoa.getCpf(),
                pessoa.getDataNascimento(),
                pessoa.getGenero(),
                pessoa.getEnderecoLegado(),
                null,
                null,
                null,
                null,
                null,
                pessoa.getDataCadastro(),
                null);
    }
}
