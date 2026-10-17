package br.unitins.tp2.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PacienteResponseDTO(
    Long id,
    String nome,
    String email,
    List<TelefoneResponseDTO> telefones,
    String cpf,
    LocalDate dataNascimento,
    String genero,
    String endereco,
    String observacoes,
    LocalDateTime dataCadastro,
    Boolean ativo
) {
}