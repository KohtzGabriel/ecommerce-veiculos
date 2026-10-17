package br.unitins.tp2.dto;

public record MunicipioResponseDTO(
    Long id,
    String nome,
    EstadoResponseDTO estado
) {
}