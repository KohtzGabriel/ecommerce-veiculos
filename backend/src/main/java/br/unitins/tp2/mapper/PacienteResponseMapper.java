package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.PacienteResponseDTO;
import br.unitins.tp2.model.Paciente;
import br.unitins.tp2.model.Pessoa;

public final class PacienteResponseMapper {

    private PacienteResponseMapper() {
    }

    public static PacienteResponseDTO toResponse(Paciente paciente) {
        return new PacienteResponseDTO(
                paciente.getId(),
                paciente.getPessoa().getNome(),
                paciente.getPessoa().getEmail(),
                paciente.getPessoa().getTelefones().stream().map(TelefoneMapper::toResponse).toList(),
                paciente.getPessoa().getCpf(),
                paciente.getPessoa().getDataNascimento(),
                paciente.getPessoa().getGenero(),
                paciente.getPessoa().getEnderecoLegado(),
                paciente.getObservacoes(),
                paciente.getDataCadastro(),
                paciente.isAtivo());
    }

    public static PacienteResponseDTO toResponse(Pessoa pessoa) {
        return new PacienteResponseDTO(
                null,
                pessoa.getNome(),
                pessoa.getEmail(),
                pessoa.getTelefones().stream().map(TelefoneMapper::toResponse).toList(),
                pessoa.getCpf(),
                pessoa.getDataNascimento(),
                pessoa.getGenero(),
                pessoa.getEnderecoLegado(),
                null,
                pessoa.getDataCadastro(),
                null);
    }
}
