package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.ClienteResponseDTO;
import br.unitins.tp2.model.Cliente;

public final class ClienteResponseMapper {

    private ClienteResponseMapper() {
    }

    public static ClienteResponseDTO toResponse(Cliente cliente) {
        var pessoa = cliente.getPessoa();
        return new ClienteResponseDTO(
                cliente.getId(),
                pessoa.getNome(),
                pessoa.getEmail(),
                pessoa.getTelefones().stream().map(TelefoneMapper::toResponse).toList(),
                pessoa.getCpf(),
                pessoa.getDataNascimento(),
                pessoa.getGenero(),
                EnderecoMapper.toResponse(pessoa.getEndereco()),
                cliente.getDataCadastro());
    }
}
