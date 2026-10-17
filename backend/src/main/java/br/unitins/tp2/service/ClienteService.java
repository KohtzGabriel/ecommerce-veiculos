package br.unitins.tp2.service;

import br.unitins.tp2.dto.ClienteRequestDTO;
import br.unitins.tp2.model.Cliente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface ClienteService {

    Cliente create(@NotNull @Valid ClienteRequestDTO dto);

    Cliente createAssociado(long pessoaId, boolean ativo);

    void delete(long id);

    Cliente findById(long id);

    Cliente findByCpf(String cpf);
}
