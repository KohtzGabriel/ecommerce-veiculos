package br.unitins.tp2.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ClienteResponseDTO(
        Long id,
        String nome,
        String email,
        List<TelefoneResponseDTO> telefones,
        String cpf,
        LocalDate dataNascimento,
        String genero,
        EnderecoResponseDTO endereco,
        LocalDateTime dataCadastro) {
}
