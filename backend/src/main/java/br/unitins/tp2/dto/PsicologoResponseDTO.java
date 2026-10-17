package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PsicologoResponseDTO(
    Long id,
    String nome,
    String email,
    List<TelefoneResponseDTO> telefones,
    String cpf,
    LocalDate dataNascimento,
    String genero,
    String endereco,
    String crp,
    String especialidade,
    String bio,
    BigDecimal valorConsulta,
    Integer duracaoConsulta,
    LocalDateTime dataCadastro,
    Boolean ativo
) {
}